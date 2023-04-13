package examplealgorithm

import Message
import NodeState

class ExampleState(nodeNumber: Int): NodeState(nodeNumber) {
    var numberOfConfirmations = 0
    var numberOfRequests = 0
}

fun doSomeWork() {
    println("Entering critical section")
    Thread.sleep(5000)
    println("Exiting critical section")
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
        println("Got request from $sender")
        state.numberOfRequests++
        println("Number of requests: ${state.numberOfRequests}/${state.numberOfNodes}")
        waitFor { state.numberOfRequests == state.numberOfNodes }
        sendTo(
            message = ResponseMessage(),
            recipient = sender
        )
        println("Sent response to $sender")
    }
}

@kotlinx.serialization.Serializable
class ResponseMessage : Message<ExampleState>() {
    override suspend fun react() {
        println("Got response from $sender")
        state.numberOfConfirmations++
        println("Number of confirmations: ${state.numberOfConfirmations}/${state.numberOfNodes}")
        if (state.numberOfConfirmations < state.numberOfNodes) {
            return
        }
        doSomeWork()
    }
}

