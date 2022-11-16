import kotlinx.coroutines.channels.Channel

abstract class Message {
    val channel = Channel<Unit>()
    lateinit var lyra: Lyra

    protected suspend fun waitFor(block: () -> Boolean) {
        if (block())
            return
        lyra.messageQueue.add(channel to block)
        channel.receive()
    }

    abstract suspend fun react()

    suspend fun prepareAndReact(lyra: Lyra) {
        this.lyra = lyra
        channel.receive()
        react()
        lyra.messageQueue.removeIf { it.first == channel }
    }
}