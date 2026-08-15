package com.systemmonitor.vault.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.database.VaultFolderDao
import com.systemmonitor.vault.database.VaultFolderEntity
import com.systemmonitor.vault.database.VaultAuditDao
import com.systemmonitor.vault.database.VaultAuditEntity
import com.systemmonitor.vault.model.VaultFile
import com.systemmonitor.vault.model.VaultFolder
import com.systemmonitor.vault.storage.VaultStorageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import java.util.UUID
import javax.inject.Inject

data class VaultUiState(
    val isLocked: Boolean = true,
    val isSetup: Boolean = false,
    val currentFolderId: String? = null,
    val folders: List<VaultFolder> = emptyList(),
    val files: List<VaultFile> = emptyList(),
    val breadcrumbs: List<VaultFolder> = emptyList(),
    val isImporting: Boolean = false,
    val viewingFile: VaultFile? = null,
    val tempViewingFile: File? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val folderDao: VaultFolderDao,
    private val fileDao: VaultFileDao,
    private val storageManager: VaultStorageManager,
    private val auditDao: VaultAuditDao
) : ViewModel() {

    private val prefs = context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)
    
    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val currentFolderIdFlow = MutableStateFlow<String?>(null)

    val auditLogs: StateFlow<List<VaultAuditEntity>> = auditDao.getAllAudits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkSetupStatus()
        observeCurrentFolderContents()
    }

    private fun checkSetupStatus() {
        val pinHash = prefs.getString("vault_pin_hash", null)
        _uiState.update { it.copy(isSetup = pinHash != null) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCurrentFolderContents() {
        viewModelScope.launch {
            currentFolderIdFlow.flatMapLatest { folderId ->
                combine(
                    folderDao.getFoldersInFolder(folderId),
                    fileDao.getFilesInFolder(folderId)
                ) { folderEntities, fileEntities ->
                    val folders = folderEntities.map { VaultFolder(it.id, it.name, it.parentId, it.createdAt) }
                    val files = fileEntities.map { VaultFile(it.id, it.name, it.localPath, it.mimeType, it.sizeBytes, it.parentId, it.createdAt) }
                    
                    // Fetch breadcrumbs
                    val breadcrumbs = mutableListOf<VaultFolder>()
                    var tempId = folderId
                    while (tempId != null) {
                        val parent = folderDao.getFolderById(tempId)
                        if (parent != null) {
                            breadcrumbs.add(0, VaultFolder(parent.id, parent.name, parent.parentId, parent.createdAt))
                            tempId = parent.parentId
                        } else {
                            tempId = null
                        }
                    }
                    Triple(folders, files, breadcrumbs)
                }
            }.collect { (folders, files, breadcrumbs) ->
                _uiState.update {
                    it.copy(
                        folders = folders,
                        files = files,
                        breadcrumbs = breadcrumbs,
                        currentFolderId = currentFolderIdFlow.value
                    )
                }
            }
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("") { str, it -> str + "%02x".format(it) }
    }

    private fun logEvent(action: String, details: String) {
        viewModelScope.launch {
            val audit = VaultAuditEntity(
                id = UUID.randomUUID().toString(),
                action = action,
                details = details,
                timestamp = System.currentTimeMillis()
            )
            auditDao.insertAudit(audit)
        }
    }

    fun setupVault(pin: String): Boolean {
        if (pin.length < 4) return false
        val hash = hashPin(pin)
        prefs.edit().putString("vault_pin_hash", hash).apply()
        _uiState.update { it.copy(isSetup = true, isLocked = false) }
        logEvent("SETUP", "Secure Vault initialized and PIN created.")
        return true
    }

    fun unlockVault(pin: String): Boolean {
        val savedHash = prefs.getString("vault_pin_hash", null) ?: return false
        val hash = hashPin(pin)
        val matches = hash == savedHash
        if (matches) {
            _uiState.update { it.copy(isLocked = false) }
            logEvent("UNLOCK", "Vault unlocked successfully.")
        } else {
            logEvent("UNLOCK_FAILED", "Failed unlock attempt with incorrect PIN.")
        }
        return matches
    }

    fun lockVault() {
        val wasLocked = _uiState.value.isLocked
        _uiState.update { it.copy(isLocked = true, currentFolderId = null) }
        currentFolderIdFlow.value = null
        if (!wasLocked) {
            logEvent("LOCK", "Vault locked manually or on app backgrounded.")
        }
    }

    fun resetVault() {
        viewModelScope.launch {
            prefs.edit().clear().apply()
            auditDao.clearAllAudits()
            _uiState.update { VaultUiState(isLocked = true, isSetup = false) }
        }
    }

    fun navigateToFolder(folderId: String?) {
        currentFolderIdFlow.value = folderId
    }

    fun navigateUp() {
        val currentId = currentFolderIdFlow.value ?: return
        viewModelScope.launch {
            val folder = folderDao.getFolderById(currentId)
            currentFolderIdFlow.value = folder?.parentId
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            val folder = VaultFolderEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                parentId = currentFolderIdFlow.value,
                createdAt = System.currentTimeMillis()
            )
            folderDao.insertFolder(folder)
            logEvent("FOLDER_CREATE", "Created folder: '$name'")
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            val oldFolder = folderDao.getFolderById(folderId)
            folderDao.renameFolder(folderId, newName)
            logEvent("FOLDER_RENAME", "Renamed folder from '${oldFolder?.name}' to '$newName'")
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            val folder = folderDao.getFolderById(folderId)
            folderDao.deleteFolderById(folderId)
            fileDao.deleteFilesByParentId(folderId)
            logEvent("FOLDER_DELETE", "Deleted folder and contents: '${folder?.name}'")
        }
    }

    fun importFile(uri: Uri, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true) }
            val result = storageManager.importFile(
                uri = uri,
                fileName = fileName,
                mimeType = mimeType,
                parentId = currentFolderIdFlow.value
            )
            if (result.isSuccess) {
                _uiState.update { it.copy(isImporting = false) }
                logEvent("FILE_IMPORT", "Imported and encrypted file: '$fileName'")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Unknown error"
                _uiState.update {
                    it.copy(
                        isImporting = false,
                        errorMessage = err
                    )
                }
                logEvent("FILE_IMPORT_FAILED", "Failed to import file '$fileName': $err")
            }
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            val entity = fileDao.getFileById(fileId) ?: return@launch
            File(entity.localPath).delete()
            fileDao.deleteFileById(fileId)
            logEvent("FILE_DELETE", "Deleted file: '${entity.name}'")
        }
    }

    fun renameFile(fileId: String, newName: String) {
        viewModelScope.launch {
            val oldFile = fileDao.getFileById(fileId)
            fileDao.renameFile(fileId, newName)
            logEvent("FILE_RENAME", "Renamed file from '${oldFile?.name}' to '$newName'")
        }
    }

    fun exportFile(fileId: String, outputUri: Uri, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val entity = fileDao.getFileById(fileId)
            val result = storageManager.exportFile(fileId, outputUri)
            if (result.isSuccess) {
                logEvent("FILE_EXPORT", "Exported and decrypted file: '${entity?.name}'")
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Export failed"
                logEvent("FILE_EXPORT_FAILED", "Failed to export file '${entity?.name}': $err")
                onFailure(err)
            }
        }
    }

    fun openFileViewer(file: VaultFile) {
        viewModelScope.launch {
            val tempFile = storageManager.createTempDecryptedFile(file.id)
            if (tempFile != null && tempFile.exists()) {
                _uiState.update { it.copy(viewingFile = file, tempViewingFile = tempFile) }
                logEvent("FILE_VIEW", "Opened sandboxed preview for file: '${file.name}'")
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to decrypt and open file") }
                logEvent("FILE_VIEW_FAILED", "Failed to decrypt and preview file: '${file.name}'")
            }
        }
    }

    fun closeFileViewer() {
        val currentTemp = _uiState.value.tempViewingFile
        _uiState.update { it.copy(viewingFile = null, tempViewingFile = null) }
        viewModelScope.launch {
            currentTemp?.delete()
            storageManager.cleanupTempFiles()
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    override fun onCleared() {
        super.onCleared()
        storageManager.cleanupTempFiles()
    }
}
