package serialization

import Message
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf
import kotlin.reflect.KClass

class MessageSerializer(val serializationType: SerializationType) {
    data class SerDes(val serialize: (Message) -> String, val deserialize: (String) -> Message)
    val serdesMap = mutableMapOf<KClass<*>, SerDes>()
    val numberToClassType = mutableMapOf<Int, KClass<*>>()
    val classTypeToNumber = mutableMapOf<KClass<*>, Int>()

    inline fun <reified T : Message> registerMessageType() {
        when (serializationType) {
            SerializationType.JSON -> {
                serdesMap[T::class] = SerDes(
                    serialize = { Json.encodeToString(it as T) },
                    deserialize = { Json.decodeFromString(it) as T }
                )
            }
            SerializationType.PROTOBUF -> {
                serdesMap[T::class] = SerDes(
                    serialize = { ProtoBuf.encodeToHexString(it as T) },
                    deserialize = { ProtoBuf.decodeFromHexString(it) as T }
                )
            }
        }
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