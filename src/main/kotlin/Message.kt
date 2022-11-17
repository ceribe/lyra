import kotlinx.coroutines.channels.Channel

abstract class Message {
    val channel = Channel<Unit>()

    lateinit var onMessageEvent: (MessageEvent) -> Unit

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
        channel.receive()
    }

    suspend fun sendToAll(message: Message) {
        onMessageEvent(MessageEvent.SendToAllEvent(message))
    }

    suspend fun sendTo(message: Message, recipient: Int) {
        onMessageEvent(MessageEvent.SendToEvent(message, recipient))
    }
}