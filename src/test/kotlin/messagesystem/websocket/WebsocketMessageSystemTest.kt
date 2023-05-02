package messagesystem.websocket

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import kotlin.concurrent.thread

internal class WebsocketMessageSystemTest {

    @Test
    fun `Should send and receive message`() {
        val allNodesAddresses = listOf(
            WebsocketAddress("localhost", 8005),
            WebsocketAddress("localhost", 8006)
        )
        val node1 = WebsocketMessageSystem(allNodesAddresses)
        val node2 = WebsocketMessageSystem(allNodesAddresses)
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