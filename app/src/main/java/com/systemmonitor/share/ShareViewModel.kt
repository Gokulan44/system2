package com.systemmonitor.share

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.vault.authentication.VaultAuthenticator
import com.systemmonitor.vault.authentication.AuthenticationResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ShareUiState(
    val isAuthenticating: Boolean = true,
    val isAuthenticated: Boolean = false,
    val authError: String? = null,
    val filesToImport: List<SharedFile> = emptyList(),
    val importProgress: String = "",
    val isImporting: Boolean = false,
    val importFinished: Boolean = false,
    val importSummary: String = ""
)

@HiltViewModel
class ShareViewModel @Inject constructor(
    private val intentParser: ShareIntentParser,
    private val fileResolver: SharedFileResolver,
    private val authenticator: VaultAuthenticator,
    private val shareImportManager: ShareImportManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ShareUiState())
    val uiState: StateFlow<ShareUiState> = _uiState.asStateFlow()

    private var rawUris: List<Uri> = emptyList()

    fun initUris(uris: List<Uri>) {
        rawUris = uris
        viewModelScope.launch {
            val resolved = uris.mapNotNull { fileResolver.resolveSharedFile(it) }
            _uiState.value = _uiState.value.copy(
                filesToImport = resolved,
                isAuthenticating = true
            )
        }
    }

    fun authenticate(pin: String) {
        viewModelScope.launch {
            val result = authenticator.authenticatePin(pin)
            if (result is AuthenticationResult.Success) {
                _uiState.value = _uiState.value.copy(
                    isAuthenticating = false,
                    isAuthenticated = true,
                    authError = null
                )
                startImport()
            } else {
                val errorMsg = when (result) {
                    is AuthenticationResult.LockedOut -> "Too many incorrect attempts. Locked out."
                    is AuthenticationResult.InvalidCredentials -> "Invalid PIN. Remaining attempts: ${result.remainingAttempts}"
                    is AuthenticationResult.Error -> result.message
                    else -> "Authentication failed."
                }
                _uiState.value = _uiState.value.copy(
                    authError = errorMsg
                )
            }
        }
    }

    private fun startImport() {
        viewModelScope.launch {
            val files = _uiState.value.filesToImport
            if (files.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    importFinished = true,
                    importSummary = "No files selected to import."
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(isImporting = true)
            val results = shareImportManager.importSharedFiles(files) { current, total ->
                _uiState.value = _uiState.value.copy(
                    importProgress = "Importing file $current of $total..."
                )
            }

            val successfulImports = results.count { it.isSuccess }
            val failedImports = results.count { it.isFailure }

            _uiState.value = _uiState.value.copy(
                isImporting = false,
                importFinished = true,
                importSummary = "Successfully imported $successfulImports of ${files.size} files." +
                        if (failedImports > 0) " ($failedImports failed)" else ""
            )
        }
    }
}
