package cu.todus.app.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

enum class NetworkQuality { POOR, FAIR, GOOD, EXCELLENT }
enum class NetworkKind { WIFI, MOBILE, UNKNOWN }

data class NetworkState(
    val kind: NetworkKind = NetworkKind.UNKNOWN,
    val quality: NetworkQuality = NetworkQuality.POOR,
    val isAvailable: Boolean = false
)

class NetworkMonitor(context: Context) {
    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private val _state = MutableStateFlow(NetworkState())
    val state: StateFlow<NetworkState> = _state.asStateFlow()
    
    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) { updateState(network) }
        override fun onLost(network: Network) { _state.value = NetworkState() }
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) { updateState(network) }
    }
    
    init {
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        connectivityManager.registerNetworkCallback(request, callback)
        updateState(connectivityManager.activeNetwork)
    }
    
    private fun updateState(network: Network?) {
        if (network == null) { _state.value = NetworkState(); return }
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return
        val kind = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkKind.WIFI
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkKind.MOBILE
            else -> NetworkKind.UNKNOWN
        }
        val kbps = caps.linkDownstreamBandwidthKbps
        val quality = when {
            kbps >= 50000 -> NetworkQuality.EXCELLENT
            kbps >= 5000 -> NetworkQuality.GOOD
            kbps >= 100 -> NetworkQuality.FAIR
            else -> NetworkQuality.POOR
        }
        _state.value = NetworkState(kind = kind, quality = quality, isAvailable = true)
    }
    
    fun stop() { connectivityManager.unregisterNetworkCallback(callback) }
}
