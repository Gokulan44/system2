package com.systemmonitor.securityscan.input

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream

@Composable
fun rememberApkPicker(onApkPicked: (File) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val tempFile = File(context.cacheDir, "picked_scan_${System.currentTimeMillis()}.apk")
                contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                if (tempFile.exists()) {
                    onApkPicked(tempFile)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    return {
        launcher.launch("application/vnd.android.package-archive")
    }
}
