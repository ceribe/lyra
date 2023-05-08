package messagesystem.zeromq

data class ZeroMQAddress(val ipAddress: String, val port: Int) {
    override fun toString(): String {
        return "$ipAddress:$port"
    }
}
