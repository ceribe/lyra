import lamportalgorithm.*
import messagesystem.socket.SocketAddress
import messagesystem.websocket.WebsocketAddress
import messagesystem.zeromq.ZeroMQAddress
import messagesystem.zeromq.ZeroMQMessageSystem
import java.io.File

fun main(args: Array<String>) {
    val nodeNumber = args[0].toInt()

    //read node addresses from nodes.txt
    val nodeAddresses = File("nodes.txt").readLines()

    val lyra = Lyra(
        messageSystem = ZeroMQMessageSystem(
            listOf(
                listOf(ZeroMQAddress("192.168.0.38", 8001), ZeroMQAddress("192.168.0.38", 8002), ZeroMQAddress("192.168.0.38", 8003)),
                listOf(ZeroMQAddress("192.168.0.52", 8001), ZeroMQAddress("192.168.0.52", 8002), ZeroMQAddress("192.168.0.52", 8003)),
                listOf(ZeroMQAddress("192.168.0.18", 8001), ZeroMQAddress("192.168.0.18", 8002), ZeroMQAddress("192.168.0.18", 8003)),
            )
        ),
        initMessage = InitMessage(),
        nodeState = LamportState(nodeNumber = nodeNumber),
        synchronizeNodes = {
            println("Press enter to start")
            readLine()
        }
    )

    lyra.messageSerializer.apply {
        registerMessageType<InitMessage>()
        registerMessageType<RequestMessage>()
        registerMessageType<ResponseMessage>()
        registerMessageType<ReleaseMessage>()
    }

    lyra.run()
}

fun getSocketAddresses(nodeAddresses: List<String>): List<SocketAddress> {
    return nodeAddresses.map { address ->
        SocketAddress(address, 8000)
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
