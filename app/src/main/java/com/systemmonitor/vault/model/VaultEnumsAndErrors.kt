package com.systemmonitor.vault.model

enum class VaultSortType {
    NAME_ASC,
    NAME_DESC,
    DATE_ASC,
    DATE_DESC,
    SIZE_ASC,
    SIZE_DESC
}

enum class VaultOperation {
    IMPORT,
    EXPORT,
    DELETE,
    TRASH,
    RESTORE,
    MOVE,
    RENAME,
    BACKUP,
    INTEGRITY_CHECK,
    LOCK,
    UNLOCK
}

sealed class VaultError : Exception() {
    data class AuthenticationFailed(override val message: String) : VaultError()
    data class FileNotFound(val fileId: String) : VaultError()
    data class EncryptionFailed(override val message: String, val causeThrowable: Throwable? = null) : VaultError()
    data class DecryptionFailed(override val message: String, val causeThrowable: Throwable? = null) : VaultError()
    data class IntegrityCheckFailed(val fileId: String, val expectedHash: String, val actualHash: String) : VaultError()
    data class DuplicateFileDetected(val fileName: String, val hash: String) : VaultError()
    data class InsufficientStorageSpace(val requiredBytes: Long, val availableBytes: Long) : VaultError()
    data class InvalidFileFormat(override val message: String) : VaultError()
    data class StoragePermissionDenied(override val message: String) : VaultError()
}
