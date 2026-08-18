package com.systemmonitor.securityanalysis.isolation

import android.content.Context
import com.systemmonitor.securityanalysis.database.ScanDao
import com.systemmonitor.securityanalysis.database.QuarantineEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarantineManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scanDao: ScanDao
) {
    fun getQuarantineDir(): File {
        val dir = File(context.filesDir, "quarantine_vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    suspend fun quarantineFile(targetFile: File, sha256: String, reason: String): File? {
        return try {
            val vault = getQuarantineDir()
            val destFile = File(vault, "q_${sha256.take(8)}_${targetFile.name}.vault")
            targetFile.copyTo(destFile, overwrite = true)
            
            val originalPath = targetFile.absolutePath
            val originalName = targetFile.name
            
            targetFile.delete()
            
            val entity = QuarantineEntity(
                originalFileName = originalName,
                originalFilePath = originalPath,
                quarantineFilePath = destFile.absolutePath,
                sha256 = sha256,
                reason = reason,
                isRestored = false
            )
            scanDao.insertQuarantine(entity)
            
            destFile
        } catch (e: Exception) {
            null
        }
    }

    suspend fun restoreFile(id: Long, originalPath: String, quarantinePath: String): Boolean {
        return try {
            val qFile = File(quarantinePath)
            if (qFile.exists()) {
                val origFile = File(originalPath)
                origFile.parentFile?.mkdirs()
                qFile.copyTo(origFile, overwrite = true)
                qFile.delete()
                scanDao.markRestored(id)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    suspend fun deleteQuarantineRecord(id: Long, quarantinePath: String): Boolean {
        return try {
            val qFile = File(quarantinePath)
            if (qFile.exists()) {
                qFile.delete()
            }
            scanDao.deleteQuarantineRecord(id)
            true
        } catch (e: Exception) {
            false
        }
    }
}
