import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import messagesystem.MessageSystem

class Lyra(
    private val messageSystem: MessageSystem,
    val nodeNumber: Int,
    private val initMessage: Message?,
) {
    val messageSerializer = MessageSerializer()
    private val messageQueue = MessageQueue()

    fun run() {
        messageSystem.init(nodeNumber)
        sendInitMessage()
        runBlocking {
            while (true) {
                val incomingMessage = getNextMessage() ?: continue
                reactToMessage(incomingMessage, this)
                checkMessageQueue()
            }
        }
    }

    private fun sendInitMessage() {
        if (initMessage == null) {
            return
        }
        val serializedMessage = messageSerializer.serializeMessageToString(initMessage)
        if (serializedMessage == null) {
            println("Failed to serialize init message")
            return
        }
        messageSystem.sendTo(serializedMessage, nodeNumber)
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
        var wasAnyConditionMet = false
        messageQueue.values.forEach { (channel, waitForConditionIsMet) ->
            if (waitForConditionIsMet()) {
                activateMessage(channel)
                wasAnyConditionMet = true
            }
        }
        if (wasAnyConditionMet) {
            checkMessageQueue()
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
                messageEvent.message.sender = nodeNumber
                val serializedMessage = messageSerializer.serializeMessageToString(messageEvent.message) ?: return
                messageSystem.sendToAll(serializedMessage)
            }
            is MessageEvent.SendToEvent -> {
                messageEvent.message.sender = nodeNumber
                val serializedMessage = messageSerializer.serializeMessageToString(messageEvent.message) ?: return
                messageSystem.sendTo(serializedMessage, messageEvent.recipient)
            }
        }
    }
}