package com.systemmonitor.share

import android.content.Intent
import android.net.Uri
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareIntentParser @Inject constructor() {
    fun parseShareIntent(intent: Intent): List<Uri> {
        val action = intent.action
        return when (action) {
            Intent.ACTION_SEND -> {
                // For backward compatibility on Android 13+
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }
                if (uri != null) listOf(uri) else emptyList()
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java) ?: emptyList()
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM) ?: emptyList()
                }
            }
            else -> emptyList()
        }
    }
}
