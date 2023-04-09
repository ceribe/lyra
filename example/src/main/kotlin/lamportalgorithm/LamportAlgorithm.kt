package lamportalgorithm

import Message
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

object NodeState {
    val queue: PriorityQueue<QueueItem> = PriorityQueue()
    var time = 0
    var numberOfResponses = 0
    const val numberOfNodes = 4
    const val nodeNumber = 0
}

fun doSomeWork() {
    println("Do some work")
}

@kotlinx.serialization.Serializable
class InitMessage : Message() {
    override suspend fun react() {
        sendToAll(message = RequestMessage(NodeState.time))
    }
}

@kotlinx.serialization.Serializable
class RequestMessage(private val timestamp: Int) : Message() {
    override suspend fun react() {
        NodeState.queue.add(QueueItem(timestamp, sender))
        sendTo(message = ResponseMessage(), recipient = sender)
    }
}

@kotlinx.serialization.Serializable
class ResponseMessage : Message() {
    override suspend fun react() {
        NodeState.numberOfResponses++
        if (NodeState.numberOfResponses < NodeState.numberOfNodes) {
            return
        }

        waitFor { NodeState.queue.peek().nodeNumber == NodeState.nodeNumber }

        doSomeWork()

        sendToAll(message = ReleaseMessage())
        NodeState.time++
        NodeState.numberOfResponses = 0
        sendToAll(message = RequestMessage(NodeState.time))
    }
}

@kotlinx.serialization.Serializable
class ReleaseMessage : Message() {
    override suspend fun react() {
        NodeState.queue.removeIf { it.nodeNumber == sender }
    }
}