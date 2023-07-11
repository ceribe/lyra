import examplealgorithm.ExampleState
import examplealgorithm.InitMessage
import examplealgorithm.RequestMessage
import examplealgorithm.ResponseMessage
import messagesystem.socket.SocketAddress
import messagesystem.socket.SocketMessageSystem
import messagesystem.websocket.WebsocketAddress
import messagesystem.zeromq.ZeroMQAddress
import serialization.SerializationType
import java.io.File

fun main(args: Array<String>) {
    val nodeAddresses = File("nodes.txt").readLines()
    val nodeName = args[0]
    val nodeNumber = nodeAddresses.indexOf(nodeName)
    val lyra = Lyra(
        messageSystem = SocketMessageSystem(getSocketAddresses(nodeAddresses)),
        initMessage = InitMessage(),
        serializationType = SerializationType.JSON,
        nodeState = ExampleState(nodeNumber),
        synchronizeNodes = { Thread.sleep(30000) }
    ) {
        registerMessageType<InitMessage>()
        registerMessageType<RequestMessage>()
        registerMessageType<ResponseMessage>()
    }
    lyra.run()
}

fun getSocketAddresses(nodeAddresses: List<String>): List<List<SocketAddress>> {
    return nodeAddresses.map { address ->
        (8000 until (8000 + nodeAddresses.size)).map {
            SocketAddress(address, it)
        }
    }
}
fun getWebsocketAddresses(nodeAddresses: List<String>): List<WebsocketAddress> {
    return nodeAddresses.map { address ->
        WebsocketAddress(address, 8000)
    }
}
fun getZeroMQAddresses(nodeAddresses: List<String>): List<List<ZeroMQAddress>> {
    return nodeAddresses.map { address ->
        (8000 until (8000 + nodeAddresses.size)).map {
            ZeroMQAddress(address, it)
        }
    }
}
