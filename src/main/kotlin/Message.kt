import kotlinx.coroutines.channels.Channel

abstract class Message<T: NodeState> {
    var sender: Int = -1
    val channel = Channel<Unit>()
    lateinit var onMessageEvent: (MessageEvent) -> Unit
    lateinit var state: T

    abstract suspend fun react()

    protected suspend fun waitFor(condition: () -> Boolean) {
        if (condition())
            return
        onMessageEvent(MessageEvent.AddNewConditionEvent(channel, condition))
        // Receives the second .send() from Lyra.activateMessage()
        channel.receive()
        waitForActivation()
    }

    suspend fun prepareAndReact(onMessageEvent: (MessageEvent) -> Unit) {
        this.onMessageEvent = onMessageEvent
        waitForActivation()
        react()
        // Receives the second .send() from Lyra.activateMessage()
        channel.receive()
        onMessageEvent(MessageEvent.RemoveMessageFromQueue(channel))
    }

    private suspend fun waitForActivation() {
//        println("[M] Waiting for activation $channel")
        // Waits for the first .send() in Lyra.activateMessage()
        channel.receive()
//        println("[M] Activated message from $channel")
    }

    suspend fun sendToAll(message: Message<T>) {
        onMessageEvent(MessageEvent.SendToAllEvent(message))
    }

    suspend fun sendTo(message: Message<T>, recipient: Int) {
        onMessageEvent(MessageEvent.SendToEvent(message, recipient))
    }
}