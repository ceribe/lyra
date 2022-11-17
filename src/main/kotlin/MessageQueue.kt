import kotlinx.coroutines.channels.Channel

interface MessageQueue {
    val queue: MutableMap<Channel<Unit>, () -> Boolean>
}