package com.muradavud.vsttomobile

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.NetworkInterface
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Timer


class AndroidVstListener(
    private var port: Int,
    private var bufferSize: Int,
    override var listener: PacketListener,
) : VstListener {
    private var sampleRate: Int? = null
    private var socket: DatagramSocket? = null
    private val timer = Timer()
    private val discoveryHeader: ByteArray = Constants.DISCOVERY_HEADER.toByteArray()
    private val audioHeader: ByteArray = Constants.AUDIO_HEADER.toByteArray()


    override fun start() {
        CoroutineScope(Dispatchers.IO).launch {
            startDiscovery()
        }
        CoroutineScope(Dispatchers.IO).launch {
            listen()
        }
    }

    private fun startDiscovery() {
        val broadcastAddress = getBroadcastAddress()
        val packet = DatagramPacket(discoveryHeader, discoveryHeader.size, broadcastAddress, port)
        DatagramSocket().use { socket ->
            socket.broadcast = true
            while (true) {
                socket.send(packet)
                Log.d("VstListener", "Discovery packet sent")
                Thread.sleep(2000)
            }
        }
    }

    private fun listen() {
        try {
            socket = DatagramSocket(port)

            Log.d("VstListener", "Listening on port: $port")

            while (true) {
                val buffer = ByteArray(bufferSize)
                val packet = DatagramPacket(buffer, buffer.size)
                socket?.receive(packet)
                if (!buffer.copyOfRange(0, 4).contentEquals(audioHeader)) {
                    println("Alien packet")
                    continue
                }
                Log.d(
                    "VstListener",
                    "Packet received from: ${packet.address.hostAddress}:${packet.port}"
                )
                Log.d("VstListener", "Packet length: ${packet.length}")

                val sampleRate =
                    ByteBuffer.wrap(buffer.copyOfRange(4, 8)).order(ByteOrder.LITTLE_ENDIAN).int

                if (sampleRate != this.sampleRate || this.sampleRate == null) {
                    this.sampleRate = sampleRate
                    Log.d("VstListener", "Sample Rate: $sampleRate")
                    listener.onSampleRateChanged(sampleRate)
                }

                if (packet.length > 0) {
                    listener.onPacketReceived(buffer, packet.length - 8, 8)
                }
            }
        } catch (e: Exception) {
            Log.e("VstListener", "Exception occurred", e)
        } finally {
            socket?.close()
            timer.cancel()
        }
    }

    private fun getBroadcastAddress(): InetAddress {
        val interfaces = NetworkInterface.getNetworkInterfaces()
        while (interfaces.hasMoreElements()) {
            val networkInterface = interfaces.nextElement()
            if (networkInterface.isLoopback || !networkInterface.isUp) {
                continue
            }

            for (interfaceAddress in networkInterface.interfaceAddresses) {
                val broadcast = interfaceAddress.broadcast ?: continue

                return broadcast
            }
        }
        throw IllegalStateException("No broadcast address found")
    }
}

