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
            WebsocketAddress("localhost", 8009),
            WebsocketAddress("localhost", 8010)
        )
        val node1 = WebsocketMessageSystem(allNodesAddresses)
        val node2 = WebsocketMessageSystem(allNodesAddresses)
        runBlocking {
            val job1 = thread(start = true) {
                node1.init(0)
                Thread.sleep(500)
                node1.sendTo("Hello World!", 1)
            }
            val job2 = thread(start = true) {
                node2.init(1)
                Thread.sleep(500)
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