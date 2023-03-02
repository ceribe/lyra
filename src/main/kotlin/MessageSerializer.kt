import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.reflect.KClass

class MessageSerializer {
    data class SerDes(val serialize: (Message) -> String, val deserialize: (String) -> Message)
    val serdesMap = mutableMapOf<KClass<*>, SerDes>()
    val numberToClassType = mutableMapOf<Int, KClass<*>>()
    val classTypeToNumber = mutableMapOf<KClass<*>, Int>()

    inline fun <reified T : Message> registerMessageType() {
        serdesMap[T::class] = SerDes({ Json.encodeToString(it as T) }, { Json.decodeFromString(it) as T })
        val newClassNumber = numberToClassType.size
        numberToClassType[newClassNumber] = T::class
        classTypeToNumber[T::class] = newClassNumber
    }

    fun serializeMessageToString(message: Message): String? {
        val serializer = serdesMap[message::class]?.serialize ?: return null
        val serializedMessage = serializer(message)
        return "${message.sender}:${classTypeToNumber[message::class]}:$serializedMessage"
    }

    fun deserializeMessageFromString(serializedMessageWithNumber: String): Message? {
        val (sender, typeNumber, serializedMessage) = serializedMessageWithNumber.split(":", limit = 3)
        val classType = numberToClassType[typeNumber.toInt()] ?: return null
        val deserialize = serdesMap[classType]?.deserialize ?: return null
        val deserializedMessage = deserialize(serializedMessage)
        deserializedMessage.sender = sender.toInt()
        return deserializedMessage
    }
}