package com.systemmonitor.vault.backup

import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.database.VaultFolderDao
import com.systemmonitor.vault.database.VaultFolderEntity
import com.systemmonitor.vault.database.VaultFileEntity
import com.systemmonitor.vault.storage.VaultDirectoryManager
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EncryptedBackupManager @Inject constructor(
    private val directoryManager: VaultDirectoryManager,
    private val fileDao: VaultFileDao,
    private val folderDao: VaultFolderDao
) {
    suspend fun createBackupArchive(): Result<File> {
        return try {
            val backupFile = File(directoryManager.backupDir, "vault_backup_${System.currentTimeMillis()}.zip")
            
            // 1. Gather all database records
            val folders = folderDao.getAllFoldersList()
            val files = fileDao.getAllFilesList()
            
            val jsonRoot = JSONObject()
            
            val foldersArray = JSONArray()
            folders.forEach {
                val f = JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("parentId", it.parentId ?: JSONObject.NULL)
                    put("createdAt", it.createdAt)
                    put("isTrash", it.isTrash)
                }
                foldersArray.put(f)
            }
            jsonRoot.put("folders", foldersArray)

            val filesArray = JSONArray()
            files.forEach {
                val f = JSONObject().apply {
                    put("id", it.id)
                    put("name", it.name)
                    put("localPath", it.localPath)
                    put("mimeType", it.mimeType)
                    put("sizeBytes", it.sizeBytes)
                    put("parentId", it.parentId ?: JSONObject.NULL)
                    put("createdAt", it.createdAt)
                    put("fileHash", it.fileHash ?: JSONObject.NULL)
                    put("checksum", it.checksum ?: JSONObject.NULL)
                    put("isTrash", it.isTrash)
                    put("trashedAt", it.trashedAt ?: JSONObject.NULL)
                }
                filesArray.put(f)
            }
            jsonRoot.put("files", filesArray)

            // 2. Package into zip archive
            ZipOutputStream(FileOutputStream(backupFile)).use { zos ->
                // Write Metadata
                val metaEntry = ZipEntry("vault_metadata.json")
                zos.putNextEntry(metaEntry)
                zos.write(jsonRoot.toString(4).toByteArray())
                zos.closeEntry()
                
                // Write encrypted file contents
                directoryManager.encryptedDir.listFiles()?.forEach { file ->
                    if (file.isFile) {
                        val entry = ZipEntry("files/${file.name}")
                        zos.putNextEntry(entry)
                        FileInputStream(file).use { fis -> fis.copyTo(zos) }
                        zos.closeEntry()
                    }
                }
            }
            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun restoreBackupArchive(backupFile: File): Result<Unit> {
        return try {
            val zipFile = ZipFile(backupFile)
            val metaEntry = zipFile.getEntry("vault_metadata.json")
                ?: return Result.failure(Exception("Invalid backup format: Missing vault_metadata.json"))

            // 1. Read metadata JSON
            val metaContent = zipFile.getInputStream(metaEntry).bufferedReader().use { it.readText() }
            val jsonRoot = JSONObject(metaContent)

            // 2. Parse and restore folders
            val foldersArray = jsonRoot.optJSONArray("folders")
            if (foldersArray != null) {
                for (i in 0 until foldersArray.length()) {
                    val f = foldersArray.getJSONObject(i)
                    val folder = VaultFolderEntity(
                        id = f.getString("id"),
                        name = f.getString("name"),
                        parentId = if (f.isNull("parentId")) null else f.getString("parentId"),
                        createdAt = f.getLong("createdAt"),
                        isTrash = f.optBoolean("isTrash", false)
                    )
                    folderDao.insertFolder(folder)
                }
            }

            // 3. Parse and restore files
            val filesArray = jsonRoot.optJSONArray("files")
            if (filesArray != null) {
                for (i in 0 until filesArray.length()) {
                    val f = filesArray.getJSONObject(i)
                    val fileEntity = VaultFileEntity(
                        id = f.getString("id"),
                        name = f.getString("name"),
                        localPath = f.getString("localPath"),
                        mimeType = f.getString("mimeType"),
                        sizeBytes = f.getLong("sizeBytes"),
                        parentId = if (f.isNull("parentId")) null else f.getString("parentId"),
                        createdAt = f.getLong("createdAt"),
                        fileHash = if (f.isNull("fileHash")) null else f.getString("fileHash"),
                        checksum = if (f.isNull("checksum")) null else f.getString("checksum"),
                        isTrash = f.optBoolean("isTrash", false),
                        trashedAt = if (f.isNull("trashedAt")) null else f.getLong("trashedAt")
                    )
                    fileDao.insertFile(fileEntity)
                }
            }

            // 4. Extract encrypted content blocks
            val entries = zipFile.entries()
            while (entries.hasMoreElements()) {
                val entry = entries.nextElement()
                if (entry.name.startsWith("files/") && !entry.isDirectory) {
                    val fileName = entry.name.removePrefix("files/")
                    val destFile = File(directoryManager.encryptedDir, fileName)
                    zipFile.getInputStream(entry).use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

@Singleton
class BackupValidator @Inject constructor() {
    fun validateBackupFile(file: File): Boolean {
        return file.exists() && file.length() > 0 && file.name.endsWith(".zip", ignoreCase = true)
    }
}

@Singleton
class VaultBackupManager @Inject constructor(
    val backupManager: EncryptedBackupManager,
    val backupValidator: BackupValidator
)
