package atomicbroadcastalgorithm

import Message
import NodeState
import java.util.*

data class SystemMessage(val content: String, val uuid: String)

class AtomicBroadcastState(nodeNumber: Int): NodeState(nodeNumber) {
    val deliveredMessagesUUIDs = hashSetOf<String>()
    val messagesToDeliver = mutableListOf<SystemMessage>()
    var lastProposedMessageIndex = 0
    var numberOfAcceptMessages = 0
    var proposedMessageUUID = ""

    fun setNextProposedMessageUUID() {
        numberOfAcceptMessages = 0
        lastProposedMessageIndex++
        if (lastProposedMessageIndex >= messagesToDeliver.size)
            lastProposedMessageIndex = 0

        proposedMessageUUID = messagesToDeliver[lastProposedMessageIndex].uuid
    }

    val isProposingAMessage: Boolean
        get() = proposedMessageUUID != ""
}

@kotlinx.serialization.Serializable
class InitMessage : Message<AtomicBroadcastState>() {
    override suspend fun react() {
        sendToAll(message = BroadcastMessage("Hello world!", UUID.randomUUID().toString()))
    }
}

@kotlinx.serialization.Serializable
class BroadcastMessage(private val content: String, private val uuid: String) : Message<AtomicBroadcastState>() {
    override suspend fun react() {
        println("Got broadcast from $sender")
        if (state.deliveredMessagesUUIDs.contains(uuid) || state.messagesToDeliver.any { it.uuid == uuid })
            return

        state.messagesToDeliver.add(SystemMessage(content, uuid))
        if (state.nodeNumber == 0 && !state.isProposingAMessage)
        {
            state.numberOfAcceptMessages = 0
            state.lastProposedMessageIndex = 0
            state.proposedMessageUUID = state.messagesToDeliver[0].uuid
            sendToAll(message = ProposeMessage(state.proposedMessageUUID))
        }
    }
}

@kotlinx.serialization.Serializable
class ProposeMessage(private val proposedMessageUUID: String) : Message<AtomicBroadcastState>() {
    override suspend fun react() {
        println("Got propose from $sender")
        if (state.messagesToDeliver.any { it.uuid == proposedMessageUUID }) {
            sendTo(message = AcceptProposeMessage(proposedMessageUUID), recipient = sender)
        }
        else
        {
            sendTo(message = RejectProposeMessage(proposedMessageUUID), recipient = sender)
        }
    }
}

@kotlinx.serialization.Serializable
class AcceptProposeMessage(private val proposedMessageUUID: String) : Message<AtomicBroadcastState>() {
    override suspend fun react() {
        if (proposedMessageUUID != state.proposedMessageUUID)
            return

        println("Got accept propose from $sender")
        state.numberOfAcceptMessages++

        if (state.numberOfAcceptMessages == state.numberOfNodes) {
            sendToAll(message = DeliverMessage(proposedMessageUUID))
            if (state.messagesToDeliver.size <= 1) {
                state.proposedMessageUUID = ""
            }
            else {
                state.setNextProposedMessageUUID()
                sendToAll(message = ProposeMessage(state.proposedMessageUUID))
            }
        }
    }
}

@kotlinx.serialization.Serializable
class RejectProposeMessage(private val proposedMessageHash: String) : Message<AtomicBroadcastState>() {
    override suspend fun react() {
        if (proposedMessageHash != state.proposedMessageUUID)
            return

        println("Got reject propose from $sender")
        state.setNextProposedMessageUUID()
        sendToAll(message = ProposeMessage(state.proposedMessageUUID))
    }
}

@kotlinx.serialization.Serializable
class DeliverMessage(private val deliveredMessageHash: String) : Message<AtomicBroadcastState>() {
    override suspend fun react() {
        val messageToDeliver = state.messagesToDeliver.find { it.uuid == deliveredMessageHash }!!
        println("Delivered message ${messageToDeliver.content} with UUID ${messageToDeliver.uuid}")
        state.deliveredMessagesUUIDs.add(deliveredMessageHash)
        state.messagesToDeliver.removeIf { it.uuid == deliveredMessageHash }

        sendToAll(message = BroadcastMessage("Hello world!", UUID.randomUUID().toString()))
    }
}