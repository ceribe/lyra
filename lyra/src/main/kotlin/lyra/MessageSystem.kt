package lyra

interface MessageSystem {
    fun send(message: String)
    fun receive(): String
}