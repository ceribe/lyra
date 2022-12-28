package messagesystem

import messagesystem.zeromq.NodeAddress

interface MessageSystem {

    fun sendTo(message: String, recipient: Int)
    fun sendToAll(message: String)
    fun receive(): String
    fun init()
}