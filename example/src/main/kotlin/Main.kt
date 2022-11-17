fun main() {
    val lyra = Lyra().apply {
        registerMessageType<ExampleMessage>()
    }
    val message: Message = ExampleMessage(5)
    val serializer = lyra.serdesMap[message::class]!!.serialize
    val serializedMessage = serializer(message)
    println(serializedMessage)

    val deserializer = lyra.serdesMap[ExampleMessage::class]!!.deserialize
    val msg = deserializer(serializedMessage)
    println(msg)
}