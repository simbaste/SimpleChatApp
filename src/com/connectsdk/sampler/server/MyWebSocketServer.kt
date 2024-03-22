package com.connectsdk.sampler.server

import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class MyWebSocketServer private constructor(address: InetSocketAddress): WebSocketServer(address) {
    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        println("New connection from ${conn?.remoteSocketAddress}")
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        println("Connection closed by ${conn?.remoteSocketAddress}")
        conn?.send("WELCOME")
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        message?.let {
            println("Received message from ${conn?.remoteSocketAddress}: $it")
            // You can parse the message JSON here if needed
            // For example, if message is JSON:
            // val parsedMessage = Json.decodeFromString<Message>(it)
        }
    }

    fun sendMessage(message: String) {
        connections?.forEach {
            println("sending $message to ${it.remoteSocketAddress}")
            it.send(message)
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        println("WebSocket error: $ex")
    }

    override fun onStart() {
        println("WebSocket server started on $address")
    }

    companion object {
        // Start the WebSocket server
        fun startServer(): MyWebSocketServer {
            val address = InetSocketAddress("172.20.10.8", 8080) // Choose your desired IP address and port
            val server = MyWebSocketServer(address)
            server.start()
            return server
        }
    }

}