package com.timo.timoterminal.service

import android.content.Context
import android.util.Log
import io.reactivex.disposables.Disposable
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.LifecycleEvent
import ua.naiksoftware.stomp.dto.StompMessage


class WebSocketService {
    private lateinit var client: StompClient
    private var subscriptions: HashMap<String, Disposable> = HashMap()
    private lateinit var clientListener: Disposable

    fun connectToWebSocket(url: String) {
        if (this::client.isInitialized) {
            Log.d("WEBSOCKEt", "connectToWebSocket: connection is already established! ")
        } else {
            client = Stomp.over(Stomp.ConnectionProvider.OKHTTP, url)
            client.connect()


            clientListener = client.lifecycle().subscribe { lifecycleEvent ->
                when (lifecycleEvent.getType()) {
                    LifecycleEvent.Type.OPENED -> Log.d("WEBSOCKET", "Stomp connection opened")
                    LifecycleEvent.Type.ERROR -> Log.e(
                        "WEBSOCKET",
                        "Error",
                        lifecycleEvent.getException()
                    )

                    LifecycleEvent.Type.CLOSED -> Log.d("WEBSOCKET", "Stomp connection closed")
                    else -> {
                        Log.d(
                            "WEBSOCKET",
                            "connectToWebSocket: something else happened .... please check!"
                        )
                    }
                }
            }
        }
    }

    fun connectToTopic(topicUrl: String, callback: (StompMessage) -> Unit) {
        if (subscriptions[topicUrl] != null) {
            if (subscriptions[topicUrl]!!.isDisposed) {
                val subscription = client.topic(topicUrl).subscribe { topicMessage ->
                    callback(topicMessage)
                }
                subscriptions[topicUrl] = subscription
            } else {
                Log.d("WEBSOCKET", "connectToWebSocket: subscription is already established!")
            }
        } else {
            val subscription = client.topic(topicUrl).subscribe { topicMessage ->
                callback(topicMessage)
            }
            subscriptions[topicUrl] = subscription
        }
    }

    fun sendToTopic(topicUrl: String, message: String) {
        //Message should be a JSON string!!
        client.send(topicUrl, message).subscribe()
    }

    fun unsubscribeFromTopic(topicUrl: String) {
        subscriptions[topicUrl]?.dispose()
    }

    fun closeConnection() {
        client.disconnect()
        clientListener.dispose()
    }

}