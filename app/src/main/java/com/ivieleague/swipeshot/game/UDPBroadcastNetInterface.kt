package com.ivieleague.swipeshot.game

import com.ivieleague.swipeshot.*
import java.lang.reflect.Type
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * Created by josep on 2/18/2017.
 */
inline fun <reified T : Any> UDPBroadcastNetInterface(
        address: InetAddress = InetAddress.getByName("255.255.255.255"),
        port: Int = 42392
): UDPBroadcastNetInterface<T> = UDPBroadcastNetInterface(
        type = T::class.java,
        address = address,
        port = port
)

class UDPBroadcastNetInterface<T : Any>(
        val type: Type,
        val address: InetAddress,
        val port: Int
) : NetInterface<T?> {

    var socket: DatagramSocket? = null
    fun connect() {
        if (socket != null) disconnect()
        socket = DatagramSocket(port).apply {
            soTimeout = 1000
        }
        socket!!.receiveQueue(socketReceiveQueue)
        socket!!.sendQueue(socketSendQueue)
    }

    fun disconnect() {
        socket?.close()
        socket = null
    }

    override val broadcastQueue: Queue<T?>
        get() = socketSendQueue.mapping<DatagramPacket, T?>(
                forwards = {
                    try {
                        it.gsonFrom<T>(type)!!
                    } catch(e: Exception) {
                        e.printStackTrace(); null
                    }
                },
                reverse = { it!!.gsonToDatagramPacket(address, port) }
        )
    override val receiveQueue: Queue<T?>
        get() = socketReceiveQueue.mapping<DatagramPacket, T?>(
                forwards = {
                    try {
                        it.gsonFrom<T>(type)!!
                    } catch(e: Exception) {
                        e.printStackTrace(); null
                    }
                },
                reverse = { it!!.gsonToDatagramPacket(address, port) }
        )

    val socketReceiveQueue = ConcurrentLinkedQueue<DatagramPacket>()
    val socketSendQueue = ArrayBlockingQueue<DatagramPacket>(128)
}