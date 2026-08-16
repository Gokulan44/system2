package com.systemmonitor.vault.documents

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PdfVaultViewer @Inject constructor() {
    fun renderPdfFirstPage(pdfFile: File, width: Int = 400, height: Int = 600): Bitmap? {
        return try {
            val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fileDescriptor)
            if (renderer.pageCount > 0) {
                val page = renderer.openPage(0)
                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                renderer.close()
                fileDescriptor.close()
                bitmap
            } else {
                renderer.close()
                fileDescriptor.close()
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

@Singleton
class TextDocumentManager @Inject constructor() {
    fun readTextPreview(textFile: File, maxCharacters: Int = 2000): String {
        return try {
            textFile.bufferedReader().use { reader ->
                val buffer = CharArray(maxCharacters)
                val read = reader.read(buffer, 0, maxCharacters)
                if (read > 0) String(buffer, 0, read) else ""
            }
        } catch (e: Exception) {
            "Unable to read text preview"
        }
    }
}

@Singleton
class OfficeDocumentManager @Inject constructor() {
    fun isSupportedOfficeDocument(fileName: String): Boolean {
        val ext = File(fileName).extension.lowercase()
        return ext in listOf("doc", "docx", "xls", "xlsx", "ppt", "pptx")
    }
}

@Singleton
class DocumentPreviewManager @Inject constructor(
    val pdfViewer: PdfVaultViewer,
    val textManager: TextDocumentManager,
    val officeManager: OfficeDocumentManager
)
