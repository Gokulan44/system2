package com.systemmonitor.vault.media

import android.graphics.Bitmap
import com.systemmonitor.vault.storage.VaultStorageManager
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageProcessor @Inject constructor(
    private val thumbnailManager: ThumbnailManager
) {
    fun processImagePreview(decryptedFile: File): Bitmap? {
        return thumbnailManager.generateImageThumbnail(decryptedFile, maxDimension = 1024)
    }
}

@Singleton
class VideoProcessor @Inject constructor(
    private val thumbnailManager: ThumbnailManager,
    private val mediaMetadataReader: MediaMetadataReader
) {
    fun processVideoDetails(decryptedFile: File): VideoInfo {
        val metadata = mediaMetadataReader.readMediaMetadata(decryptedFile)
        val thumbnail = thumbnailManager.generateVideoThumbnail(decryptedFile)
        return VideoInfo(metadata.durationMs, metadata.width, metadata.height, thumbnail)
    }

    data class VideoInfo(
        val durationMs: Long,
        val width: Int,
        val height: Int,
        val thumbnail: Bitmap?
    )
}

@Singleton
class AudioProcessor @Inject constructor(
    private val mediaMetadataReader: MediaMetadataReader
) {
    fun processAudioDetails(decryptedFile: File): AudioInfo {
        val metadata = mediaMetadataReader.readMediaMetadata(decryptedFile)
        return AudioInfo(metadata.durationMs, metadata.title ?: "Unknown Track", metadata.artist ?: "Unknown Artist")
    }

    data class AudioInfo(
        val durationMs: Long,
        val title: String,
        val artist: String
    )
}

@Singleton
class PreviewManager @Inject constructor(
    private val storageManager: VaultStorageManager
) {
    suspend fun getPreviewFile(fileId: String): File? {
        return storageManager.createTempDecryptedFile(fileId)
    }
}

@Singleton
class VaultMediaManager @Inject constructor(
    val thumbnailManager: ThumbnailManager,
    val metadataReader: MediaMetadataReader,
    val imageProcessor: ImageProcessor,
    val videoProcessor: VideoProcessor,
    val audioProcessor: AudioProcessor,
    val previewManager: PreviewManager
)
