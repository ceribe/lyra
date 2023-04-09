package messagesystem.zeromq

import messagesystem.MessageSystem
import org.zeromq.SocketType
import org.zeromq.ZMQ

/**
 * MessageSystem implementation using ZeroMQ.
 * @param allNodesAddresses list of lists of addresses of all nodes in the network. Eg the first list is the addresses of the first node, the second list is the addresses of the second node, etc.
 */
class ZeroMQMessageSystem(private val allNodesAddresses: List<List<NodeAddress>>) : MessageSystem {
    private lateinit var pubSockets: List<ZMQ.Socket>
    private lateinit var subSocket: ZMQ.Socket

    override fun init(nodeNumber: Int): Int {
        val context = ZMQ.context(1)
        val nodeAddresses = allNodesAddresses[nodeNumber]

        pubSockets = nodeAddresses.map {
            val pubSocket = context.socket(SocketType.PUB)
            pubSocket.bind("tcp://${it}")
            pubSocket
        }

        subSocket = context.socket(SocketType.SUB)
        subSocket.subscribe("".toByteArray())
        allNodesAddresses.map { it[nodeNumber] }.forEach { subSocket.connect("tcp://${it}") }

        // After connecting all sockets program has to wait a bit so no messages are lost
        Thread.sleep(1000)
        return allNodesAddresses.size
    }

    override fun sendTo(message: String, recipient: Int) {
        pubSockets[recipient].send(message)
    }

    override fun sendToAll(message: String) {
        pubSockets.forEach { it.send(message) }
    }

    override fun receive(): String {
        return subSocket.recvStr()
    }
}