package com.systemmonitor.share

import android.net.Uri

data class SharedFile(
    val uri: Uri,
    val name: String,
    val mimeType: String,
    val size: Long
)
