package com.systemmonitor.features.reports

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.local.database.dao.BatteryDao
import com.systemmonitor.local.database.dao.MemoryDao
import com.systemmonitor.local.database.dao.NetworkDao
import com.systemmonitor.local.database.dao.StorageDao
import com.systemmonitor.features.security.data.dao.SecurityScanDao
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class ReportsUiState(
    val isGenerating: Boolean = false,
    val progress: Float = 0.0f,
    val statusText: String = "",
    val successMessage: String? = null,
    val lastGeneratedFileUri: Uri? = null,
    val error: String? = null,
    
    // Real stats counts for UI cards
    val securityScanCount: Int = 0,
    val deviceReadingCount: Int = 0,
    val networkReadingCount: Int = 0,
    val fileScanCount: Int = 0
)

@HiltViewModel
class ReportsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val batteryDao: BatteryDao,
    private val memoryDao: MemoryDao,
    private val networkDao: NetworkDao,
    private val storageDao: StorageDao,
    private val securityScanDao: SecurityScanDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportsUiState())
    val uiState: StateFlow<ReportsUiState> = _uiState.asStateFlow()

    init {
        loadReportCounts()
    }

    fun loadReportCounts() {
        viewModelScope.launch(Dispatchers.IO) {
            val since24h = System.currentTimeMillis() - (24 * 3600 * 1000L)
            
            // Query room DBs for actual recorded item counts
            val batteryCount = batteryDao.getSince(since24h).size
            val memoryCount = memoryDao.getSince(since24h).size
            val storageCount = storageDao.getSince(since24h).size
            
            val scans = securityScanDao.getAllScans().firstOrNull() ?: emptyList()
            val totalScans = scans.size
            
            val netCount = networkDao.getSince(since24h).size
            
            _uiState.update {
                it.copy(
                    securityScanCount = totalScans,
                    deviceReadingCount = batteryCount + memoryCount,
                    networkReadingCount = netCount,
                    fileScanCount = storageCount
                )
            }
        }
    }

    fun generateReport(format: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isGenerating = true,
                    progress = 0.0f,
                    statusText = "Initializing Report Engine...",
                    successMessage = null,
                    lastGeneratedFileUri = null,
                    error = null
                )
            }

            try {
                // Step 1: Collect Battery Data
                _uiState.update { it.copy(progress = 0.15f, statusText = "Querying Battery Metrics...") }
                delay(300)
                val since24h = System.currentTimeMillis() - (24 * 3600 * 1000L)
                val batteryReadings = withContext(Dispatchers.IO) { batteryDao.getSince(since24h) }
                val avgBatteryTemp = withContext(Dispatchers.IO) { batteryDao.getAverageTemperatureSince(since24h) }
                val avgBatteryLevel = withContext(Dispatchers.IO) { batteryDao.getAverageLevelSince(since24h) }

                // Step 2: Collect Memory Data
                _uiState.update { it.copy(progress = 0.35f, statusText = "Querying Memory & CPU Statistics...") }
                delay(300)
                val memoryReadings = withContext(Dispatchers.IO) { memoryDao.getSince(since24h) }
                val avgMemoryUsed = withContext(Dispatchers.IO) { memoryDao.getAverageUsedSince(since24h) }

                // Step 3: Collect Network Data
                _uiState.update { it.copy(progress = 0.55f, statusText = "Querying Network Load Profiles...") }
                delay(300)
                val networkReadings = withContext(Dispatchers.IO) { networkDao.getSince(since24h) }

                // Step 4: Collect Security Scan Info
                _uiState.update { it.copy(progress = 0.70f, statusText = "Reading Security logs...") }
                delay(300)
                val scans = withContext(Dispatchers.IO) { securityScanDao.getAllScans().firstOrNull() ?: emptyList() }
                val latestScan = scans.firstOrNull()
                val threats = latestScan?.let { withContext(Dispatchers.IO) { securityScanDao.getThreatsForScan(it.scanId) } } ?: emptyList()

                // Step 5: Format and Save File
                _uiState.update { it.copy(progress = 0.85f, statusText = "Compiling Report File...") }
                delay(400)

                val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                val fileName = when (format) {
                    "PDF" -> "SystemMonitor_Report_$timestamp.txt" // plain text viewable easily as PDF representation
                    "CSV" -> "SystemMonitor_Battery_$timestamp.csv"
                    else -> "SystemMonitor_Data_$timestamp.json"
                }

                val mimeType = when (format) {
                    "PDF" -> "text/plain"
                    "CSV" -> "text/csv"
                    else -> "application/json"
                }

                val content = when (format) {
                    "PDF" -> buildTextReport(batteryReadings, avgBatteryLevel, avgBatteryTemp, memoryReadings, avgMemoryUsed, networkReadings, latestScan, threats)
                    "CSV" -> buildCsvReport(batteryReadings)
                    else -> buildJsonReport(batteryReadings, memoryReadings, networkReadings, scans)
                }

                val savedUri = withContext(Dispatchers.IO) {
                    writeToDownloads(fileName, mimeType, content)
                }

                if (savedUri != null) {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            progress = 1.0f,
                            statusText = "Report Exported Successfully!",
                            successMessage = "Report downloaded to: Downloads/$fileName",
                            lastGeneratedFileUri = savedUri
                        )
                    }
                    loadReportCounts() // update counts card
                } else {
                    _uiState.update {
                        it.copy(
                            isGenerating = false,
                            error = "Could not save file to Downloads directory"
                        )
                    }
                }

            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isGenerating = false,
                        error = "Failed to generate report: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    private fun buildTextReport(
        battery: List<com.systemmonitor.local.database.entity.BatteryEntity>,
        avgLevel: Double?,
        avgTemp: Double?,
        memory: List<com.systemmonitor.local.database.entity.MemoryEntity>,
        avgMem: Double?,
        network: List<com.systemmonitor.local.database.entity.NetworkEntity>,
        latestScan: com.systemmonitor.features.security.data.entity.SecurityScanEntity?,
        threats: List<com.systemmonitor.features.security.data.entity.ThreatEntity>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val build = StringBuilder()
        build.append("=========================================\n")
        build.append("          SYSTEM MONITOR REPORT          \n")
        build.append("     Generated at: ${dateFormat.format(Date())} \n")
        build.append("=========================================\n\n")

        build.append("[BATTERY METRICS (Last 24 Hours)]\n")
        build.append("  Total Recorded Readings: ${battery.size}\n")
        build.append("  Average Battery Level  : ${avgLevel?.let { String.format(Locale.US, "%.1f%%", it) } ?: "N/A"}\n")
        build.append("  Average Temperature    : ${avgTemp?.let { String.format(Locale.US, "%.1f°C", it) } ?: "N/A"}\n")
        battery.firstOrNull()?.let {
            build.append("  Current Charging Status: ${if (it.isCharging) "Charging (${it.chargePlug})" else "Discharging"}\n")
            build.append("  Current Battery Health : ${it.healthStatus}\n")
        }
        build.append("\n")

        build.append("[MEMORY METRICS (Last 24 Hours)]\n")
        build.append("  Total Recorded Readings: ${memory.size}\n")
        build.append("  Average Memory Usage   : ${avgMem?.let { String.format(Locale.US, "%.1f MB", it) } ?: "N/A"}\n")
        memory.firstOrNull()?.let {
            build.append("  Latest Memory Total    : ${it.totalMb} MB\n")
            build.append("  Latest Memory Available: ${it.availableMb} MB\n")
        }
        build.append("\n")

        build.append("[NETWORK MONITORED READINGS]\n")
        build.append("  Total Recorded Readings: ${network.size}\n")
        network.take(5).forEach {
            build.append("  - Time: ${dateFormat.format(Date(it.timestamp))} | Rx: ${it.downstreamKbps} Kbps, Tx: ${it.upstreamKbps} Kbps\n")
        }
        build.append("\n")

        build.append("[SECURITY AUDIT SUMMARY]\n")
        if (latestScan != null) {
            build.append("  Last Scan Completed    : ${dateFormat.format(Date(latestScan.timestamp))}\n")
            build.append("  Security Score         : ${latestScan.score}/100\n")
            build.append("  Security Rating        : ${latestScan.rating}\n")
            build.append("  Identified Issues Count: ${latestScan.issuesFoundCount}\n")
            if (threats.isNotEmpty()) {
                build.append("  List of Flagged Issues:\n")
                threats.forEach {
                    build.append("    * [${it.severity}] ${it.title} - ${it.description}\n")
                }
            } else {
                build.append("    No threats or warnings found.\n")
            }
        } else {
            build.append("  No security scans have been performed yet.\n")
        }
        build.append("\n=========================================\n")
        build.append("            End of Report                \n")
        build.append("=========================================\n")
        return build.toString()
    }

    private fun buildCsvReport(battery: List<com.systemmonitor.local.database.entity.BatteryEntity>): String {
        val build = StringBuilder()
        build.append("ID,Timestamp,LevelPercent,IsCharging,PlugType,Health,Temperature,Voltage\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        battery.forEach {
            build.append("${it.id},${dateFormat.format(Date(it.timestamp))},${it.levelPercent},${it.isCharging},${it.chargePlug},${it.healthStatus},${it.temperatureCelsius},${it.voltageMillivolts}\n")
        }
        return build.toString()
    }

    private fun buildJsonReport(
        battery: List<com.systemmonitor.local.database.entity.BatteryEntity>,
        memory: List<com.systemmonitor.local.database.entity.MemoryEntity>,
        network: List<com.systemmonitor.local.database.entity.NetworkEntity>,
        scans: List<com.systemmonitor.features.security.data.entity.SecurityScanEntity>
    ): String {
        val build = StringBuilder()
        build.append("{\n")
        build.append("  \"generated_at\": \"${SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.getDefault()).format(Date())}\",\n")
        
        // Battery block
        build.append("  \"battery_readings_count\": ${battery.size},\n")
        build.append("  \"battery_latest\": ")
        battery.firstOrNull()?.let {
            build.append("{\n")
            build.append("    \"level\": ${it.levelPercent},\n")
            build.append("    \"is_charging\": ${it.isCharging},\n")
            build.append("    \"plug\": \"${it.chargePlug}\",\n")
            build.append("    \"temp_celsius\": ${it.temperatureCelsius}\n")
            build.append("  },\n")
        } ?: build.append("null,\n")

        // Memory block
        build.append("  \"memory_readings_count\": ${memory.size},\n")
        build.append("  \"memory_latest\": ")
        memory.firstOrNull()?.let {
            build.append("{\n")
            build.append("    \"used_mb\": ${it.usedMb},\n")
            build.append("    \"available_mb\": ${it.availableMb},\n")
            build.append("    \"total_mb\": ${it.totalMb}\n")
            build.append("  },\n")
        } ?: build.append("null,\n")

        // Network block
        build.append("  \"network_readings_count\": ${network.size},\n")

        // Security block
        build.append("  \"security_scans\": [\n")
        scans.take(5).forEachIndexed { index, scan ->
            build.append("    {\n")
            build.append("      \"timestamp\": ${scan.timestamp},\n")
            build.append("      \"score\": ${scan.score},\n")
            build.append("      \"rating\": \"${scan.rating}\",\n")
            build.append("      \"issues_found\": ${scan.issuesFoundCount}\n")
            build.append("    }${if (index < scans.take(5).size - 1) "," else ""}\n")
        }
        build.append("  ]\n")
        build.append("}")
        return build.toString()
    }

    private fun writeToDownloads(fileName: String, mimeType: String, content: String): Uri? {
        val resolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                runCatching {
                    resolver.openOutputStream(uri)?.use { outputStream ->
                        outputStream.write(content.toByteArray())
                    }
                    uri
                }.getOrNull()
            } else null
        } else {
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            if (!downloadsDir.exists()) {
                downloadsDir.mkdirs()
            }
            val file = java.io.File(downloadsDir, fileName)
            runCatching {
                file.writeText(content)
                Uri.fromFile(file)
            }.getOrNull()
        }
    }
}
