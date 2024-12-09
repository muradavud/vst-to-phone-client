package com.muradavud.vsttomobile

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import java.util.Timer
import java.util.TimerTask
import kotlin.concurrent.schedule

class MainActivity : ComponentActivity(), PacketListener {
    private lateinit var vstListener: VstListener
    private lateinit var audioTrack: AudioTrack
    private var foundStreamState = mutableStateOf(false)
    private var sampleRateState = mutableIntStateOf(0)
    private val timer = Timer()
    private var timerTask: TimerTask? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        vstListener = AndroidVstListener(
            port = Constants.PORT,
            bufferSize = Constants.BUFFER_SIZE,
            listener = this,
        )
        vstListener.start()

        setContent {
            App(
                foundStreamState, onMuteClick = { handleMuteClick() }, sampleRate = sampleRateState
            )
        }
    }

    private fun handleMuteClick() {
        if (audioTrack.playState == AudioTrack.PLAYSTATE_PLAYING) {
            audioTrack.pause()
            audioTrack.flush()
        } else {
            audioTrack.play()
        }
    }

    override fun onPacketReceived(buffer: ByteArray, length: Int, offset: Int) {
        audioTrack.write(buffer, offset, length)
        foundStreamState.value = true

        timerTask?.cancel()
        timerTask = timer.schedule(Constants.STREAM_TIMEOUT) {
            foundStreamState.value = false
        }
    }

    override fun onSampleRateChanged(sampleRate: Int) {
        sampleRateState.intValue = sampleRate
        audioTrack = AudioTrack(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build(),
            AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_STEREO).build(),
            Constants.PLAYER_BUFFER_SIZE,
            AudioTrack.MODE_STREAM,
            AudioManager.AUDIO_SESSION_ID_GENERATE
        )
        audioTrack.play()
    }
}