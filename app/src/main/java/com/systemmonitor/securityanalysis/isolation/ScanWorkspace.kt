package com.systemmonitor.securityanalysis.isolation

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScanWorkspace @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun getWorkspaceDir(): File {
        val dir = File(context.filesDir, "scan_workspace")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    fun prepareIsolatedCopy(sourceFile: File): File {
        val workspace = getWorkspaceDir()
        val destFile = File(workspace, "isolated_${System.currentTimeMillis()}_${sourceFile.name}")
        sourceFile.copyTo(destFile, overwrite = true)
        return destFile
    }

    fun clearWorkspace() {
        val workspace = getWorkspaceDir()
        workspace.listFiles()?.forEach { it.delete() }
    }
}
