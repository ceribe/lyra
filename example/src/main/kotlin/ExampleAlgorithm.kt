const val N = 5

object NodeState {
    var numberOfConfirmations = 0
    var numberOfRequests = 0
}

fun doSomeWork() {
    InitMessage()
}

@kotlinx.serialization.Serializable
class InitMessage : Message() {
    override suspend fun react() {
        sendToAll(message = RequestMessage())
    }
}

@kotlinx.serialization.Serializable
class RequestMessage : Message() {
    override suspend fun react() {
        NodeState.numberOfRequests++
        waitFor { NodeState.numberOfRequests == N }
        sendTo(
            message = ResponseMessage(),
            recipient = sender
        )
    }
}

@kotlinx.serialization.Serializable
class ResponseMessage : Message() {
    override suspend fun react() {
        NodeState.numberOfConfirmations++
        if (NodeState.numberOfConfirmations < N) {
            return
        }
        doSomeWork()
    }
}

