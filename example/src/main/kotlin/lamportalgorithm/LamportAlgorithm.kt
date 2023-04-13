package lamportalgorithm

import Message
import NodeState
import java.util.*

data class QueueItem(val timestamp: Int, val nodeNumber: Int) : Comparable<QueueItem> {
    override fun compareTo(other: QueueItem): Int {
        return when {
            timestamp > other.timestamp -> 1
            timestamp < other.timestamp -> -1
            else -> nodeNumber.compareTo(other.nodeNumber)
        }
    }
}

class LamportState(nodeNumber: Int): NodeState(nodeNumber) {
    val queue: PriorityQueue<QueueItem> = PriorityQueue()
    var time = 0
    var numberOfResponses = 0
}

fun doSomeWork() {
    println("Entering critical section")
    Thread.sleep(5000)
    println("Exiting critical section")
}

@kotlinx.serialization.Serializable
class InitMessage : Message<LamportState>() {
    override suspend fun react() {
        sendToAll(message = RequestMessage(state.time))
    }
}

@kotlinx.serialization.Serializable
class RequestMessage(private val timestamp: Int) : Message<LamportState>() {
    override suspend fun react() {
        println("Got request from $sender")
        state.queue.add(QueueItem(timestamp, sender))
        sendTo(message = ResponseMessage(), recipient = sender)
    }
}

@kotlinx.serialization.Serializable
class ResponseMessage : Message<LamportState>() {
    override suspend fun react() {
        println("Got response from $sender")
        state.numberOfResponses++
        println("Number of responses: ${state.numberOfResponses}/${state.numberOfNodes}")
        if (state.numberOfResponses < state.numberOfNodes) {
            return
        }

        waitFor { state.queue.peek().nodeNumber == state.nodeNumber }

        doSomeWork()

        sendToAll(message = ReleaseMessage())
        state.time++
        state.numberOfResponses = 0
        sendToAll(message = RequestMessage(state.time))
    }
}

@kotlinx.serialization.Serializable
class ReleaseMessage : Message<LamportState>() {
    override suspend fun react() {
        println("Got release from $sender")
        state.queue.removeIf { it.nodeNumber == sender }
    }
}