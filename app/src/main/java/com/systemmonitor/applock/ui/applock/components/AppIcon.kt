package com.systemmonitor.applock.ui.applock.components

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap

@Composable
fun AppIcon(
    packageName: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val pm = context.packageManager
    
    val iconDrawable = remember(packageName) {
        try {
            pm.getApplicationIcon(packageName)
        } catch (e: Exception) {
            pm.defaultActivityIcon
        }
    }
    
    val bitmap = remember(iconDrawable) {
        iconDrawable.toBitmap().asImageBitmap()
    }
    
    Image(
        bitmap = bitmap,
        contentDescription = "Application Logo",
        modifier = modifier
    )
}
