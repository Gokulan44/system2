package com.systemmonitor.vault.storage

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultPathManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val encryptedDir: File = File(context.filesDir, "vault/encrypted").apply { mkdirs() }
    val tempDir: File = File(context.cacheDir, "vault/temp").apply { mkdirs() }
    val backupDir: File = File(context.filesDir, "vault/backups").apply { mkdirs() }
    val trashDir: File = File(context.filesDir, "vault/trash").apply { mkdirs() }

    fun clearTempDir() {
        try {
            tempDir.listFiles()?.forEach { it.deleteRecursively() }
        } catch (e: Exception) {
            // Ignore failure
        }
    }
}
