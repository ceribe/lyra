package messagesystem.zeromq

import messagesystem.MessageSystem

class ZeroMQMessageSystem(private val addresses: List<NodeAddress>) : MessageSystem {
    override fun sendTo(message: String, recipient: Int) {
        TODO("Not yet implemented")
    }

    override fun sendToAll(message: String) {
        TODO("Not yet implemented")
    }

    override fun receive(): String {
        TODO("Not yet implemented")
    }

    override fun init() {
        TODO("Not yet implemented")
    }
}