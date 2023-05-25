import atomicbroadcastalgorithm.*
import lamportalgorithm.LamportState
import lamportalgorithm.ReleaseMessage
import lamportalgorithm.RequestMessage
import lamportalgorithm.ResponseMessage
import messagesystem.socket.SocketAddress
import messagesystem.socket.SocketMessageSystem
import messagesystem.websocket.WebsocketAddress
import messagesystem.websocket.WebsocketMessageSystem
import messagesystem.zeromq.ZeroMQAddress
import messagesystem.zeromq.ZeroMQMessageSystem
import serialization.SerializationType
import java.io.File
import kotlin.concurrent.thread
import kotlin.system.exitProcess

fun main(args: Array<String>) {
    val nodeAddresses = File("nodes.txt").readLines()
    val nodeName = args[0]
    val nodeNumber = nodeAddresses.indexOf(nodeName)
    val algorithmNumber = args[1].toInt()
    val messageSystemNumber = args[2].toInt()
    val serializationTypeNumber = args[3].toInt()

    val serializationType = if (serializationTypeNumber == 0) SerializationType.JSON else SerializationType.PROTOBUF
    val messageSystem = when(messageSystemNumber) {
        0 -> {
            val addresses = getSocketAddresses(nodeAddresses)
            SocketMessageSystem(addresses)
        }
        1 -> {
            val addresses = getWebsocketAddresses(nodeAddresses)
            WebsocketMessageSystem(addresses)
        }
        2 -> {
            val addresses = getZeroMQAddresses(nodeAddresses)
            ZeroMQMessageSystem(addresses)
        }
        else -> {
            throw IllegalArgumentException("Unknown message system number: $messageSystemNumber")
        }
    }

    val getResult: () -> Long
    val lyra = when(algorithmNumber) {
        0 -> {
            val state = LamportState(nodeNumber)
            val lyra = Lyra(
                messageSystem = messageSystem,
                initMessage = lamportalgorithm.InitMessage(),
                serializationType = serializationType,
                nodeState = state,
                synchronizeNodes = { Thread.sleep(30000) }
            )
            lyra.messageSerializer.apply {
                registerMessageType<lamportalgorithm.InitMessage>()
                registerMessageType<RequestMessage>()
                registerMessageType<ResponseMessage>()
                registerMessageType<ReleaseMessage>()
            }
            getResult = { state.numberOfCriticalSectionEnters }
            lyra
        }
        1 -> {
            val state = AtomicBroadcastState(nodeNumber)
            val lyra = Lyra(
                messageSystem = messageSystem,
                initMessage = atomicbroadcastalgorithm.InitMessage(),
                serializationType = serializationType,
                nodeState = state,
                synchronizeNodes = { Thread.sleep(30000) }
            )
            lyra.messageSerializer.apply {
                registerMessageType<atomicbroadcastalgorithm.InitMessage>()
                registerMessageType<BroadcastMessage>()
                registerMessageType<ProposeMessage>()
                registerMessageType<AcceptProposeMessage>()
                registerMessageType<RejectProposeMessage>()
                registerMessageType<DeliverMessage>()
            }
            getResult = { state.numberOfDeliveredMessages }
            lyra
        }
        else -> {
            throw IllegalArgumentException("Unknown algorithm number: $algorithmNumber")
        }
    }

    thread(start = true) {
        lyra.run()
    }
    Thread.sleep(15*60*1000)
    println("$nodeName: ${getResult()}")
    exitProcess(0)
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
