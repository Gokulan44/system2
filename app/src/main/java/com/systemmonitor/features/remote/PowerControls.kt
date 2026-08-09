package com.systemmonitor.features.remote

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NightsStay
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.domain.model.CommandType

@Composable
fun PowerControls(
    onCommandSelect: (CommandType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Quick Power Controls",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            PowerControlButton(
                title = "Lock",
                icon = Icons.Default.Lock,
                color = Color(0xFF3B82F6),
                onClick = { onCommandSelect(CommandType.LOCK) },
                modifier = Modifier.weight(1f)
            )
            PowerControlButton(
                title = "Sleep",
                icon = Icons.Default.NightsStay,
                color = Color(0xFF8B5CF6),
                onClick = { onCommandSelect(CommandType.SLEEP) },
                modifier = Modifier.weight(1f)
            )
            PowerControlButton(
                title = "Restart",
                icon = Icons.Default.RestartAlt,
                color = Color(0xFFF59E0B),
                onClick = { onCommandSelect(CommandType.RESTART) },
                modifier = Modifier.weight(1f)
            )
            PowerControlButton(
                title = "Shutdown",
                icon = Icons.Default.PowerSettingsNew,
                color = Color(0xFFEF4444),
                onClick = { onCommandSelect(CommandType.SHUTDOWN) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PowerControlButton(
    title: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(75.dp)
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
