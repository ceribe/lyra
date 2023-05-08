package messagesystem.websocket

import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.server.websocket.WebSockets
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking
import messagesystem.MessageSystem
import kotlinx.coroutines.channels.Channel

import kotlin.concurrent.thread

/**
 * MessageSystem implementation using websockets.
 * @param allSocketAddresses list of addresses of all nodes in the network. Eg the first list is the address of the first node, the second list is the address of the second node, etc.
 */
class WebsocketMessageSystem(private val allSocketAddresses: List<WebsocketAddress>) : MessageSystem {
    private val messagesToReceive = Channel<String>(capacity = Channel.UNLIMITED)

    override fun init(nodeNumber: Int): Int {
        val address = allSocketAddresses[nodeNumber]

        thread(start = true) {
            runBlocking {
                embeddedServer(Netty, port = address.port) {
                    install(WebSockets)
                    routing {
                        webSocket("/") {
                            for (frame in incoming) {
                                if (frame is Frame.Text) {
                                    messagesToReceive.send(frame.readText())
                                }
                            }
                        }
                    }
                }.start(wait = true)
            }
        }

        return allSocketAddresses.size
    }

    override fun sendTo(message: String, recipient: Int) {
        val recipientSocketAddress = allSocketAddresses[recipient]
        val client = HttpClient {
            install(io.ktor.client.plugins.websocket.WebSockets)
        }

        runBlocking {
            client.webSocket(
                method = HttpMethod.Get,
                host = recipientSocketAddress.ipAddress,
                port = recipientSocketAddress.port,
                path = "/"
            ) {
                outgoing.send(Frame.Text(message))
            }
        }
        client.close()
    }

    override fun sendToAll(message: String) {
        for (i in allSocketAddresses.indices) {
            sendTo(message, i)
        }
    }

    override fun receive(): String {
        return runBlocking {
            messagesToReceive.receive()
        }
    }
}