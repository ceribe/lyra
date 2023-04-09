import lamportalgorithm.*
import messagesystem.zeromq.ZeroMQMessageSystem

fun main() {
    val lyra = Lyra(
        messageSystem = ZeroMQMessageSystem(listOf()),
        initMessage = InitMessage(),
        nodeState = LamportState(nodeNumber = 0)
    )

    lyra.messageSerializer.apply {
        registerMessageType<InitMessage>()
        registerMessageType<RequestMessage>()
        registerMessageType<ResponseMessage>()
        registerMessageType<ReleaseMessage>()
    }

    lyra.run()
//    val message: Message = ExampleMessage(5)
//    val serializer = lyra.messageSerializer.serdesMap[message::class]!!.serialize
//    val serializedMessage = serializer(message)
//    println(serializedMessage)
//
//    val deserializer = lyra.messageSerializer.serdesMap[ExampleMessage::class]!!.deserialize
//    val msg = deserializer(serializedMessage)
//    println(msg)
//
//    val serializedMessageWithNumber = lyra.messageSerializer.serializeMessageToString(message) ?: return
//    println(serializedMessageWithNumber)
//
//    val deserializedMessage = lyra.messageSerializer.deserializeMessageFromString(serializedMessageWithNumber)
//    println(deserializedMessage)
}