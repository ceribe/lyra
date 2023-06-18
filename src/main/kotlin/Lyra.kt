import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import messagesystem.MessageSystem
import serialization.MessageSerializer
import serialization.SerializationType

/**
 * Main class used to run the Lyra network of nodes. To send and receive messages it will use the provided [messageSystem].
 * All messages are serialized before being sent and deserialized after being received using a serialization method
 * that matches [serializationType]. During the execution given [nodeState] will be used to hold the state of the node.
 * After initialization [synchronizeNodes] will be called to handle synchronization of nodes. All nodes should reach
 * this function before any of them starts sending messages. After nodes are synchronized [initMessage] will be sent to
 * this node. Each node has to run its own instance of this class. [registerMessageTypes] is used to register all
 * message types that will be used in the network.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class Lyra<T: NodeState>(
    private val messageSystem: MessageSystem,
    private val initMessage: Message<T>?,
    private val serializationType: SerializationType = SerializationType.JSON,
    private val nodeState: T,
    private val synchronizeNodes: () -> Unit,
    registerMessageTypes: MessageSerializer<T>.() -> Unit
) {
    private val messageSerializer = MessageSerializer<T>(serializationType)
    private val messageQueue = MessageQueue()

    init {
        messageSerializer.registerMessageTypes()
    }
    /**
     * Starts the execution of the node. This function will block until the node is stopped.
     */
    fun run() {
        nodeState.numberOfNodes = messageSystem.init(nodeState.nodeNumber)
        synchronizeNodes()
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

    /**
     * Checks which messages are ready to be processed and activates them. Will call itself recursively until no more
     * messages are ready to be processed.
     */
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
        if (channel.isClosedForSend) {
            return
        }
//        println("[L] Activating channel $channel")
        // Send is doubled here to ensure that this coroutine gets suspended and waits till the message coroutine is done.
        // Otherwise, it would be possible to activate the same message several times, which would make waitFor() not work.
        // The second send will be received either after react() is done or after the next waitFor() is called.
        channel.send(Unit)
        channel.send(Unit)
//        println("[L] Activated channel $channel")
    }

    private fun onMessageEvent(messageEvent: MessageEvent) {
        when (messageEvent) {
            is MessageEvent.AddNewConditionEvent -> messageQueue[messageEvent.channel] = messageEvent.condition
            is MessageEvent.RemoveMessageFromQueue ->
            {
                val channel = messageEvent.channel
                // Sometimes channel can get activated after message stopped being processed, this could stop the program from working
                if (!channel.isEmpty)
                {
                    runBlocking {
                        channel.receive()
                    }
                }
                channel.close()
                messageQueue.remove(channel)
            }
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