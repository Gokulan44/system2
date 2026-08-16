package com.systemmonitor.vault.media

import android.media.MediaMetadataRetriever
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaMetadataReader @Inject constructor() {

    fun readMediaMetadata(file: File): MediaMetadata {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(file.absolutePath)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)

            MediaMetadata(
                durationMs = duration,
                width = width,
                height = height,
                title = title,
                artist = artist
            )
        } catch (e: Exception) {
            MediaMetadata()
        } finally {
            try { retriever.release() } catch (_: Exception) {}
        }
    }

    data class MediaMetadata(
        val durationMs: Long = 0L,
        val width: Int = 0,
        val height: Int = 0,
        val title: String? = null,
        val artist: String? = null
    )
}
