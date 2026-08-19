package com.systemmonitor.securityscan.quarantine

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.systemmonitor.securityscan.input.ScanTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarantineManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val quarantineStore: QuarantineStore
) {
    fun getQuarantinedApps(): List<QuarantineMetadata> {
        return quarantineStore.getQuarantinedItems()
    }

    fun quarantine(target: ScanTarget, reason: String): Boolean {
        val file = File(target.apkPath)
        if (file.exists()) {
            val metadata = QuarantineMetadata(
                id = System.currentTimeMillis().toString(),
                appName = target.appName,
                packageName = target.packageName,
                originalPath = file.absolutePath,
                quarantineTime = System.currentTimeMillis(),
                reason = reason
            )
            val success = quarantineStore.addQuarantinedItem(metadata, file)
            if (success) {
                // If it is an installed app, prompt for uninstall
                val isInstalled = context.packageManager.getInstalledPackages(0)
                    .any { it.packageName == target.packageName }
                if (isInstalled) {
                    val intent = Intent(Intent.ACTION_UNINSTALL_PACKAGE).apply {
                        data = Uri.parse("package:${target.packageName}")
                        putExtra(Intent.EXTRA_RETURN_RESULT, true)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    try {
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    file.delete()
                }
            }
            return success
        }
        return false
    }

    fun restore(itemId: String): Boolean {
        val items = quarantineStore.getQuarantinedItems()
        val item = items.find { it.id == itemId } ?: return false
        val destFile = File(item.originalPath)
        // Make parent directories if needed
        destFile.parentFile?.mkdirs()
        return quarantineStore.restoreQuarantinedItem(itemId, destFile)
    }

    fun deletePermanently(itemId: String): Boolean {
        val removed = quarantineStore.removeQuarantinedItem(itemId)
        return removed != null
    }
}
