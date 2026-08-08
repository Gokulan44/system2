package com.systemmonitor.features.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.domain.model.TransportType

@Composable
fun NetworkCard(
    modifier: Modifier = Modifier,
    viewModel: NetworkViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Network", style = MaterialTheme.typography.titleMedium)
            when (val current = state) {
                is NetworkUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                is NetworkUiState.Ready -> {
                    val network = current.network
                    Text(
                        text = if (network.isConnected) network.transportType.label() else "Disconnected",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    if (network.transportType == TransportType.WIFI) {
                        current.wifi?.let { wifi ->
                            Text(
                                text = wifi.ssid ?: "Connected (name unavailable)",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "Signal: ${wifi.signalBars}/4 · ${wifi.linkSpeedMbps} Mbps",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                    if (network.isMetered) {
                        Text(
                            text = "Metered connection",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun TransportType.label(): String = when (this) {
    TransportType.WIFI -> "Wi-Fi"
    TransportType.CELLULAR -> "Cellular"
    TransportType.ETHERNET -> "Ethernet"
    TransportType.VPN -> "VPN"
    TransportType.NONE -> "No connection"
}
