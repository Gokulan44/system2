package com.systemmonitor.vault.apk

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApkMetadataReader @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun readApkDetails(apkFile: File): ApkInfo? {
        return try {
            val pm = context.packageManager
            val info = pm.getPackageArchiveInfo(apkFile.absolutePath, 0) ?: return null
            info.applicationInfo?.sourceDir = apkFile.absolutePath
            info.applicationInfo?.publicSourceDir = apkFile.absolutePath

            val label = info.applicationInfo?.loadLabel(pm)?.toString() ?: info.packageName
            val version = info.versionName ?: "1.0"
            val drawable = info.applicationInfo?.loadIcon(pm)
            val bitmap = (drawable as? BitmapDrawable)?.bitmap

            ApkInfo(
                packageName = info.packageName,
                label = label,
                versionName = version,
                icon = bitmap
            )
        } catch (e: Exception) {
            null
        }
    }

    data class ApkInfo(
        val packageName: String,
        val label: String,
        val versionName: String,
        val icon: Bitmap?
    )
}

@Singleton
class ApkSecurityValidator @Inject constructor() {
    fun validateApkSafety(apkFile: File): Boolean {
        // Basic file check
        return apkFile.exists() && apkFile.length() > 0 && apkFile.name.endsWith(".apk", ignoreCase = true)
    }
}

@Singleton
class ApkThumbnailManager @Inject constructor(
    private val metadataReader: ApkMetadataReader
) {
    fun getApkIcon(apkFile: File): Bitmap? {
        return metadataReader.readApkDetails(apkFile)?.icon
    }
}

@Singleton
class ApkVaultManager @Inject constructor(
    val metadataReader: ApkMetadataReader,
    val securityValidator: ApkSecurityValidator,
    val thumbnailManager: ApkThumbnailManager
)
