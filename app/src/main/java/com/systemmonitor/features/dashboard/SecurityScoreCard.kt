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

@Composable
fun SecurityScoreCard(
    modifier: Modifier = Modifier,
    viewModel: SecurityViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Security Score", style = MaterialTheme.typography.titleMedium)
            when (val current = state) {
                is SecurityUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                is SecurityUiState.Ready -> {
                    val result = current.result
                    Text(
                        text = "${result.score} / 100",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "${result.totalAppsScanned} apps scanned",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    if (result.sideloadedAppCount > 0) {
                        Text(
                            text = "${result.sideloadedAppCount} app(s) installed outside the Play Store",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    if (result.excessivePermissionAppCount > 0) {
                        Text(
                            text = "${result.excessivePermissionAppCount} app(s) request unusually many sensitive permissions",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
