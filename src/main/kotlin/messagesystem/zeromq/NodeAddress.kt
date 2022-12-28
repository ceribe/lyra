package messagesystem.zeromq

data class NodeAddress(val ipAddress: String, val port: Int) {
    override fun toString(): String {
        return "$ipAddress:$port"
    }
}
