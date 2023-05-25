import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class MessageTest {
    companion object {
        var someCondition: Boolean = false
    }

    private val message = TestMessage("Hello World!")

    @kotlinx.serialization.Serializable
    data class TestMessage(val someString: String) : Message<NodeState>() {
        override suspend fun react() {
            waitFor { someCondition }
        }

        suspend fun activate() {
            channel.send(Unit)
            channel.send(Unit)
        }
    }

    @Test
    fun `Message should be processed after activation`() {
        someCondition = false
        runBlocking {

            // This launch method simulates the Lyra class
            launch {
                yield()
                someCondition = true
                message.activate()
            }

            assertFalse(someCondition)
            message.prepareAndReact { }
            assertTrue(someCondition)
        }
    }

    @Test
    fun `Message should be added to queue if condition is not met`() {
        someCondition = false
        runBlocking {

            // This launch method simulates the Lyra class
            launch {
                message.activate()
                yield()
                someCondition = true
                message.activate()
            }

            var wasMessageAddedToQueue = false
            message.prepareAndReact {
                if (it is MessageEvent.AddNewConditionEvent) {
                    wasMessageAddedToQueue = true
                }
            }
            assertTrue(wasMessageAddedToQueue)
        }
    }

    @Test
    fun `Message should be removed from queue after being processed`() {
        someCondition = true
        runBlocking {

            // This launch method simulates the Lyra class
            launch {
                message.activate()
            }

            var wasMessageRemovedFromQueue = false
            message.prepareAndReact {
                if (it is MessageEvent.RemoveMessageFromQueue) {
                    wasMessageRemovedFromQueue = true
                }
            }
            assertTrue(wasMessageRemovedFromQueue)
        }
    }
}