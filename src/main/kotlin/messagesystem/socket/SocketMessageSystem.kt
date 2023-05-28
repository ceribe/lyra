package messagesystem.socket

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.runBlocking
import messagesystem.MessageSystem
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket
import kotlin.concurrent.thread

/**
 * MessageSystem implementation using sockets.
 * @param allSocketAddresses list of addresses of all nodes in the network. Eg the first list is the address of the first node, the second list is the address of the second node, etc.
 */
class SocketMessageSystem(private val allSocketAddresses: List<List<SocketAddress>>) : MessageSystem {
    private lateinit var sendSockets: MutableList<PrintWriter?>
    private var nodeNumber: Int = 0
    private val messagesToReceive = Channel<String>(capacity = Channel.UNLIMITED)

    override fun init(nodeNumber: Int): Int {
        this.nodeNumber = nodeNumber
        val nodeAddresses = allSocketAddresses[nodeNumber]
        sendSockets = nodeAddresses.map { null }.toMutableList()

        nodeAddresses.forEach {
            thread(start = true) {
                val serverSocket = ServerSocket(it.port)
                val socket = serverSocket.accept()
                val reader = BufferedReader(InputStreamReader(socket.getInputStream()))
                while (true) {
                    val message = reader.readLine() ?: continue
                    runBlocking {
                        messagesToReceive.send(message)
                    }
                }
            }
        }

        return allSocketAddresses.size
    }

    override fun sendTo(message: String, recipient: Int) {
        if (sendSockets[recipient] == null)
        {
            val address = allSocketAddresses[recipient][nodeNumber]
            val socket = Socket(address.ipAddress, address.port)
            val writer = PrintWriter(socket.getOutputStream(), true)
            sendSockets[recipient] = writer
        }
        sendSockets[recipient]!!.println(message)
    }

    override fun sendToAll(message: String) {
        for (i in allSocketAddresses.indices) {
            sendTo(message, i)
        }
    }

    override fun receive(): String {
        return runBlocking {
            messagesToReceive.receive()
        }
    }
}