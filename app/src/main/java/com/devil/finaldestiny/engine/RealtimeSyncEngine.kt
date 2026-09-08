package com.devil.finaldestiny.engine

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.json.JSONObject
import java.net.URI
import java.net.http.HttpClient
import java.net.http.WebSocket
import java.nio.ByteBuffer
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

data class RealtimeChatMessage(
    val id: String,
    val senderId: String,
    val senderName: String,
    val recipientId: String,
    val text: String,
    val timestamp: Long
)

data class RealtimeGiftEvent(
    val giftId: String,
    val giftName: String,
    val giftIcon: String,
    val senderName: String,
    val recipientName: String,
    val coinValue: Int,
    val roomId: String
)

data class RealtimeMatchEvent(
    val matchId: String,
    val matchedUserId: String,
    val matchedUserName: String,
    val matchedUserAvatar: String,
    val timestamp: Long
)

class RealtimeSyncEngine(
    private val serverWsUrl: String = "wss://twwezpogwtmjavoemdvi.supabase.co/realtime/v1/websocket",
    private val apiKey: String = "sb_publishable_RiDqsSCPGbWGxd6570P1FA_y_L10U_w",
    private val userId: String = "u101"
) {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var webSocket: WebSocket? = null
    
    private val _isConnected = MutableStateFlow(false)
    val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    private val _unreadMessageCount = MutableStateFlow(0)
    val unreadMessageCount: StateFlow<Int> = _unreadMessageCount.asStateFlow()

    // Event callbacks
    var onDirectMessageReceived: ((RealtimeChatMessage) -> Unit)? = null
    var onGiftDelivered: ((RealtimeGiftEvent) -> Unit)? = null
    var onSwipeMatchEvent: ((RealtimeMatchEvent) -> Unit)? = null

    // Exponential backoff configuration
    private var reconnectAttempt = 0
    private val initialDelayMs = 1000L
    private val maxDelayMs = 30000L

    fun connect() {
        if (_isConnected.value) return
        scope.launch {
            establishWebSocketConnection()
        }
    }

    private suspend fun establishWebSocketConnection() {
        try {
            val client = HttpClient.newHttpClient()
            val wsUri = URI.create("$serverWsUrl?apikey=$apiKey&vsn=1.0.0&user_id=$userId")

            val listener = object : WebSocket.Listener {
                override fun onOpen(webSocket: WebSocket) {
                    _isConnected.value = true
                    reconnectAttempt = 0
                    webSocket.request(1)

                    // Subscribe to Supabase Realtime publication tables ('messages', 'calls', 'profiles')
                    subscribeToSupabaseTable(webSocket, "messages", "1")
                    subscribeToSupabaseTable(webSocket, "calls", "2")
                    subscribeToSupabaseTable(webSocket, "profiles", "3")
                }

                override fun onText(webSocket: WebSocket, data: CharSequence, last: Boolean): CompletionStage<*>? {
                    handleIncomingJson(data.toString())
                    webSocket.request(1)
                    return CompletableFuture.completedFuture(null)
                }

                override fun onClose(webSocket: WebSocket, statusCode: Int, reason: String): CompletionStage<*>? {
                    _isConnected.value = false
                    scheduleExponentialReconnect()
                    return CompletableFuture.completedFuture(null)
                }

                override fun onError(webSocket: WebSocket, error: Throwable) {
                    _isConnected.value = false
                    scheduleExponentialReconnect()
                }
            }

            webSocket = client.newWebSocketBuilder()
                .header("apikey", apiKey)
                .header("Authorization", "Bearer $apiKey")
                .buildAsync(wsUri, listener)
                .join()

        } catch (e: Exception) {
            _isConnected.value = false
            scheduleExponentialReconnect()
        }
    }

    private fun subscribeToSupabaseTable(ws: WebSocket, tableName: String, refId: String) {
        try {
            val joinPayload = JSONObject().apply {
                put("topic", "realtime:public:$tableName")
                put("event", "phx_join")
                put("payload", JSONObject().apply {
                    put("config", JSONObject().apply {
                        put("postgres_changes", org.json.JSONArray().apply {
                            put(JSONObject().apply {
                                put("event", "*")
                                put("schema", "public")
                                put("table", tableName)
                            })
                        })
                    })
                })
                put("ref", refId)
            }
            ws.sendText(joinPayload.toString(), true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun scheduleExponentialReconnect() {
        scope.launch {
            reconnectAttempt++
            val delayMs = (initialDelayMs * Math.pow(2.0, (reconnectAttempt - 1).toDouble()))
                .toLong()
                .coerceAtMost(maxDelayMs)

            delay(delayMs)
            if (!_isConnected.value) {
                establishWebSocketConnection()
            }
        }
    }

    private fun handleIncomingJson(jsonStr: String) {
        try {
            val json = JSONObject(jsonStr)
            when (json.optString("event")) {
                "direct_message" -> {
                    val msgObj = json.getJSONObject("payload")
                    val message = RealtimeChatMessage(
                        id = msgObj.optString("id"),
                        senderId = msgObj.optString("sender_id"),
                        senderName = msgObj.optString("sender_name"),
                        recipientId = msgObj.optString("recipient_id"),
                        text = msgObj.optString("text"),
                        timestamp = msgObj.optLong("timestamp", System.currentTimeMillis())
                    )
                    _unreadMessageCount.value += 1
                    onDirectMessageReceived?.invoke(message)
                }
                "gift_delivery" -> {
                    val giftObj = json.getJSONObject("payload")
                    val giftEvent = RealtimeGiftEvent(
                        giftId = giftObj.optString("gift_id"),
                        giftName = giftObj.optString("gift_name"),
                        giftIcon = giftObj.optString("gift_icon", "🎁"),
                        senderName = giftObj.optString("sender_name"),
                        recipientName = giftObj.optString("recipient_name"),
                        coinValue = giftObj.optInt("coin_value", 100),
                        roomId = giftObj.optString("room_id")
                    )
                    onGiftDelivered?.invoke(giftEvent)
                }
                "swipe_match" -> {
                    val matchObj = json.getJSONObject("payload")
                    val matchEvent = RealtimeMatchEvent(
                        matchId = matchObj.optString("match_id"),
                        matchedUserId = matchObj.optString("matched_user_id"),
                        matchedUserName = matchObj.optString("matched_user_name"),
                        matchedUserAvatar = matchObj.optString("matched_user_avatar"),
                        timestamp = matchObj.optLong("timestamp", System.currentTimeMillis())
                    )
                    onSwipeMatchEvent?.invoke(matchEvent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendDirectMessage(recipientId: String, text: String) {
        val payload = JSONObject().apply {
            put("event", "direct_message")
            put("payload", JSONObject().apply {
                put("id", "msg_${System.currentTimeMillis()}")
                put("sender_id", userId)
                put("recipient_id", recipientId)
                put("text", text)
                put("timestamp", System.currentTimeMillis())
            })
        }
        webSocket?.sendText(payload.toString(), true)
    }

    fun sendVirtualGift(roomId: String, giftId: String, giftName: String, coinValue: Int, recipientName: String) {
        val payload = JSONObject().apply {
            put("event", "gift_delivery")
            put("payload", JSONObject().apply {
                put("gift_id", giftId)
                put("gift_name", giftName)
                put("coin_value", coinValue)
                put("recipient_name", recipientName)
                put("room_id", roomId)
            })
        }
        webSocket?.sendText(payload.toString(), true)
    }

    fun resetUnreadCounter() {
        _unreadMessageCount.value = 0
    }

    fun disconnect() {
        webSocket?.sendClose(1000, "Client disconnect")
        _isConnected.value = false
        scope.cancel()
    }
}
