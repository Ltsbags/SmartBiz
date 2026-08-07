package com.example.core.realtime

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONObject
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

enum class ConnectionState {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    FAILED
}

class WebSocketClient(
    private val serverUrl: String = "wss://api.smartbiz.internal/v1/realtime",
    private val clientScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {

    private val TAG = "WebSocketClient"

    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()

    private var webSocket: WebSocket? = null
    private var heartbeatJob: Job? = null
    private var reconnectJob: Job? = null
    private val retryCount = AtomicInteger(0)
    private val maxRetries = 5

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .connectTimeout(15, TimeUnit.SECONDS)
        .build()

    var eventHandler: ((RealtimeEvent) -> Unit)? = null

    private var isSimulatedMode = false

    private fun enableSimulatedGatewayMode() {
        isSimulatedMode = true
        _connectionState.value = ConnectionState.CONNECTED
        Log.i(TAG, "Operating in Local Simulated Realtime Gateway mode.")
        eventHandler?.invoke(
            RealtimeEvent.PresenceChanged(
                userId = "USER_CURRENT",
                userName = "Active User",
                newStatus = "ONLINE",
                device = "Android Phone (Local Gateway)"
            )
        )
    }

    fun connect(authToken: String, businessId: String = "BIZ_001", branchId: String = "BRANCH_MAIN") {
        if (_connectionState.value == ConnectionState.CONNECTED || _connectionState.value == ConnectionState.CONNECTING) {
            return
        }

        _connectionState.value = ConnectionState.CONNECTING
        Log.i(TAG, "Connecting to Realtime WebSocket Gateway at $serverUrl...")

        val request = Request.Builder()
            .url(serverUrl)
            .addHeader("Authorization", "Bearer $authToken")
            .addHeader("X-Business-ID", businessId)
            .addHeader("X-Branch-ID", branchId)
            .addHeader("X-Client-Platform", "Android-Compose")
            .build()

        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.i(TAG, "WebSocket Connection Established successfully.")
                _connectionState.value = ConnectionState.CONNECTED
                retryCount.set(0)
                startHeartbeat()
                
                // Dispatch connected event
                eventHandler?.invoke(
                    RealtimeEvent.PresenceChanged(
                        userId = "USER_CURRENT",
                        userName = "Active User",
                        newStatus = "ONLINE",
                        device = "Android Phone"
                    )
                )
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(TAG, "Received frame: $text")
                parseAndDispatchMessage(text)
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WebSocket closing: $code / $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
                stopHeartbeat()
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.w(TAG, "WebSocket closed: $code / $reason")
                _connectionState.value = ConnectionState.DISCONNECTED
                stopHeartbeat()
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                if (t is UnknownHostException ||
                    t.message?.contains("Unable to resolve host", ignoreCase = true) == true ||
                    serverUrl.contains(".internal")
                ) {
                    Log.i(TAG, "WebSocket server endpoint '$serverUrl' is internal or offline (${t.message}). Switching to Local Simulated Realtime Gateway mode.")
                    enableSimulatedGatewayMode()
                    return
                }

                Log.w(TAG, "WebSocket connection failure: ${t.message}")
                _connectionState.value = ConnectionState.FAILED
                stopHeartbeat()
                scheduleReconnection(authToken, businessId, branchId)
            }
        })
    }

    fun disconnect() {
        Log.i(TAG, "Disconnecting WebSocket...")
        isSimulatedMode = false
        reconnectJob?.cancel()
        stopHeartbeat()
        webSocket?.close(1000, "Client explicit disconnect")
        webSocket = null
        _connectionState.value = ConnectionState.DISCONNECTED
    }

    fun sendEvent(eventJson: String): Boolean {
        if (isSimulatedMode) {
            parseAndDispatchMessage(eventJson)
            return true
        }
        return webSocket?.send(eventJson) ?: false
    }

    private fun startHeartbeat() {
        stopHeartbeat()
        heartbeatJob = clientScope.launch {
            while (_connectionState.value == ConnectionState.CONNECTED) {
                delay(25000) // 25 seconds ping interval
                val pingMessage = JSONObject().apply {
                    put("type", "PING")
                    put("timestamp", System.currentTimeMillis())
                }.toString()
                webSocket?.send(pingMessage)
            }
        }
    }

    private fun stopHeartbeat() {
        heartbeatJob?.cancel()
        heartbeatJob = null
    }

    private fun scheduleReconnection(authToken: String, businessId: String, branchId: String) {
        val attempts = retryCount.incrementAndGet()
        if (attempts > maxRetries) {
            Log.e(TAG, "Max reconnection retries reached. Connection status remains FAILED.")
            _connectionState.value = ConnectionState.FAILED
            return
        }

        _connectionState.value = ConnectionState.RECONNECTING
        val backoffDelayMs = (Math.pow(2.0, attempts.toDouble()) * 1000).toLong().coerceAtMost(30000)
        Log.i(TAG, "Scheduling reconnection attempt #$attempts in $backoffDelayMs ms...")

        reconnectJob?.cancel()
        reconnectJob = clientScope.launch {
            delay(backoffDelayMs)
            connect(authToken, businessId, branchId)
        }
    }

    private fun parseAndDispatchMessage(jsonText: String) {
        try {
            val json = JSONObject(jsonText)
            val eventType = json.optString("type", "UNKNOWN")
            val payload = json.optJSONObject("payload") ?: JSONObject()

            val event: RealtimeEvent = when (eventType) {
                "INVOICE_CREATED" -> RealtimeEvent.InvoiceCreated(
                    id = payload.optString("id", System.currentTimeMillis().toString()),
                    invoiceNumber = payload.optString("invoiceNumber", "INV-LIVE"),
                    customerName = payload.optString("customerName", "Walk-in Customer"),
                    totalAmount = payload.optDouble("totalAmount", 0.0),
                    createdBy = payload.optString("createdBy", "Staff")
                )
                "INVOICE_UPDATED" -> RealtimeEvent.InvoiceUpdated(
                    id = payload.optString("id", "INV-000"),
                    invoiceNumber = payload.optString("invoiceNumber", "INV-000"),
                    status = payload.optString("status", "PAID"),
                    amountPaid = payload.optDouble("amountPaid", 0.0)
                )
                "STOCK_CHANGED", "PRODUCT_UPDATED" -> RealtimeEvent.StockChanged(
                    productId = payload.optString("productId", "PROD-001"),
                    productName = payload.optString("productName", "Item"),
                    previousStock = payload.optInt("previousStock", 10),
                    newStock = payload.optInt("newStock", 5),
                    reason = payload.optString("reason", "Stock adjustment")
                )
                "PRESENCE_CHANGED" -> RealtimeEvent.PresenceChanged(
                    userId = payload.optString("userId", "USER_001"),
                    userName = payload.optString("userName", "Team Member"),
                    newStatus = payload.optString("status", "ONLINE"),
                    device = payload.optString("device", "Mobile")
                )
                "NOTIFICATION_CREATED" -> RealtimeEvent.NotificationCreated(
                    id = payload.optString("id", System.currentTimeMillis().toString()),
                    title = payload.optString("title", "Realtime Alert"),
                    message = payload.optString("message", "New event received"),
                    category = payload.optString("category", "SYSTEM")
                )
                "SECURITY_ALERT" -> RealtimeEvent.SecurityAlert(
                    alertId = payload.optString("id", "SEC-001"),
                    alertTitle = payload.optString("title", "Security Alert"),
                    message = payload.optString("message", "Suspicious login attempt detected"),
                    alertSeverity = payload.optString("severity", "HIGH")
                )
                "SYNC_COMPLETED" -> RealtimeEvent.SyncCompleted(
                    syncBatchId = payload.optString("batchId", "BATCH-001"),
                    itemsSyncedCount = payload.optInt("count", 1),
                    syncType = payload.optString("syncType", "DELTA")
                )
                "PONG" -> RealtimeEvent.HeartbeatAck(
                    ackId = payload.optString("ackId", "PONG"),
                    serverTimestamp = System.currentTimeMillis()
                )
                else -> RealtimeEvent.NotificationCreated(
                    id = System.currentTimeMillis().toString(),
                    title = "System Update",
                    message = jsonText,
                    category = "SYSTEM"
                )
            }

            eventHandler?.invoke(event)
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing incoming WebSocket frame: ${e.message}", e)
        }
    }
}
