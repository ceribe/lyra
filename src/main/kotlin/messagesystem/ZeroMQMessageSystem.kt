package messagesystem

class ZeroMQMessageSystem : MessageSystem {
    override fun sendTo(message: String, recipient: Int) {
        TODO("Not yet implemented")
    }

    override fun sendToAll(message: String) {
        TODO("Not yet implemented")
    }

    override fun receive(): String {
        TODO("Not yet implemented")
    }

    override fun setAddresses(addresses: List<NodeAddress>) {
        TODO("Not yet implemented")
    }

    override fun init() {
        TODO("Not yet implemented")
    }
}