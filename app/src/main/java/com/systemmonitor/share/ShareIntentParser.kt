package com.systemmonitor.share

import android.content.Intent
import android.net.Uri
import android.os.Build
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShareIntentParser @Inject constructor() {
    fun parseShareIntent(intent: Intent): List<Uri> {
        val uris = mutableListOf<Uri>()

        // 1. Extract from ClipData (modern Android standard for single/multiple shares)
        intent.clipData?.let { clipData ->
            for (i in 0 until clipData.itemCount) {
                clipData.getItemAt(i).uri?.let { uris.add(it) }
            }
        }

        if (uris.isNotEmpty()) {
            return uris.filterNotNull().distinct()
        }

        // 2. Fallback to EXTRA_STREAM and data URI
        val action = intent.action
        when (action) {
            Intent.ACTION_SEND -> {
                val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)
                }
                if (uri != null) {
                    uris.add(uri)
                } else {
                    intent.data?.let { uris.add(it) }
                }
            }
            Intent.ACTION_SEND_MULTIPLE -> {
                val extraUris = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)
                }
                extraUris?.let { uris.addAll(it) }
            }
        }

        return uris.filterNotNull().distinct()
    }
}
