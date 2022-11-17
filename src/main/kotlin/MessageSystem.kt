interface MessageSystem {

    fun sendTo(message: String, recipient: Int)
    fun sendToAll(message: String)
    fun receive(): String
}