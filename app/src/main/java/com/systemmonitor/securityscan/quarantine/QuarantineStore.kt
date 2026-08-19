package com.systemmonitor.securityscan.quarantine

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarantineStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val quarantineDir = File(context.filesDir, "quarantine").apply { mkdirs() }
    private val manifestFile = File(quarantineDir, "quarantine_manifest.json")

    @Synchronized
    fun getQuarantinedItems(): List<QuarantineMetadata> {
        if (!manifestFile.exists()) return emptyList()
        return try {
            val content = manifestFile.readText()
            val array = JSONArray(content)
            val list = mutableListOf<QuarantineMetadata>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    QuarantineMetadata(
                        id = obj.getString("id"),
                        appName = obj.getString("appName"),
                        packageName = if (obj.has("packageName") && !obj.isNull("packageName")) obj.getString("packageName") else null,
                        originalPath = obj.getString("originalPath"),
                        quarantineTime = obj.getLong("quarantineTime"),
                        reason = obj.getString("reason")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    @Synchronized
    fun addQuarantinedItem(item: QuarantineMetadata, sourceFile: File): Boolean {
        val targetFile = File(quarantineDir, "${item.id}.qbin")
        val success = xorCopy(sourceFile, targetFile)
        if (success) {
            val items = getQuarantinedItems().toMutableList()
            items.removeAll { it.id == item.id }
            items.add(item)
            saveItems(items)
        }
        return success
    }

    @Synchronized
    fun removeQuarantinedItem(itemId: String): QuarantineMetadata? {
        val items = getQuarantinedItems().toMutableList()
        val item = items.find { it.id == itemId } ?: return null
        items.remove(item)
        saveItems(items)
        
        val file = File(quarantineDir, "$itemId.qbin")
        if (file.exists()) {
            file.delete()
        }
        return item
    }

    @Synchronized
    fun restoreQuarantinedItem(itemId: String, destFile: File): Boolean {
        val qFile = File(quarantineDir, "$itemId.qbin")
        if (!qFile.exists()) return false
        val success = xorCopy(qFile, destFile)
        if (success) {
            removeQuarantinedItem(itemId)
        }
        return success
    }

    private fun saveItems(items: List<QuarantineMetadata>) {
        try {
            val array = JSONArray()
            for (item in items) {
                val obj = JSONObject().apply {
                    put("id", item.id)
                    put("appName", item.appName)
                    put("packageName", item.packageName)
                    put("originalPath", item.originalPath)
                    put("quarantineTime", item.quarantineTime)
                    put("reason", item.reason)
                }
                array.put(obj)
            }
            manifestFile.writeText(array.toString(2))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Copy file applying simple XOR 0x5A encryption.
     * This renders the file invalid as a ZIP/APK, isolating it safely.
     */
    private fun xorCopy(src: File, dest: File): Boolean {
        return try {
            val buffer = ByteArray(8192)
            FileInputStream(src).use { input ->
                FileOutputStream(dest).use { output ->
                    var read: Int
                    while (input.read(buffer).also { read = it } != -1) {
                        for (i in 0 until read) {
                            buffer[i] = (buffer[i].toInt() xor 0x5A).toByte()
                        }
                        output.write(buffer, 0, read)
                    }
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
