package com.muradavud.vsttomobile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    foundStreamState: State<Boolean>,
    onMuteClick: () -> Unit,
    sampleRate: State<Int>,
) {
    MaterialTheme {
        Column(
            Modifier
                .fillMaxWidth()
                .background(Color(0xFF333E44)),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            StreamStatusUI(
                foundStream = foundStreamState.value,
                onMuteClick = onMuteClick,
                sampleRate = sampleRate
            )
        }
    }
}


@Composable
fun StreamStatusUI(
    foundStream: Boolean,
    onMuteClick: () -> Unit,
    sampleRate: State<Int>,
) {
    val isMuted = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        if (foundStream) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sample rate: ${sampleRate.value}", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mute", color = Color.White)
                    Switch(checked = isMuted.value, onCheckedChange = {
                        isMuted.value = it
                        onMuteClick()
                    })
                }
            }
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Looking for stream", color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                CircularProgressIndicator()
            }
        }
    }
}