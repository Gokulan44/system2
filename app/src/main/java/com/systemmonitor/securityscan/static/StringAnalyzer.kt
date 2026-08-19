package com.systemmonitor.securityscan.static

import java.io.File
import java.util.regex.Pattern
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

data class StringAnalysisResult(
    val httpUrls: List<String>,
    val ipAddresses: List<String>,
    val shellCommands: List<String>
)

@Singleton
class StringAnalyzer @Inject constructor() {
    private val httpPattern = Pattern.compile("http://[a-zA-Z0-9.\\-_]+(:\\d+)?(/[a-zA-Z0-9.\\-_]*)*")
    private val ipPattern = Pattern.compile("\\b(?:[0-9]{1,3}\\.){3}[0-9]{1,3}\\b")
    private val shellPattern = Pattern.compile("(?i)\\b(pm install|rm -rf|chmod \\+x|mount -o remount|/system/bin/|/system/xbin/)\\b")

    fun analyze(file: File): StringAnalysisResult {
        val httpUrls = mutableSetOf<String>()
        val ipAddresses = mutableSetOf<String>()
        val shellCommands = mutableSetOf<String>()

        try {
            ZipFile(file).use { zip ->
                val entries = zip.entries().asSequence().filter {
                    val name = it.name
                    name.endsWith(".dex") || name.startsWith("assets/") || name.endsWith(".properties")
                }

                val buffer = ByteArray(1024 * 1024) // 1MB buffer chunk
                for (entry in entries) {
                    zip.getInputStream(entry).use { stream ->
                        var read: Int
                        while (stream.read(buffer).also { read = it } != -1) {
                            val content = String(buffer, 0, read, Charsets.US_ASCII)
                            
                            val httpMatcher = httpPattern.matcher(content)
                            while (httpMatcher.find()) {
                                httpUrls.add(httpMatcher.group())
                                if (httpUrls.size >= 10) break
                            }

                            val ipMatcher = ipPattern.matcher(content)
                            while (ipMatcher.find()) {
                                val ip = ipMatcher.group()
                                // Skip local host / common non-routable IPs if wanted, or log them
                                if (ip != "127.0.0.1" && ip != "0.0.0.0") {
                                    ipAddresses.add(ip)
                                }
                                if (ipAddresses.size >= 10) break
                            }

                            val shellMatcher = shellPattern.matcher(content)
                            while (shellMatcher.find()) {
                                shellCommands.add(shellMatcher.group())
                                if (shellCommands.size >= 10) break
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Log/ignore zip parsing errors
        }

        return StringAnalysisResult(
            httpUrls = httpUrls.toList(),
            ipAddresses = ipAddresses.toList(),
            shellCommands = shellCommands.toList()
        )
    }
}
