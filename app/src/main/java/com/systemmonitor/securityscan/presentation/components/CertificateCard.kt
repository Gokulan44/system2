package com.systemmonitor.securityscan.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.systemmonitor.securityscan.static.CertificateInfo

@Composable
fun CertificateCard(
    info: CertificateInfo?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.8f))
            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Signing Certificate Details",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        if (info == null) {
            Text(
                text = "Could not parse or extract application certificate details. Signatures might be corrupted.",
                color = Color(0xFFEF4444),
                fontSize = 12.sp
            )
        } else {
            val detailsMap = listOf(
                "Subject" to info.subject.substringAfter("CN=").substringBefore(","),
                "Issuer" to info.issuer.substringAfter("CN=").substringBefore(","),
                "Algorithm" to info.sigAlgName,
                "Serial No" to info.serialNumber,
                "Expires" to info.validTo
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                detailsMap.forEach { (label, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = label,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = value.take(28),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
