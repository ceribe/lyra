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
    private val messageQueue = MessageQueue()

    inline fun <reified T : Message> registerMessageType() {
        serdesMap[T::class] = SerDes({ Json.encodeToString(it as T) }, { Json.decodeFromString(it) as T })
        val newClassNumber = numberToClassType.size
        numberToClassType[newClassNumber] = T::class
        classTypeToNumber[T::class] = newClassNumber
    }

    private fun serializeMessageWithNumber(message: Message): String? {
        val serializer = serdesMap[message::class]?.serialize ?: return null
        val serializedMessage = serializer(message)
        return "${classTypeToNumber[message::class]}:$serializedMessage"
    }

    private fun sendTo(message: Message, recipient: Int) {
        val serializedMessageWithNumber = serializeMessageWithNumber(message) ?: return
        messageSystem.sendTo(serializedMessageWithNumber, recipient)
    }

    private fun sendToAll(message: Message) {
        val serializedMessageWithNumber = serializeMessageWithNumber(message) ?: return
        messageSystem.sendToAll(serializedMessageWithNumber)
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
        val serializedMessageWithNumber = messageSystem.receive()
        val (typeNumber, serializedMessage) = serializedMessageWithNumber.split(":")
        val classType = numberToClassType[typeNumber.toInt()] ?: return null
        val deserialize = serdesMap[classType]?.deserialize ?: return null
        return deserialize(serializedMessage)
    }

    private suspend fun reactToMessage(incomingMessage: Message, scope: CoroutineScope) {
        scope.launch {
            incomingMessage.prepareAndReact(this@Lyra::onMessageEvent)
        }
        activateMessage(incomingMessage.channel)
    }

    private suspend fun checkMessageQueue() {
        messageQueue.values.forEach { (channel, waitForConditionIsMet) ->
            if (waitForConditionIsMet()) {
                activateMessage(channel)
            }
        }
    }

    private suspend fun activateMessage(channel: Channel<Unit>) {
        channel.send(Unit)
    }

    private fun onMessageEvent(messageEvent: MessageEvent) {
        when (messageEvent) {
            is MessageEvent.AddNewConditionEvent -> messageQueue[messageEvent.channel] = messageEvent.condition
            is MessageEvent.RemoveMessageFromQueue -> messageQueue.remove(messageEvent.channel)
            is MessageEvent.SendToAllEvent -> sendToAll(messageEvent.message)
            is MessageEvent.SendToEvent -> sendTo(messageEvent.message, messageEvent.recipient)
        }
    }
}