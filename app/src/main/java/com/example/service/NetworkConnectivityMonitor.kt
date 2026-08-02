package com.example.service

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NetworkStateInfo(
    val isConnected: Boolean,
    val connectionType: String, // "Wi-Fi", "Cellular 4G/5G", "Cellular 2G/3G", "Offline"
    val isCellular: Boolean,
    val isWifi: Boolean,
    val isLowBandwidth: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Android System Network Connectivity Monitor using ConnectivityManager NetworkCallbacks.
 * Continuously tracks network availability across Wi-Fi and Cellular interfaces,
 * emitting real-time state updates to notify co-op staff when the device transitions
 * into Offline Mode so data is securely queued locally in Room DB.
 */
class NetworkConnectivityMonitor private constructor(context: Context) {

    private val applicationContext = context.applicationContext
    private val connectivityManager =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager

    private val _networkState = MutableStateFlow(checkInitialNetworkState())
    val networkState: StateFlow<NetworkStateInfo> = _networkState.asStateFlow()

    private val _isOnline = MutableStateFlow(_networkState.value.isConnected)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    init {
        startMonitoring()
    }

    private fun checkInitialNetworkState(): NetworkStateInfo {
        val cm = connectivityManager ?: return NetworkStateInfo(
            isConnected = false,
            connectionType = "Offline",
            isCellular = false,
            isWifi = false,
            isLowBandwidth = true
        )

        val activeNetwork = cm.activeNetwork ?: return NetworkStateInfo(
            isConnected = false,
            connectionType = "Offline",
            isCellular = false,
            isWifi = false,
            isLowBandwidth = true
        )

        val capabilities = cm.getNetworkCapabilities(activeNetwork) ?: return NetworkStateInfo(
            isConnected = false,
            connectionType = "Offline",
            isCellular = false,
            isWifi = false,
            isLowBandwidth = true
        )

        val hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isWifi = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        val typeText = when {
            isWifi -> "Wi-Fi Network"
            isCellular -> "Cellular Data"
            else -> "Ethernet/Other"
        }

        return NetworkStateInfo(
            isConnected = hasInternet,
            connectionType = if (hasInternet) typeText else "Offline",
            isCellular = isCellular,
            isWifi = isWifi,
            isLowBandwidth = isCellular
        )
    }

    fun startMonitoring() {
        if (networkCallback != null || connectivityManager == null) return

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
            .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
            .build()

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                Log.i(TAG, "Network available: $network")
                updateCapabilities(network)
            }

            override fun onLost(network: Network) {
                Log.w(TAG, "Network lost: $network")
                updateState(
                    NetworkStateInfo(
                        isConnected = false,
                        connectionType = "Offline",
                        isCellular = false,
                        isWifi = false,
                        isLowBandwidth = true
                    )
                )
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                updateCapabilities(network, networkCapabilities)
            }
        }

        try {
            connectivityManager.registerNetworkCallback(request, networkCallback!!)
            Log.i(TAG, "NetworkConnectivityMonitor callback registered successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to register network callback", e)
        }
    }

    private fun updateCapabilities(network: Network, capabilitiesParam: NetworkCapabilities? = null) {
        val cm = connectivityManager ?: return
        val caps = capabilitiesParam ?: cm.getNetworkCapabilities(network) ?: return

        val isInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isCellular = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)

        val connType = when {
            isWifi -> "Wi-Fi Signal"
            isCellular -> "Cellular Data"
            else -> "Ethernet"
        }

        updateState(
            NetworkStateInfo(
                isConnected = isInternet,
                connectionType = if (isInternet) connType else "Offline",
                isCellular = isCellular,
                isWifi = isWifi,
                isLowBandwidth = isCellular
            )
        )
    }

    private fun updateState(state: NetworkStateInfo) {
        _networkState.value = state
        _isOnline.value = state.isConnected
    }

    /**
     * Manual simulator override for testing signal loss in rural areas.
     */
    fun toggleOfflineSimulation(forceOffline: Boolean) {
        if (forceOffline) {
            updateState(
                NetworkStateInfo(
                    isConnected = false,
                    connectionType = "Offline (Simulated Signal Loss)",
                    isCellular = false,
                    isWifi = false,
                    isLowBandwidth = true
                )
            )
        } else {
            updateState(checkInitialNetworkState())
        }
    }

    fun stopMonitoring() {
        networkCallback?.let {
            try {
                connectivityManager?.unregisterNetworkCallback(it)
            } catch (e: Exception) {
                Log.e(TAG, "Error unregistering network callback", e)
            }
        }
        networkCallback = null
    }

    companion object {
        private const val TAG = "NetworkConnectivityMonitor"

        @Volatile
        private var INSTANCE: NetworkConnectivityMonitor? = null

        fun getInstance(context: Context): NetworkConnectivityMonitor {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: NetworkConnectivityMonitor(context).also { INSTANCE = it }
            }
        }
    }
}
