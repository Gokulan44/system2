package com.systemmonitor.vault.importing

import android.net.Uri
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.storage.VaultStorageManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileImportManager @Inject constructor(
    private val vaultStorageManager: VaultStorageManager
) {
    suspend fun importFile(
        uri: Uri,
        name: String,
        mimeType: String,
        parentId: String? = null,
        fileHash: String? = null
    ): Result<VaultFileEntity> {
        return vaultStorageManager.importFile(
            uri = uri,
            fileName = name,
            mimeType = mimeType,
            parentId = parentId,
            fileHash = fileHash
        )
    }
}
