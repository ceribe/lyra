package messagesystem.socket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

internal class SocketMessageSystemTest {

    @Test
    fun `Should send and receive message`() {
        val allNodesAddresses = listOf(
            SocketAddress("localhost", 8005),
            SocketAddress("localhost", 8006)
        )
        val node1 = SocketMessageSystem(allNodesAddresses)
        val node2 = SocketMessageSystem(allNodesAddresses)
        runBlocking {
            val job1 = thread(start = true) {
                node1.init(0)
                Thread.sleep(100)
                node1.sendTo("Hello World!", 1)
            }
            val job2 = thread(start = true) {
                node2.init(1)
                Thread.sleep(100)
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