import kotlinx.coroutines.channels.Channel

class MessageQueue {
    private val queue = mutableMapOf<Channel<Unit>, () -> Boolean>()

    val values: List<Pair<Channel<Unit>, () -> Boolean>>
        get() = queue.toList()

    operator fun set(channel: Channel<Unit>, condition: () -> Boolean) {
        queue[channel] = condition
    }

    fun remove(channel: Channel<Unit>) {
        queue.remove(channel)
    }
}