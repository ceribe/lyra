# Lyra: Distributed Algorithms Platform

Lyra is a library that helps with implementation of distributed algorithms. Instead of the typical approach the whole algorithm is programmed as a set of responses to incoming messages. Serialization, synchronization, message passing are abstracted away so the only thing that end-user (programmer) has to do is implement the algorithm.

In the current state the project is finished. However documentation and tests aren't finished. I might do it at some point. 

# How to use

### 1. Add lyra module to your project
Either copy the files or link the module.

### 2. Create a class which implements NodeState

An instance of this class will be used to store local state of a node

```kotlin
class ExampleState(nodeNumber: Int): NodeState(nodeNumber) {
    var numberOfConfirmations = 0
    var numberOfRequests = 0
}
```

### 3. Implement the algorithm

The algorithm implemented below works like this:
- Each node sends a request message to each node
- After getting a request from each node, each node sends a response message back
- After getting a response from each node, each node does "some work"

```kotlin
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
```

- sendToAll - Sends given message to all nodes in the system (including the node node that's sending the message)
- sendTo - Sands given message to node with given index
- waitFor - Suspends the execution of the "react" function until the given condition is met

### 5. Create a Lyra instance and run it

```kotlin
val lyra = Lyra(
    messageSystem = SocketMessageSystem(...),
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
```

- messageSystem - Any class that implements "MessageSystem" interface. This component is used to send messages between nodes
- initMessage - Message that node will send to itself after initalization. If is null then no message is send
- serializationType - Either JSON or ProtoBuf
- nodeState - Initial state that the node will use
- synchronizeNodes - Function that should make it so all nodes are initialized before any message is send so no messages are lost

In { } all used message types have to be registered (in same order an all nodes).
An instance of Lyra class has to be created and run on all nodes in the system.
