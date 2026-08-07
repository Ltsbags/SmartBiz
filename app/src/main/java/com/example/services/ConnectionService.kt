package com.example.services

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import com.example.core.realtime.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class NetworkType {
    NONE,
    WIFI,
    CELLULAR,
    ETHERNET
}

data class ConnectionHealth(
    val networkType: NetworkType = NetworkType.WIFI,
    val isInternetAvailable: Boolean = true,
    val realtimeState: ConnectionState = ConnectionState.CONNECTED,
    val latencyMs: Long = 18,
    val lastCheckedTimestamp: Long = System.currentTimeMillis()
)

class ConnectionService(
    private val context: Context,
    private val realtimeService: RealtimeService? = null,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val TAG = "ConnectionService"
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _connectionHealth = MutableStateFlow(ConnectionHealth())
    val connectionHealth: StateFlow<ConnectionHealth> = _connectionHealth.asStateFlow()

    init {
        registerNetworkCallback()
        if (realtimeService != null) {
            scope.launch {
                realtimeService.connectionState.collect { state ->
                    _connectionHealth.value = _connectionHealth.value.copy(
                        realtimeState = state,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    )
                }
            }
        }
    }

    private fun registerNetworkCallback() {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    val caps = connectivityManager.getNetworkCapabilities(network)
                    val type = when {
                        caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> NetworkType.WIFI
                        caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> NetworkType.CELLULAR
                        caps?.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) == true -> NetworkType.ETHERNET
                        else -> NetworkType.WIFI
                    }

                    Log.i(TAG, "Network restored: $type. Re-establishing realtime channel...")
                    _connectionHealth.value = _connectionHealth.value.copy(
                        networkType = type,
                        isInternetAvailable = true,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    )

                    // Auto reconnect WebSocket if requested
                    realtimeService?.startRealtimeSession()
                }

                override fun onLost(network: Network) {
                    Log.w(TAG, "Network lost. Switching to Offline Mode.")
                    _connectionHealth.value = _connectionHealth.value.copy(
                        networkType = NetworkType.NONE,
                        isInternetAvailable = false,
                        realtimeState = ConnectionState.DISCONNECTED,
                        lastCheckedTimestamp = System.currentTimeMillis()
                    )
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback: ${e.message}")
        }
    }

    fun triggerManualReconnect() {
        Log.i(TAG, "Manual reconnection requested by operator.")
        realtimeService?.startRealtimeSession()
    }
}
