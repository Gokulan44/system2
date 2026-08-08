package com.systemmonitor.securityanalysis.isolation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuarantineManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getQuarantineDir(): File {
        val dir = File(context.filesDir, "quarantine_vault")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun quarantineFile(targetFile: File, sha256: String): File? {
        return try {
            val vault = getQuarantineDir()
            val destFile = File(vault, "q_${sha256.take(8)}_${targetFile.name}.vault")
            targetFile.copyTo(destFile, overwrite = true)
            targetFile.delete()
            destFile
        } catch (e: Exception) {
            null
        }
    }
}
