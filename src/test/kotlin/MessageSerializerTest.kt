import org.junit.jupiter.api.Test
import serialization.MessageSerializer
import serialization.SerializationType

internal class MessageSerializerTest {

    @kotlinx.serialization.Serializable
    data class TestMessage1(val someString: String) : Message<NodeState>() {
        override suspend fun react() {}
    }
    
    @kotlinx.serialization.Serializable
    data class TestMessage2(val someString: String, val someInt: Int, val someLong: Long) : Message<NodeState>() {
        override suspend fun react() {}
    }
    
    @Test
    fun `Should serialize and deserialize message`() {
        SerializationType.values().forEach {
            val messageSerializer = MessageSerializer(it)
            messageSerializer.registerMessageType<TestMessage1>()
            messageSerializer.registerMessageType<TestMessage2>()
            val testMessage1 = TestMessage1("Hello World!")
            testMessage1.sender = 1
            val testMessage2 = TestMessage2("Hello World!", 42, 42L)
            testMessage2.sender = 2
            val serializedMessage1 = messageSerializer.serializeMessageToString(testMessage1)
            val serializedMessage2 = messageSerializer.serializeMessageToString(testMessage2)
            val deserializedMessage1 =
                messageSerializer.deserializeMessageFromString(serializedMessage1!!) as TestMessage1
            val deserializedMessage2 =
                messageSerializer.deserializeMessageFromString(serializedMessage2!!) as TestMessage2
            assert(testMessage1 == deserializedMessage1)
            assert(testMessage2 == deserializedMessage2)
            assert(testMessage1.sender == deserializedMessage1.sender)
            assert(testMessage2.sender == deserializedMessage2.sender)
        }
    }
}