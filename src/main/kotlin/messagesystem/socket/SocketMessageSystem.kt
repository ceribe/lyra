package messagesystem.socket

import messagesystem.MessageSystem
import java.net.ServerSocket
import java.net.Socket

/**
 * MessageSystem implementation using sockets.
 * @param allSocketAddresses list of addresses of all nodes in the network. Eg the first list is the address of the first node, the second list is the address of the second node, etc.
 */
class SocketMessageSystem(private val allSocketAddresses: List<SocketAddress>) : MessageSystem {
    private lateinit var receiveSocket: ServerSocket

    override fun init(nodeNumber: Int): Int {
        val address = allSocketAddresses[nodeNumber]
        receiveSocket = ServerSocket(address.port)
        return allSocketAddresses.size
    }

    override fun sendTo(message: String, recipient: Int) {
        val recipientSocketAddress = allSocketAddresses[recipient]
        val recipientSocket = Socket(recipientSocketAddress.ipAddress, recipientSocketAddress.port)
        recipientSocket.getOutputStream().write(message.toByteArray())
        recipientSocket.close()
    }

    override fun sendToAll(message: String) {
        for (i in allSocketAddresses.indices) {
            sendTo(message, i)
        }
    }

    override fun receive() = receiveSocket
        .accept()
        .getInputStream()
        .bufferedReader()
        .readLines()
        .joinToString("\n")
}