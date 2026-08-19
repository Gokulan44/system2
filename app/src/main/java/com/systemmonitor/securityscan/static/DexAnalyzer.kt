package com.systemmonitor.securityscan.static

import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

data class DexAnalysisResult(
    val hasDynamicLoading: Boolean,
    val hasReflection: Boolean,
    val hasSystemExec: Boolean,
    val hasAntiDebugging: Boolean,
    val foundPatterns: List<String>
)

@Singleton
class DexAnalyzer @Inject constructor() {
    fun analyze(file: File): DexAnalysisResult {
        var hasDynamicLoading = false
        var hasReflection = false
        var hasSystemExec = false
        var hasAntiDebugging = false
        val foundPatterns = mutableListOf<String>()

        try {
            ZipFile(file).use { zip ->
                val dexEntries = zip.entries().asSequence().filter {
                    it.name.startsWith("classes") && it.name.endsWith(".dex")
                }

                for (entry in dexEntries) {
                    zip.getInputStream(entry).use { stream ->
                        val bytes = stream.readBytes()
                        
                        if (!hasDynamicLoading && containsPattern(bytes, "DexClassLoader") || containsPattern(bytes, "PathClassLoader")) {
                            hasDynamicLoading = true
                            foundPatterns.add("Dynamic Class Loading (DexClassLoader/PathClassLoader)")
                        }
                        if (!hasReflection && containsPattern(bytes, "getMethod") || containsPattern(bytes, "getDeclaredMethod") || containsPattern(bytes, "java/lang/reflect")) {
                            hasReflection = true
                            foundPatterns.add("Java Reflection APIs")
                        }
                        if (!hasSystemExec && containsPattern(bytes, "exec") || containsPattern(bytes, "ProcessBuilder") || containsPattern(bytes, "/system/bin/sh") || containsPattern(bytes, "/system/xbin/su")) {
                            hasSystemExec = true
                            foundPatterns.add("System Shell Execution (exec/su)")
                        }
                        if (!hasAntiDebugging && containsPattern(bytes, "isDebuggerConnected")) {
                            hasAntiDebugging = true
                            foundPatterns.add("Anti-Debugging Check (isDebuggerConnected)")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Error scanning zip/dex
        }

        return DexAnalysisResult(
            hasDynamicLoading = hasDynamicLoading,
            hasReflection = hasReflection,
            hasSystemExec = hasSystemExec,
            hasAntiDebugging = hasAntiDebugging,
            foundPatterns = foundPatterns
        )
    }

    private fun containsPattern(bytes: ByteArray, pattern: String): Boolean {
        val patternBytes = pattern.toByteArray(Charsets.UTF_8)
        if (patternBytes.isEmpty() || bytes.size < patternBytes.size) return false

        // Simple Boyer-Moore-Horspool or naive search for bytes
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
