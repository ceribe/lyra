package examplealgorithm

import Message
import NodeState

class ExampleState(nodeNumber: Int): NodeState(nodeNumber) {
    var numberOfConfirmations = 0
    var numberOfRequests = 0
}

fun doSomeWork() {
    println("Doing some work")
}

@kotlinx.serialization.Serializable
class InitMessage : Message<ExampleState>() {
    override suspend fun react() {
        sendToAll(message = RequestMessage())
    }
}

@kotlinx.serialization.Serializable
class RequestMessage : Message<ExampleState>() {
    override suspend fun react() {
        state.numberOfRequests++
        waitFor { state.numberOfRequests == state.numberOfNodes }
        sendTo(
            message = ResponseMessage(),
            recipient = sender
        )
    }
}

@kotlinx.serialization.Serializable
class ResponseMessage : Message<ExampleState>() {
    override suspend fun react() {
        state.numberOfConfirmations++
        if (state.numberOfConfirmations < state.numberOfNodes) {
            return
        }
        doSomeWork()
    }
}

