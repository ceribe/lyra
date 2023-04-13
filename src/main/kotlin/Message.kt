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
        waitForActivation()
    }

    suspend fun prepareAndReact(onMessageEvent: (MessageEvent) -> Unit) {
        this.onMessageEvent = onMessageEvent
        waitForActivation()
        react()
        onMessageEvent(MessageEvent.RemoveMessageFromQueue(channel))
    }

    private suspend fun waitForActivation() {
//        println("Waiting for activation $channel")
        channel.receive()
//        println("Activated message from $channel")
    }

    suspend fun sendToAll(message: Message<T>) {
        onMessageEvent(MessageEvent.SendToAllEvent(message))
    }

    suspend fun sendTo(message: Message<T>, recipient: Int) {
        onMessageEvent(MessageEvent.SendToEvent(message, recipient))
    }
}