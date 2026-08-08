package com.systemmonitor.monitoring

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.systemmonitor.domain.model.NetworkInfo
import com.systemmonitor.domain.model.TransportType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readCurrent(): NetworkInfo {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val capabilities = network?.let { cm.getNetworkCapabilities(it) }

        val transport = when {
            capabilities == null -> TransportType.NONE
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> TransportType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> TransportType.CELLULAR
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> TransportType.ETHERNET
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> TransportType.VPN
            else -> TransportType.NONE
        }

        val isConnected = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED) == true
        val isMetered = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)?.not() ?: false

        return NetworkInfo(
            timestamp = System.currentTimeMillis(),
            isConnected = isConnected,
            transportType = transport,
            isMetered = isMetered,
            downstreamKbps = capabilities?.linkDownstreamBandwidthKbps ?: 0,
            upstreamKbps = capabilities?.linkUpstreamBandwidthKbps ?: 0
        )
    }
}
