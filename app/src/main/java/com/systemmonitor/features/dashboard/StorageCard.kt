package com.systemmonitor.features.dashboard

import androidx.compose.foundation.layout.Column
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
import kotlin.math.roundToInt

@Composable
fun StorageCard(
    modifier: Modifier = Modifier,
    viewModel: StorageViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Storage", style = MaterialTheme.typography.titleMedium)
            when (val current = state) {
                is StorageUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                is StorageUiState.Ready -> {
                    val storage = current.storage
                    val freeGb = (storage.freeGb * 10).roundToInt() / 10.0
                    val totalGb = (storage.totalGb * 10).roundToInt() / 10.0
                    Text(
                        text = "$freeGb GB free of $totalGb GB",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LinearProgressIndicator(
                        progress = { storage.usedPercent / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    if (storage.isLowStorage) {
                        Text(
                            text = "⚠ Storage nearly full",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}
