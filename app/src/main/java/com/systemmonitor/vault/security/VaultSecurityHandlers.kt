package com.systemmonitor.vault.security

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.WindowManager
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.importexport.FileHashManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScreenshotProtection @Inject constructor() {
    fun enableProtection(activity: Activity) {
        activity.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )
    }

    fun disableProtection(activity: Activity) {
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }
}

@Singleton
class ClipboardProtection @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun clearClipboard() {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("", "")
        clipboard.setPrimaryClip(clip)
    }
}

@Singleton
class ScreenLockManager @Inject constructor() {
    fun shouldLockOnBackground(): Boolean = true
}

@Singleton
class FileIntegrityManager @Inject constructor(
    private val fileDao: VaultFileDao,
    private val fileHashManager: FileHashManager
) {
    suspend fun verifyIntegrity(fileId: String): IntegrityResult {
        val entity = fileDao.getFileById(fileId)
            ?: return IntegrityResult.Corrupted("File not found in database")

        val file = File(entity.localPath)
        if (!file.exists()) {
            return IntegrityResult.Corrupted("Encrypted file missing from storage")
        }

        val expectedHash = entity.fileHash
        if (expectedHash.isNull_or_blank()) {
            return IntegrityResult.Valid("No stored hash to verify against")
        }

        val currentHash = fileHashManager.calculateSha256(file)
        return if (currentHash == expectedHash) {
            IntegrityResult.Valid("Checksum match confirmed")
        } else {
            IntegrityResult.Corrupted("SHA-256 checksum mismatch")
        }
    }

    private fun String?.isNull_or_blank(): Boolean = this == null || this.trim().isEmpty()

    sealed class IntegrityResult {
        data class Valid(val message: String) : IntegrityResult()
        data class Corrupted(val reason: String) : IntegrityResult()
    }
}

@Singleton
class VaultSecurityManager @Inject constructor(
    val screenshotProtection: ScreenshotProtection,
    val clipboardProtection: ClipboardProtection,
    val screenLockManager: ScreenLockManager,
    val fileIntegrityManager: FileIntegrityManager
)
