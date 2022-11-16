fun main() {
    val lyra = Lyra().apply {
        registerMessageType<ExampleMessage>()
    }
    val serializer = lyra.serdesMap[ExampleMessage::class]!!.serialize
    val message = ExampleMessage(5)
    val serializedMessage = serializer(message)
    println(serializedMessage)

    val deserializer = lyra.serdesMap[ExampleMessage::class]!!.deserialize
    val msg = deserializer(serializedMessage)
    println(msg)
}