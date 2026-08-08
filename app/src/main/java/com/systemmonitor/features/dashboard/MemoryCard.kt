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

@Composable
fun MemoryCard(
    modifier: Modifier = Modifier,
    viewModel: MemoryViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()

    Card(modifier = modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Memory", style = MaterialTheme.typography.titleMedium)
            when (val current = state) {
                is MemoryUiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                is MemoryUiState.Ready -> {
                    val memory = current.memory
                    Text(
                        text = "${memory.usedMb} MB / ${memory.totalMb} MB",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LinearProgressIndicator(
                        progress = { memory.usedPercent / 100f },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    if (memory.isLowMemory) {
                        Text(
                            text = "⚠ System reports low memory",
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
