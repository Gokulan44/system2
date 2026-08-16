package com.systemmonitor.vault.media

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThumbnailManager @Inject constructor() {

    fun generateImageThumbnail(file: File, maxDimension: Int = 256): Bitmap? {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeFile(file.absolutePath, options)
        val (width, height) = options.outWidth to options.outHeight

        if (width <= 0 || height <= 0) return null

        var sampleSize = 1
        while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
            sampleSize *= 2
        }

        val decodeOptions = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        return BitmapFactory.decodeFile(file.absolutePath, decodeOptions)
    }

    fun generateVideoThumbnail(file: File): Bitmap? {
        return try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(file.absolutePath)
            val bitmap = retriever.getFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
            retriever.release()
            bitmap
        } catch (e: Exception) {
            null
        }
    }
}
