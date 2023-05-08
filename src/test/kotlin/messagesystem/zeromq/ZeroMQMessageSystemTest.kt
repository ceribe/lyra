package messagesystem.zeromq

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

internal class ZeroMQMessageSystemTest {

    @Test
    fun `Should send and receive message`() {
        val allNodesAddresses = listOf(
            listOf(ZeroMQAddress("localhost", 8001), ZeroMQAddress("localhost", 8002)),
            listOf(ZeroMQAddress("localhost", 8003), ZeroMQAddress("localhost", 8004))
        )
        val node1 = ZeroMQMessageSystem(allNodesAddresses)
        val node2 = ZeroMQMessageSystem(allNodesAddresses)
        runBlocking {
            val job1 = thread(start = true) {
                node1.init(0)
                node1.sendTo("Hello World!", 1)
            }
            val job2 = thread(start = true) {
                node2.init(1)
                val message = node2.receive()
                assertEquals("Hello World!", message)
            }

            withContext(Dispatchers.IO) {
                job1.join()
                job2.join()
            }
        }
    }
}