import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import messagesystem.MessageSystem
import messagesystem.NodeAddress
import messagesystem.ZeroMQMessageSystem

class Lyra(
    private val addresses: List<NodeAddress>,
    private val messageSystem: MessageSystem = ZeroMQMessageSystem(),
) {
    val messageSerializer = MessageSerializer()
    private val messageQueue = MessageQueue()

    fun run() {
        messageSystem.setAddresses(addresses)
        messageSystem.init()
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
        return messageSerializer.deserializeMessageFromString(serializedMessageWithNumber)
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
            is MessageEvent.SendToAllEvent -> {
                val serializedMessageWithNumber = messageSerializer.serializeMessageToString(messageEvent.message) ?: return
                messageSystem.sendToAll(serializedMessageWithNumber)
            }
            is MessageEvent.SendToEvent -> {
                val serializedMessageWithNumber = messageSerializer.serializeMessageToString(messageEvent.message) ?: return
                messageSystem.sendTo(serializedMessageWithNumber, messageEvent.recipient)
            }
        }
    }
}