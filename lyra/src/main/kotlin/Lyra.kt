import kotlin.reflect.KClass
import kotlinx.serialization.json.Json
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString

data class SerDes(val serialize: (Message) -> String, val deserialize: (String) -> Message)

class Lyra(private val messageSystem: MessageSystem = ZeroMQMessageSystem()) {
    val serdesMap = mutableMapOf<KClass<*>, SerDes>()
    val numberToClassType = mutableMapOf<Int, KClass<*>>()
    val classTypeToNumber = mutableMapOf<KClass<*>, Int>()

    inline fun <reified T : Message> registerMessageType() {
        serdesMap[T::class] = SerDes({ Json.encodeToString(it as T) }, { Json.decodeFromString(it) as T })
        val newClassNumber = numberToClassType.size
        numberToClassType[newClassNumber] = T::class
        classTypeToNumber[T::class] = newClassNumber
    }

    fun run() {
        while (true) {
            val message = messageSystem.receive()
            val typeNumber = message.substringBefore(':').toInt() // TODO
            val classType = numberToClassType[typeNumber] ?: continue
            val deserializer = serdesMap[classType]?.deserialize ?: continue
            val deserializedMessage = deserializer(message)
            deserializedMessage.react()
        }
    }

    fun send(message: Message) {
        val serializer = serdesMap[message::class]?.serialize ?: return
        messageSystem.send(serializer(message))
    }
}