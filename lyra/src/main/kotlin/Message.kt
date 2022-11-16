import kotlinx.coroutines.channels.Channel

abstract class Message {
    val channel = Channel<Unit>()

    /**
     * Reference to the queue which is used to synchronize "react" function calls.
     */
    lateinit var queue: MutableMap<Channel<Unit>, () -> Boolean>

    abstract suspend fun react()

    protected suspend fun waitFor(condition: () -> Boolean) {
        if (condition())
            return
        queue[channel] = condition
        waitForActivation()
    }

    suspend fun prepareAndReact(mainMessageQueue: MessageQueue) {
        this.queue = mainMessageQueue.queue
        waitForActivation()
        react()
        queue.remove(channel)
    }

    protected suspend fun waitForActivation() {
        channel.receive()
    }
}