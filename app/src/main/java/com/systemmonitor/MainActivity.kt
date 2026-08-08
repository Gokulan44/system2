package com.systemmonitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.systemmonitor.features.dashboard.BatteryCard
import com.systemmonitor.features.dashboard.MemoryCard
import com.systemmonitor.features.dashboard.NetworkCard
import com.systemmonitor.features.dashboard.SecurityScoreCard
import com.systemmonitor.features.dashboard.StorageCard
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    // Placeholder dashboard — swap for DashboardScreen +
                    // NavHost once auth and navigation land.
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        BatteryCard()
                        MemoryCard()
                        StorageCard()
                        NetworkCard()
                        SecurityScoreCard()
                    }
                }
            }
        }
    }
}
