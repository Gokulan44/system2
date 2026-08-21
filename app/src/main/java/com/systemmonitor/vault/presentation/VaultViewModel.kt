package com.systemmonitor.vault.presentation

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.vault.authentication.AuthenticationResult
import com.systemmonitor.vault.authentication.VaultAuthManager
import com.systemmonitor.vault.backup.VaultBackupManager
import com.systemmonitor.vault.database.VaultAuditEntity
import com.systemmonitor.vault.folders.VaultFolderManager
import com.systemmonitor.vault.importexport.ImportProgress
import com.systemmonitor.vault.importexport.VaultExportManager
import com.systemmonitor.vault.importing.FileImportManager
import com.systemmonitor.vault.model.VaultFile
import com.systemmonitor.vault.model.VaultFolder
import com.systemmonitor.vault.repository.VaultRepository
import com.systemmonitor.vault.security.VaultSecurityManager
import com.systemmonitor.vault.storage.VaultStorageManager
import com.systemmonitor.vault.trash.VaultTrashManager
import com.systemmonitor.vault.domain.model.VaultIntegrityStatus
import com.systemmonitor.vault.domain.usecase.ImportVaultFileUseCase
import com.systemmonitor.vault.domain.usecase.GetVaultFilesUseCase
import com.systemmonitor.vault.domain.usecase.VerifyVaultIntegrityUseCase
import com.systemmonitor.vault.domain.usecase.DecryptVaultFileUseCase
import com.systemmonitor.vault.domain.usecase.ExportVaultFileUseCase
import com.systemmonitor.vault.domain.usecase.DeleteVaultFileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class VaultAnalysisState(
    val totalSize: Long = 0L,
    val totalFiles: Int = 0,
    val imageCount: Int = 0,
    val imageSize: Long = 0L,
    val videoCount: Int = 0,
    val videoSize: Long = 0L,
    val documentCount: Int = 0,
    val documentSize: Long = 0L,
    val audioCount: Int = 0,
    val audioSize: Long = 0L,
    val archiveCount: Int = 0,
    val archiveSize: Long = 0L,
    val apkCount: Int = 0,
    val apkSize: Long = 0L,
    val otherCount: Int = 0,
    val otherSize: Long = 0L
)

data class VaultUiState(
    val isLocked: Boolean = true,
    val isSetup: Boolean = false,
    val currentFolderId: String? = null,
    val folders: List<VaultFolder> = emptyList(),
    val files: List<VaultFile> = emptyList(),
    val breadcrumbs: List<VaultFolder> = emptyList(),
    val isImporting: Boolean = false,
    val importProgress: ImportProgress = ImportProgress(),
    val viewingFile: VaultFile? = null,
    val tempViewingFile: File? = null,
    val errorMessage: String? = null
)

