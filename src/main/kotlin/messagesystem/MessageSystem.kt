package messagesystem

interface MessageSystem {

    fun sendTo(message: String, recipient: Int)
    fun sendToAll(message: String)
    fun receive(): String
    fun setAddresses(addresses: List<NodeAddress>)
    fun init()
}