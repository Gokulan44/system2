package com.systemmonitor.vault.importexport

enum class ImportStatus {
    IDLE,
    VALIDATING,
    CHECKING_DUPLICATES,
    ENCRYPTING,
    COMPLETED,
    FAILED
}

enum class ExportStatus {
    IDLE,
    VALIDATING,
    DECRYPTING,
    WRITING,
    COMPLETED,
    FAILED
}

data class ImportProgress(
    val status: ImportStatus = ImportStatus.IDLE,
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val bytesProcessed: Long = 0L,
    val totalBytes: Long = 0L,
    val errorMessage: String? = null
)

data class ExportProgress(
    val status: ExportStatus = ExportStatus.IDLE,
    val currentFileName: String = "",
    val currentFileIndex: Int = 0,
    val totalFiles: Int = 0,
    val bytesProcessed: Long = 0L,
    val totalBytes: Long = 0L,
    val errorMessage: String? = null
)
