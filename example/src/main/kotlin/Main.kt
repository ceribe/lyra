import lamportalgorithm.*
import messagesystem.zeromq.NodeAddress
import messagesystem.zeromq.ZeroMQMessageSystem

fun main(args: Array<String>) {
    val nodeNumber = args[0].toInt()

    val lyra = Lyra(
        messageSystem = ZeroMQMessageSystem(
            listOf(
                listOf(NodeAddress("192.168.0.38", 8001), NodeAddress("192.168.0.38", 8002), NodeAddress("192.168.0.38", 8003)),
                listOf(NodeAddress("192.168.0.52", 8001), NodeAddress("192.168.0.52", 8002), NodeAddress("192.168.0.52", 8003)),
                listOf(NodeAddress("192.168.0.18", 8001), NodeAddress("192.168.0.18", 8002), NodeAddress("192.168.0.18", 8003)),
            )
        ),
        initMessage = InitMessage(),
        nodeState = LamportState(nodeNumber = nodeNumber)
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