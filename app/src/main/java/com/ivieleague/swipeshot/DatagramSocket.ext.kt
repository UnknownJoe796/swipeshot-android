package com.ivieleague.swipeshot

import com.lightningkite.kotlin.networking.gsonFrom
import com.lightningkite.kotlin.networking.gsonToString
import java.lang.reflect.Type
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketTimeoutException
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.TimeUnit

/**
 * Created by josep on 2/18/2017.
 */
fun DatagramSocket.receiveQueue(
        queue: ConcurrentLinkedQueue<DatagramPacket> = ConcurrentLinkedQueue()
): Queue<DatagramPacket> {
    Thread {
        while (!isClosed) {
            try {
                val data = ByteArray(4096)
                val packet = DatagramPacket(data, data.size)
                receive(packet)
                println("RECEIVING ${packet.address}")
                queue.add(packet)
            } catch(e: SocketTimeoutException) {
                /*squish*/
            } catch(e: Exception) {
                e.printStackTrace()
            }
        }
    }.start()
    return queue
}

fun DatagramSocket.sendQueue(
        queue: BlockingQueue<DatagramPacket> = ArrayBlockingQueue(128)
): BlockingQueue<DatagramPacket> {
    Thread {
        while (!isClosed) {
            var item: DatagramPacket? = null
            try {
                item = queue.poll(100, TimeUnit.MILLISECONDS)
                if (item != null) {
                    send(item)
                    println("SENDING")
                }
            } catch(e: Exception) {
                e.printStackTrace()
                //If it failed to send a packet, put the packet back in the queue
                if (item != null)
                    queue.add(item)
            }
        }
    }.start()
    return queue
}

fun DatagramPacket.string(): String = String(data, 0, length)
inline fun <reified T : Any> DatagramPacket.gsonFrom() = string().gsonFrom<T>()
fun <T : Any> DatagramPacket.gsonFrom(type: Type) = string().gsonFrom<T>(type)
fun <T : Any> T.gsonToDatagramPacket(address: InetAddress? = null, port: Int = -1): DatagramPacket {
    val data = this.gsonToString().toByteArray()
    return DatagramPacket(data, data.size, address, port)
}


//fun DatagramPacket.gsonFromUntyped(typeMap: Map<String, Type>):Any{
//    val string = this.string()
//    if(!string.startsWith("//")) throw IllegalArgumentException()
//    val type = typeMap[string.substring(2, string.indexOf('\n'))]!!
//    return toString().gsonFrom<Any>(type)
//}
//fun <T: Any> T.gsonToDatagramPacket(typeMap: Map<Class<*>, String>, address:InetAddress? = null, port:Int = -1):DatagramPacket{
//    val data = this.gsonToString().toByteArray()
//    return DatagramPacket(data, data.size, address, port)
//}

//Lifecycles

//fun LifecycleConnectable.bindNet()