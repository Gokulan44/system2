package com.systemmonitor.features.dashboard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.systemmonitor.domain.model.Battery

@Composable
fun BatteryCard(
    modifier: Modifier = Modifier,
    viewModel: BatteryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Battery", style = MaterialTheme.typography.titleMedium)

            when (val current = state) {
                is BatteryUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                is BatteryUiState.Unavailable -> Text(
                    text = "Battery data unavailable",
                    style = MaterialTheme.typography.bodyMedium
                )
                is BatteryUiState.Ready -> BatteryContent(current.battery)
            }
        }
    }
}

@Composable
private fun BatteryContent(battery: Battery) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
        Text(text = "${battery.levelPercent}%", style = MaterialTheme.typography.headlineMedium)
    }
    LinearProgressIndicator(
        progress = { battery.levelPercent / 100f },
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
    )
    Text(
        text = if (battery.isCharging) "Charging via ${battery.chargePlug}" else "Not charging",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(top = 4.dp)
    )
    Text(
        text = "${battery.temperatureCelsius}°C · ${battery.health}",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 2.dp)
    )
    if (battery.isCritical) {
        Text(
            text = "⚠ Critical battery level",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    } else if (battery.isLow) {
        Text(
            text = "Battery is low",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
