package com.systemmonitor.securityscan.static

import android.content.Context
import android.content.pm.PackageManager
import com.systemmonitor.securityscan.input.ScanTarget
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

data class ParsedComponent(
    val name: String,
    val exported: Boolean,
    val permission: String?
)

data class ParsedManifest(
    val packageName: String,
    val permissions: List<String>,
    val activities: List<ParsedComponent>,
    val services: List<ParsedComponent>,
    val receivers: List<ParsedComponent>,
    val providers: List<ParsedComponent>,
    val usesCleartextTraffic: Boolean
)

@Singleton
class ManifestAnalyzer @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun analyze(target: ScanTarget): ParsedManifest {
        return if (target.isSystemApp) {
            analyzeInstalled(target.packageName)
        } else {
            analyzeFile(File(target.apkPath))
        }
    }

    private fun analyzeInstalled(packageName: String): ParsedManifest {
        val pm = context.packageManager
        return try {
            val packageInfo = pm.getPackageInfo(
                packageName,
                PackageManager.GET_PERMISSIONS or
                        PackageManager.GET_ACTIVITIES or
                        PackageManager.GET_SERVICES or
                        PackageManager.GET_RECEIVERS or
                        PackageManager.GET_PROVIDERS
            )

            val permissions = packageInfo.requestedPermissions?.toList() ?: emptyList()

            val activities = packageInfo.activities?.map {
                ParsedComponent(it.name, it.exported, it.permission)
            } ?: emptyList()

            val services = packageInfo.services?.map {
                ParsedComponent(it.name, it.exported, it.permission)
            } ?: emptyList()

            val receivers = packageInfo.receivers?.map {
                ParsedComponent(it.name, it.exported, it.permission)
            } ?: emptyList()

            val providers = packageInfo.providers?.map {
                ParsedComponent(it.name, it.exported, it.readPermission ?: it.writePermission)
            } ?: emptyList()

            // Fetch usesCleartextTraffic from ApplicationInfo
            val appInfo = pm.getApplicationInfo(packageName, 0)
            val usesCleartextTraffic = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                (appInfo.flags and android.content.pm.ApplicationInfo.FLAG_USES_CLEARTEXT_TRAFFIC) != 0
            } else {
                true
            }

            ParsedManifest(
                packageName = packageName,
                permissions = permissions,
                activities = activities,
                services = services,
                receivers = receivers,
                providers = providers,
                usesCleartextTraffic = usesCleartextTraffic
            )
        } catch (e: Exception) {
            ParsedManifest(packageName, emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), true)
        }
    }

    private fun analyzeFile(file: File): ParsedManifest {
        return try {
            ZipFile(file).use { zip ->
                val entry = zip.getEntry("AndroidManifest.xml")
                if (entry != null) {
                    zip.getInputStream(entry).use { stream ->
                        val bytes = stream.readBytes()
                        parseBinaryManifest(bytes)
                    }
                } else {
                    ParsedManifest("", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), true)
                }
            }
        } catch (e: Exception) {
            ParsedManifest("", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), true)
        }
    }

    /**
     * Compact binary XML decoder for AndroidManifest.xml.
     */
    private fun parseBinaryManifest(bytes: ByteArray): ParsedManifest {
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        
        // 1. Verify Magic
        val magic = buffer.int
        if (magic != 0x00080003) {
            return ParsedManifest("", emptyList(), emptyList(), emptyList(), emptyList(), emptyList(), true)
        }
        
        // File size
        buffer.int 

        val permissions = mutableListOf<String>()
        val activities = mutableListOf<ParsedComponent>()
        val services = mutableListOf<ParsedComponent>()
        val receivers = mutableListOf<ParsedComponent>()
        val providers = mutableListOf<ParsedComponent>()
        var packageName = ""
        var usesCleartextTraffic = true

        val stringPool = mutableListOf<String>()
        
        try {
            while (buffer.hasRemaining()) {
                val chunkType = buffer.int
                val chunkSize = buffer.int
                val chunkStart = buffer.position() - 8

                when (chunkType) {
                    0x001C0001 -> { // STRING_POOL
                        val stringCount = buffer.int
                        val styleCount = buffer.int
                        val flags = buffer.int
                        val stringStart = buffer.int
                        val styleStart = buffer.int
                        
                        val offsets = IntArray(stringCount) { buffer.int }
                        
                        val poolBase = chunkStart + stringStart
                        for (i in 0 until stringCount) {
                            buffer.position(poolBase + offsets[i])
                            val string = readAxmlString(buffer, (flags and 0x100) != 0)
                            stringPool.add(string)
                        }
                    }
                    0x00100102 -> { // START_TAG
                        buffer.int // Line number
                        buffer.int // Comment
                        val nsIdx = buffer.int
                        val nameIdx = buffer.int
                        buffer.int // Attribute Start
                        buffer.int // Attribute Size
                        val attrCount = buffer.short.toInt()
                        buffer.short // Class
                        
                        val tagName = if (nameIdx in stringPool.indices) stringPool[nameIdx] else ""
                        
                        var compName = ""
                        var exported = false
                        var permission: String? = null
                        var hasExportedAttr = false

                        for (i in 0 until attrCount) {
                            val attrNsIdx = buffer.int
                            val attrNameIdx = buffer.int
                            val attrRawValueIdx = buffer.int
                            val attrType = buffer.int // Type info
                            val attrData = buffer.int // Data value
                            
                            val attrName = if (attrNameIdx in stringPool.indices) stringPool[attrNameIdx] else ""
                            val attrValue = if (attrRawValueIdx in stringPool.indices) {
                                stringPool[attrRawValueIdx]
                            } else {
                                attrData.toString()
                            }

                            when (attrName) {
                                "package" -> {
                                    if (tagName == "manifest") packageName = attrValue
                                }
                                "name" -> {
                                    compName = attrValue
                                }
                                "exported" -> {
                                    exported = attrData != 0
                                    hasExportedAttr = true
                                }
                                "permission" -> {
                                    permission = attrValue
                                }
                                "usesCleartextTraffic" -> {
                                    if (tagName == "application") {
                                        usesCleartextTraffic = attrData != 0
                                    }
                                }
                            }
                        }

                        // Fully qualify component names if needed
                        if (compName.startsWith(".")) {
                            compName = packageName + compName
                        } else if (!compName.contains(".")) {
                            compName = "$packageName.$compName"
                        }

                        when (tagName) {
                            "uses-permission" -> {
                                if (compName.isNotEmpty()) permissions.add(compName)
                            }
                            "activity" -> {
                                // Default exported value for activities is false unless they have intent filters
                                // For simplicity, if not specified and has intent filter, default to true, else false.
                                // We default to exported = false if not declared explicitly.
                                activities.add(ParsedComponent(compName, exported, permission))
                            }
                            "service" -> {
                                services.add(ParsedComponent(compName, exported, permission))
                            }
                            "receiver" -> {
                                receivers.add(ParsedComponent(compName, exported, permission))
                            }
                            "provider" -> {
                                providers.add(ParsedComponent(compName, exported, permission))
                            }
                        }
                    }
                }
                
                // Advance buffer to the end of chunk
                buffer.position(chunkStart + chunkSize)
            }
        } catch (e: Exception) {
            // End of buffer or parse error
        }

        return ParsedManifest(
            packageName = packageName,
            permissions = permissions,
            activities = activities,
            services = services,
            receivers = receivers,
            providers = providers,
            usesCleartextTraffic = usesCleartextTraffic
        )
    }

    private fun readAxmlString(buffer: ByteBuffer, isUtf8: Boolean): String {
        return if (isUtf8) {
            val len = buffer.get().toInt() and 0xFF
            // String is length-prefixed, skip character length and byte length
            val bytes = ByteArray(len)
            buffer.get(bytes)
            String(bytes, Charsets.UTF_8)
        } else {
            val charLen = buffer.short.toInt() and 0xFFFF
            val bytes = ByteArray(charLen * 2)
            buffer.get(bytes)
            String(bytes, Charsets.UTF_16LE)
        }
    }
}
