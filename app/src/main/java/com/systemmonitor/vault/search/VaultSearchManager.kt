package com.systemmonitor.vault.search

import com.systemmonitor.vault.model.VaultFile
import com.systemmonitor.vault.model.VaultFileType
import com.systemmonitor.vault.model.VaultSortType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileFilter @Inject constructor() {
    fun filterFiles(
        files: List<VaultFile>,
        query: String = "",
        typeFilter: VaultFileType? = null
    ): List<VaultFile> {
        return files.filter { file ->
            val matchesQuery = query.isBlank() || file.name.contains(query, ignoreCase = true)
            val matchesType = typeFilter == null || file.fileType == typeFilter
            matchesQuery && matchesType
        }
    }
}

@Singleton
class SortManager @Inject constructor() {
    fun sortFiles(files: List<VaultFile>, sortType: VaultSortType): List<VaultFile> {
        return when (sortType) {
            VaultSortType.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            VaultSortType.NAME_DESC -> files.sortedByDescending { it.name.lowercase() }
            VaultSortType.DATE_ASC -> files.sortedBy { it.createdAt }
            VaultSortType.DATE_DESC -> files.sortedByDescending { it.createdAt }
            VaultSortType.SIZE_ASC -> files.sortedBy { it.sizeBytes }
            VaultSortType.SIZE_DESC -> files.sortedByDescending { it.sizeBytes }
        }
    }
}

@Singleton
class VaultSearchManager @Inject constructor(
    val fileFilter: FileFilter,
    val sortManager: SortManager
) {
    fun searchAndSort(
        files: List<VaultFile>,
        query: String,
        typeFilter: VaultFileType?,
        sortType: VaultSortType
    ): List<VaultFile> {
        val filtered = fileFilter.filterFiles(files, query, typeFilter)
        return sortManager.sortFiles(filtered, sortType)
    }
}