@HiltViewModel
class VaultViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val authenticator: VaultAuthManager,
    private val importManager: FileImportManager,
    private val exportManager: VaultExportManager,
    private val repository: VaultRepository,
    private val folderManager: VaultFolderManager,
    private val storageManager: VaultStorageManager,
    private val trashManager: VaultTrashManager,
    private val securityManager: VaultSecurityManager,
    private val backupManager: VaultBackupManager,
    private val importVaultFileUseCase: ImportVaultFileUseCase,
    private val getVaultFilesUseCase: GetVaultFilesUseCase,
    private val verifyVaultIntegrityUseCase: VerifyVaultIntegrityUseCase,
    private val decryptVaultFileUseCase: DecryptVaultFileUseCase,
    private val exportVaultFileUseCase: ExportVaultFileUseCase,
    private val deleteVaultFileUseCase: DeleteVaultFileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(VaultUiState())
    val uiState: StateFlow<VaultUiState> = _uiState.asStateFlow()

    private val currentFolderIdFlow = MutableStateFlow<String?>(null)

    val allVaultFiles: StateFlow<List<VaultFile>> = getVaultFilesUseCase.getAllFiles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val vaultAnalysis: StateFlow<VaultAnalysisState> = allVaultFiles.map { files ->
        var totalSize = 0L
        var imgCount = 0
        var imgSize = 0L
        var vidCount = 0
        var vidSize = 0L
        var docCount = 0
        var docSize = 0L
        var audCount = 0
        var audSize = 0L
        var arcCount = 0
        var arcSize = 0L
        var apkCount = 0
        var apkSize = 0L
        var othCount = 0
        var othSize = 0L

        for (file in files) {
            totalSize += file.sizeBytes
            when (file.fileType) {
                com.systemmonitor.vault.model.VaultFileType.IMAGE -> {
                    imgCount++
                    imgSize += file.sizeBytes
                }
                com.systemmonitor.vault.model.VaultFileType.VIDEO -> {
                    vidCount++
                    vidSize += file.sizeBytes
                }
                com.systemmonitor.vault.model.VaultFileType.DOCUMENT -> {
                    docCount++
                    docSize += file.sizeBytes
                }
                com.systemmonitor.vault.model.VaultFileType.AUDIO -> {
                    audCount++
                    audSize += file.sizeBytes
                }
                com.systemmonitor.vault.model.VaultFileType.ARCHIVE -> {
                    arcCount++
                    arcSize += file.sizeBytes
                }
                com.systemmonitor.vault.model.VaultFileType.APK -> {
                    apkCount++
                    apkSize += file.sizeBytes
                }
                else -> {
                    othCount++
                    othSize += file.sizeBytes
                }
            }
        }

        VaultAnalysisState(
            totalSize = totalSize,
            totalFiles = files.size,
            imageCount = imgCount,
            imageSize = imgSize,
            videoCount = vidCount,
            videoSize = vidSize,
            documentCount = docCount,
            documentSize = docSize,
            audioCount = audCount,
            audioSize = audSize,
            archiveCount = arcCount,
            archiveSize = arcSize,
            apkCount = apkCount,
            apkSize = apkSize,
            otherCount = othCount,
            otherSize = othSize
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), VaultAnalysisState())

    val auditLogs: StateFlow<List<VaultAuditEntity>> = repository.audits.getAllAudits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        checkSetupStatus()
        observeCurrentFolderContents()
        observeImportProgress()
        decryptVaultFileUseCase.cleanupTempFiles()
    }

    private fun checkSetupStatus() {
        _uiState.update { it.copy(isSetup = authenticator.isSetup()) }
    }

    private fun observeImportProgress() {
        viewModelScope.launch {
            importManager.importProgress.collect { progress ->
                _uiState.update {
                    it.copy(
                        isImporting = progress.status == com.systemmonitor.vault.importexport.ImportStatus.ENCRYPTING ||
                                      progress.status == com.systemmonitor.vault.importexport.ImportStatus.VALIDATING,
                        importProgress = progress
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCurrentFolderContents() {
        viewModelScope.launch {
            currentFolderIdFlow.flatMapLatest { folderId ->
                combine(
                    repository.folders.getFoldersInFolder(folderId),
                    getVaultFilesUseCase(folderId)
                ) { folders, files ->
                    val breadcrumbs = folderManager.folderTree.buildBreadcrumbPath(folderId)
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

    fun setupVault(pin: String): Boolean {
        val success = authenticator.setupPin(pin)
        if (success) {
            _uiState.update { it.copy(isSetup = true, isLocked = false) }
            logEvent("SETUP", "Secure Vault initialized and PIN created.")
        }
        return success
    }

    suspend fun unlockVault(pin: String): Boolean {
        val result = authenticator.authenticatePin(pin)
        return when (result) {
            is AuthenticationResult.Success -> {
                _uiState.update { it.copy(isLocked = false) }
                logEvent("UNLOCK", "Vault unlocked successfully.")
                true
            }
            is AuthenticationResult.InvalidCredentials -> {
                _uiState.update { it.copy(errorMessage = "Incorrect PIN. ${result.remainingAttempts} attempts remaining.") }
                logEvent("UNLOCK_FAILED", "Failed unlock attempt with incorrect PIN.")
                false
            }
            is AuthenticationResult.LockedOut -> {
                val seconds = result.cooldownMs / 1000
                _uiState.update { it.copy(errorMessage = "Too many failed attempts. Locked out for ${seconds}s.") }
                logEvent("UNLOCK_LOCKED_OUT", "Vault locked out due to repeated failed attempts.")
                false
            }
            is AuthenticationResult.VaultWiped -> {
                _uiState.update { it.copy(errorMessage = result.message, isSetup = false) }
                logEvent("VAULT_WIPED", "Vault wiped due to security threshold.")
                false
            }
            is AuthenticationResult.Error -> {
                _uiState.update { it.copy(errorMessage = result.message) }
                false
            }
        }
    }

    fun lockVault() {
        val wasLocked = _uiState.value.isLocked
        authenticator.lockVault()
        _uiState.update { it.copy(isLocked = true, currentFolderId = null) }
        currentFolderIdFlow.value = null
        if (!wasLocked) {
            logEvent("LOCK", "Vault locked manually or on app backgrounded.")
        }
    }

    fun resetVault() {
        viewModelScope.launch {
            context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE).edit().clear().apply()
            repository.audits.clearAllAudits()
            authenticator.lockVault()
            _uiState.update { VaultUiState(isLocked = true, isSetup = false) }
        }
    }

    fun navigateToFolder(folderId: String?) {
        currentFolderIdFlow.value = folderId
    }

    fun navigateUp() {
        val currentId = currentFolderIdFlow.value ?: return
        viewModelScope.launch {
            val folder = repository.folders.getFolderById(currentId)
            currentFolderIdFlow.value = folder?.parentId
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            folderManager.folderOperations.createFolder(name, currentFolderIdFlow.value)
            logEvent("FOLDER_CREATE", "Created folder: '$name'")
        }
    }

    fun renameFolder(folderId: String, newName: String) {
        viewModelScope.launch {
            val oldFolder = repository.folders.getFolderById(folderId)
            folderManager.folderOperations.renameFolder(folderId, newName)
            logEvent("FOLDER_RENAME", "Renamed folder from '${oldFolder?.name}' to '$newName'")
        }
    }

    fun deleteFolder(folderId: String) {
        viewModelScope.launch {
            val folder = repository.folders.getFolderById(folderId)
            folderManager.folderOperations.deleteFolder(folderId)
            logEvent("FOLDER_DELETE", "Deleted folder and contents: '${folder?.name}'")
        }
    }

    fun importFile(uri: Uri) {
        viewModelScope.launch {
            val result = importVaultFileUseCase(uri, currentFolderIdFlow.value, allowDuplicates = true)
            if (result.isFailure) {
                val err = result.exceptionOrNull()?.message ?: "Import failed"
                _uiState.update { it.copy(errorMessage = err) }
            }
        }
    }

    fun importFiles(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            val results = importVaultFileUseCase.importMultiple(uris, currentFolderIdFlow.value, allowDuplicates = true)
            val failures = results.filter { it.isFailure }
            if (failures.isNotEmpty()) {
                val err = "Import completed with ${failures.size} error(s)"
                _uiState.update { it.copy(errorMessage = err) }
            }
        }
    }

    fun deleteFile(fileId: String) {
        viewModelScope.launch {
            deleteVaultFileUseCase(fileId)
        }
    }

    fun renameFile(fileId: String, newName: String) {
        viewModelScope.launch {
            val oldFile = repository.files.getFileById(fileId)
            repository.files.renameFile(fileId, newName)
            logEvent("FILE_RENAME", "Renamed file from '${oldFile?.name}' to '$newName'")
        }
    }

    fun exportFile(fileId: String, outputUri: Uri, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val result = exportVaultFileUseCase(fileId, outputUri)
            if (result.isSuccess) {
                onSuccess()
            } else {
                val err = result.exceptionOrNull()?.message ?: "Export failed"
                onFailure(err)
            }
        }
    }

    fun openFileViewer(file: VaultFile) {
        viewModelScope.launch {
            val tempFile = decryptVaultFileUseCase(file.id)
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
            decryptVaultFileUseCase.cleanupTempFiles()
        }
    }

    suspend fun getDecryptedTempFile(fileId: String): File? {
        return decryptVaultFileUseCase(fileId)
    }

    fun runIntegrityCheck() {
        viewModelScope.launch {
            val files = getVaultFilesUseCase.getAllFiles().first()
            var corrupted = 0
            for (file in files) {
                val check = verifyVaultIntegrityUseCase(file.id)
                if (check is VaultIntegrityStatus.Corrupted) {
                    corrupted++
                }
            }
            if (corrupted == 0) {
                _uiState.update { it.copy(errorMessage = "Integrity Scan Passed: All ${files.size} files verified clean!") }
            } else {
                _uiState.update { it.copy(errorMessage = "Integrity Warning: Found $corrupted corrupted file(s).") }
            }
        }
    }

    fun createVaultBackup() {
        viewModelScope.launch {
            val result = backupManager.backupManager.createBackupArchive()
            if (result.isSuccess) {
                val backupFile = result.getOrThrow()
                _uiState.update { it.copy(errorMessage = "Vault backup archive created: ${backupFile.name}") }
                logEvent("BACKUP_CREATE", "Created encrypted backup archive: ${backupFile.name}")
            } else {
                val err = result.exceptionOrNull()?.message ?: "Backup failed"
                _uiState.update { it.copy(errorMessage = "Backup creation failed: $err") }
            }
        }
    }

    fun restoreVaultBackup(backupFile: File) {
        viewModelScope.launch {
            val result = backupManager.backupManager.restoreBackupArchive(backupFile)
            if (result.isSuccess) {
                _uiState.update { it.copy(errorMessage = "Vault restored successfully from backup!") }
                logEvent("BACKUP_RESTORE", "Restored vault backup archive: ${backupFile.name}")
                currentFolderIdFlow.value = currentFolderIdFlow.value // force refresh UI content
            } else {
                val err = result.exceptionOrNull()?.message ?: "Restore failed"
                _uiState.update { it.copy(errorMessage = "Restore failed: $err") }
                logEvent("BACKUP_RESTORE_FAILED", "Failed restore attempt: $err")
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    private fun logEvent(action: String, details: String) {
        viewModelScope.launch {
            repository.audits.logEvent(action, details)
        }
    }

    override fun onCleared() {
        super.onCleared()
        decryptVaultFileUseCase.cleanupTempFiles()
    }
}
