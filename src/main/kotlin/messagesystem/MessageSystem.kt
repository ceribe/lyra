package messagesystem

interface MessageSystem {
    fun init(nodeNumber: Int): Int
    fun sendTo(message: String, recipient: Int)
    fun sendToAll(message: String)
    fun receive(): String
}