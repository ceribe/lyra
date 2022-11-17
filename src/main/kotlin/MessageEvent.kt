import kotlinx.coroutines.channels.Channel

sealed class MessageEvent {
    data class AddNewConditionEvent(val channel: Channel<Unit>, val condition: () -> Boolean): MessageEvent()
    data class RemoveMessageFromQueue(val channel: Channel<Unit>): MessageEvent()
    data class SendToAllEvent(val message: Message): MessageEvent()
    data class SendToEvent(val message: Message, val recipient: Int): MessageEvent()
}
