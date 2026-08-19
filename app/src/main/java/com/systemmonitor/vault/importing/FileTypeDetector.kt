package com.systemmonitor.vault.importing

import com.systemmonitor.vault.model.VaultFileType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileTypeDetector @Inject constructor() {
    fun detectFileType(fileName: String, mimeType: String): VaultFileType {
        return VaultFileType.fromMimeTypeAndName(mimeType, fileName)
    }
}
