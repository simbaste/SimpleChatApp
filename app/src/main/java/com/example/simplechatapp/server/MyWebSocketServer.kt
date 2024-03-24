package com.example.simplechatapp.server

import android.util.Log
import org.java_websocket.WebSocket
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.lang.Exception
import java.net.InetSocketAddress

class MyWebSocketServer private constructor(
    address: InetSocketAddress,
    private val listener: MyWebSocketServer.Listener? = null
): WebSocketServer(address) {

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Log.d(TAG,"New connection from ${conn?.remoteSocketAddress}")
        conn?.remoteSocketAddress?.let {
            listener?.onNewConnection(it)
        }
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Log.d(TAG,"Connection closed by ${conn?.remoteSocketAddress}")
        conn?.remoteSocketAddress?.let {
            listener?.onConnectionClosed(it)
        }
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        message?.let {
            Log.d(TAG,"Received message from ${conn?.remoteSocketAddress}: $it")
            // You can parse the message JSON here if needed
            // For example, if message is JSON:
            // val parsedMessage = Json.decodeFromString<Message>(it)
        }
    }

    fun sendMessage(message: String) {
        Log.d(TAG,"sendMessage() called with: [$message] on [$connections]")
        connections?.forEach {
            Log.d(TAG,"sending $message to ${it.remoteSocketAddress}")
            it.send(message)
        }
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Log.d(TAG,"WebSocket error: $ex")
    }

    override fun onStart() {
        Log.d(TAG,"WebSocket server started on $address")
    }

    companion object {

        val TAG: String = MyWebSocketServer::class.java.simpleName

        // Start the WebSocket server
        fun startServer(ip: String, port: Int, listener: Listener? = null): MyWebSocketServer {
            val address = InetSocketAddress(ip, port)
            val server = MyWebSocketServer(address, listener)
            server.start()
            return server
        }
    }

    interface Listener {
        fun onNewConnection(address: InetSocketAddress)
        fun onConnectionClosed(address: InetSocketAddress)
    }

}