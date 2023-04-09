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
    println("Do some work")
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
        state.queue.add(QueueItem(timestamp, sender))
        sendTo(message = ResponseMessage(), recipient = sender)
    }
}

@kotlinx.serialization.Serializable
class ResponseMessage : Message<LamportState>() {
    override suspend fun react() {
        state.numberOfResponses++
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
        state.queue.removeIf { it.nodeNumber == sender }
    }
}