package lyra

class MessageSerializer<T : Message>(val typeNumber: Int) {
    fun serialize(message: T): String {
        return TODO()
    }

    fun deserialize(message: String): T {
        return TODO()
    }
}