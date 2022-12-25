fun main() {
    val lyra = Lyra(listOf())
    lyra.messageSerializer.apply {
        registerMessageType<ExampleMessage>()
    }
    val message: Message = ExampleMessage(5)
    val serializer = lyra.messageSerializer.serdesMap[message::class]!!.serialize
    val serializedMessage = serializer(message)
    println(serializedMessage)

    val deserializer = lyra.messageSerializer.serdesMap[ExampleMessage::class]!!.deserialize
    val msg = deserializer(serializedMessage)
    println(msg)

    val serializedMessageWithNumber = lyra.messageSerializer.serializeMessageToString(message) ?: return
    println(serializedMessageWithNumber)

    val deserializedMessage = lyra.messageSerializer.deserializeMessageFromString(serializedMessageWithNumber)
    println(deserializedMessage)
}