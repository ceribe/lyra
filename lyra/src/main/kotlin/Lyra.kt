import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString


class Lyra(private val messageSystem: MessageSystem = ZeroMQMessageSystem()) {
    data class SerDes(val serialize: (Message) -> String, val deserialize: (String) -> Message)
    val serdesMap = mutableMapOf<KClass<*>, SerDes>()
    val numberToClassType = mutableMapOf<Int, KClass<*>>()
    val classTypeToNumber = mutableMapOf<KClass<*>, Int>()

    val messageQueue = mutableListOf<Pair<Channel<Unit>, () -> Boolean>>()

    inline fun <reified T : Message> registerMessageType() {
        serdesMap[T::class] = SerDes({ Json.encodeToString(it as T) }, { Json.decodeFromString(it) as T })
        val newClassNumber = numberToClassType.size
        numberToClassType[newClassNumber] = T::class
        classTypeToNumber[T::class] = newClassNumber
    }

    fun send(message: Message) {
        val serializer = serdesMap[message::class]?.serialize ?: return
        messageSystem.send(serializer(message))
    }

    fun run() {
        runBlocking {
            while (true) {
                val incomingMessage = getNextMessage() ?: continue
                reactToMessage(incomingMessage, this)
                checkMessageQueue()
            }
        }
    }

    private fun getNextMessage(): Message? {
        val message = messageSystem.receive()
        val typeNumber = message.substringBefore(':').toInt() // TODO
        val classType = numberToClassType[typeNumber] ?: return null
        val deserialize = serdesMap[classType]?.deserialize ?: return null
        return deserialize(message)
    }

    private suspend fun reactToMessage(incomingMessage: Message, scope: CoroutineScope) {
        scope.launch {
            incomingMessage.prepareAndReact(this@Lyra)
        }
        incomingMessage.channel.send(Unit)
    }

    private suspend fun checkMessageQueue() {
        messageQueue.forEach { (channel, canContinue) ->
            if (canContinue()) {
                channel.send(Unit)
            }
        }
    }
}