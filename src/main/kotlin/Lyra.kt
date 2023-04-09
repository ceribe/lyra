import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import messagesystem.MessageSystem
import serialization.MessageSerializer
import serialization.SerializationType

class Lyra<T: NodeState>(
    private val messageSystem: MessageSystem,
    private val initMessage: Message<T>?,
    serializationType: SerializationType = SerializationType.JSON,
    private val nodeState: T
) {
    val messageSerializer = MessageSerializer(serializationType)
    private val messageQueue = MessageQueue()

    /** The node that receives initMessage should execute this method last to ensure that all other nodes are ready to receive messages
     */
    fun run() {
        nodeState.numberOfNodes = messageSystem.init(nodeState.nodeNumber)
        sendInitMessage()
        runBlocking {
            while (true) {
                val incomingMessage = getNextMessage() ?: continue
                incomingMessage.state = nodeState
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
        messageSystem.sendTo(serializedMessage, nodeState.nodeNumber)
    }

    private fun getNextMessage(): Message<T>? {
        val serializedMessageWithNumber = messageSystem.receive()
        return messageSerializer.deserializeMessageFromString(serializedMessageWithNumber) as Message<T>?
    }

    private suspend fun reactToMessage(incomingMessage: Message<T>, scope: CoroutineScope) {
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
                messageEvent.message.sender = nodeState.nodeNumber
                val serializedMessage = messageSerializer.serializeMessageToString(messageEvent.message) ?: return
                messageSystem.sendToAll(serializedMessage)
            }
            is MessageEvent.SendToEvent -> {
                messageEvent.message.sender = nodeState.nodeNumber
                val serializedMessage = messageSerializer.serializeMessageToString(messageEvent.message) ?: return
                messageSystem.sendTo(serializedMessage, messageEvent.recipient)
            }
        }
    }
}