import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

class Lyra(private val messageSystem: MessageSystem = ZeroMQMessageSystem()) {
    val serializers = mutableMapOf<KClass<*>, (Message) -> String>()
    val deserializers = mutableMapOf<Int, (String) -> Message>()

    inline fun <reified T : Message> register() {
        val serializer = MessageSerializer<T>(serializers.size)
        serializers[T::class] = { serializer.serialize(it as T) }
        deserializers[serializer.typeNumber] = { serializer.deserialize(it) }
    }

    fun run() {
        while (true) {
            val message = messageSystem.receive()
            val typeNumber = message.substringBefore(':').toInt() // TODO
            val deserializer = deserializers[typeNumber] ?: continue
            val deserializedMessage = deserializer(message)
            deserializedMessage.react()
        }
    }

    fun send(message: Message) {
        val serializer = serializers[message::class] ?: return
        messageSystem.send(serializer(message))
    }
}