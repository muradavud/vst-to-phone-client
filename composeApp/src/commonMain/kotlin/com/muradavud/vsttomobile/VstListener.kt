package com.muradavud.vsttomobile

interface VstListener {
    fun start()
    var listener: PacketListener
}

interface PacketListener {
    fun onPacketReceived(buffer: ByteArray, length: Int, offset: Int)
    fun onSampleRateChanged(sampleRate: Int)
}