package messagesystem.socketmessagesystem

import messagesystem.MessageSystem
import java.net.ServerSocket
import java.net.Socket

class SocketMessageSystem(private val allSocketAddresses: List<SocketAddress>) : MessageSystem {
    private lateinit var receiveSocket: ServerSocket

    override fun init(nodeNumber: Int) {
        val address = allSocketAddresses[nodeNumber]
        receiveSocket = ServerSocket(address.port)
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