package com.systemmonitor.securityscan.staticscan

import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

data class NativeAnalysisResult(
    val hasNativeCode: Boolean,
    val nativeLibraries: List<String>,
    val foundUnsafeApis: List<String>
)

@Singleton
class NativeLibraryAnalyzer @Inject constructor() {
    fun analyze(file: File): NativeAnalysisResult {
        var hasNativeCode = false
        val nativeLibraries = mutableListOf<String>()
        val foundUnsafeApis = mutableListOf<String>()

        try {
            ZipFile(file).use { zip ->
                val soEntries = zip.entries().asSequence().filter {
                    val name = it.name
                    name.startsWith("lib/") && name.endsWith(".so")
                }

                for (entry in soEntries) {
                    hasNativeCode = true
                    val name = entry.name.substringAfterLast('/')
                    if (name !in nativeLibraries) {
                        nativeLibraries.add(name)
                    }

                    zip.getInputStream(entry).use { stream ->
                        val bytes = stream.readBytes()
                        // Check for standard risky C function signatures
                        if (containsPattern(bytes, "system\u0000") || containsPattern(bytes, "system@")) {
                            foundUnsafeApis.add("system() shell executor in $name")
                        }
                        if (containsPattern(bytes, "popen\u0000") || containsPattern(bytes, "popen@")) {
                            foundUnsafeApis.add("popen() pipe executor in $name")
                        }
                        if (containsPattern(bytes, "execve\u0000") || containsPattern(bytes, "execve@")) {
                            foundUnsafeApis.add("execve() binary executor in $name")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ignore zip errors
        }

        return NativeAnalysisResult(
            hasNativeCode = hasNativeCode,
            nativeLibraries = nativeLibraries,
            foundUnsafeApis = foundUnsafeApis
        )
    }

    private fun containsPattern(bytes: ByteArray, pattern: String): Boolean {
        val patternBytes = pattern.toByteArray(Charsets.UTF_8)
        if (patternBytes.isEmpty() || bytes.size < patternBytes.size) return false

        val maxIndex = bytes.size - patternBytes.size
        for (i in 0..maxIndex) {
            var found = true
            for (j in patternBytes.indices) {
                if (bytes[i + j] != patternBytes[j]) {
                    found = false
                    break
                }
            }
            if (found) return true
        }
        return false
    }
}
