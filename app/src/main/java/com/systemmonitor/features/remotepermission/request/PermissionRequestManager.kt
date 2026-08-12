package com.systemmonitor.features.remotepermission.request

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.systemmonitor.data.network.ConnectionManager
import com.systemmonitor.domain.model.ConnectionMode
import com.systemmonitor.domain.model.Laptop
import com.systemmonitor.domain.model.LaptopStatus
import com.systemmonitor.features.notifications.NotificationManager
import com.systemmonitor.features.remotepermission.data.entity.DownloadResultEntity
import com.systemmonitor.features.remotepermission.data.entity.SecurityScanResultEntity
import com.systemmonitor.features.remotepermission.domain.model.*
import com.systemmonitor.features.remotepermission.domain.repository.PermissionRepository
import com.systemmonitor.features.remotepermission.domain.usecase.ReceivePermissionRequestUseCase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import okhttp3.*
import org.json.JSONObject
import java.text.DecimalFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionRequestManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val receivePermissionRequestUseCase: ReceivePermissionRequestUseCase,
    private val repository: PermissionRepository,
    private val validator: RequestValidator,
    private val notificationManager: NotificationManager,
    private val firestore: FirebaseFirestore,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val TAG = "PermissionReqManager"
    }

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Cache active listeners and WebSockets to prevent duplicates
    private val activeFirestoreListeners = mutableMapOf<String, List<ListenerRegistration>>()
    private val activeWebSockets = mutableMapOf<String, WebSocket>()

    /**
     * Start background watchers for both LOCAL Wi-Fi and REMOTE cloud laptops.
     */
    fun startWatching(laptops: List<Laptop>) {
        managerScope.launch {
            val remoteLaptops = laptops.filter { it.connectionMode == ConnectionMode.REMOTE }
            val localLaptops = laptops.filter { it.connectionMode == ConnectionMode.LOCAL && it.status == LaptopStatus.ONLINE }

            // 1. Setup Firestore Listeners for Remote Devices
            setupRemoteListeners(remoteLaptops)

            // 2. Setup WebSocket Listeners for Local Devices
            setupLocalWebSockets(localLaptops)
        }
    }

    private fun setupRemoteListeners(remoteLaptops: List<Laptop>) {
        val currentIds = remoteLaptops.map { it.id }.toSet()
        
        // Remove listeners for devices no longer remote or deleted
        val keysToRemove = activeFirestoreListeners.keys.filter { it !in currentIds }
        for (key in keysToRemove) {
            activeFirestoreListeners[key]?.forEach { it.remove() }
            activeFirestoreListeners.remove(key)
        }

        // Add listeners for new remote devices
        for (laptop in remoteLaptops) {
            if (laptop.id in activeFirestoreListeners) continue

            Log.d(TAG, "Starting Firebase listeners for remote laptop: ${laptop.name}")
            val docRef = firestore.collection("relay").document(laptop.id)

            // Listener A: Incoming Permission Requests
            val requestReg = docRef.collection("permission_requests")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED) {
                            val data = change.document.data
                            val request = parseRequestFromMap(data, laptop.id)
                            if (request != null) {
                                managerScope.launch {
                                    handleIncomingRequest(request)
                                }
                            }
                        }
                    }
                }

            // Listener B: Incoming Download/Scan results
            val resultReg = docRef.collection("download_results")
                .addSnapshotListener { snapshot, e ->
                    if (e != null) return@addSnapshotListener
                    snapshot?.documentChanges?.forEach { change ->
                        if (change.type == DocumentChange.Type.ADDED || change.type == DocumentChange.Type.MODIFIED) {
                            val data = change.document.data
                            val reqId = data["requestId"] as? String ?: return@forEach
                            val status = data["status"] as? String ?: return@forEach
                            val sha256 = data["sha256"] as? String ?: ""
                            val riskLevel = data["riskLevel"] as? String ?: ""
                            val details = data["details"] as? String ?: ""
                            val resName = data["resourceName"] as? String ?: "Resource"

                            managerScope.launch {
                                saveAndNotifyScanResult(reqId, resName, status, sha256, riskLevel, details)
                            }
                        }
                    }
                }

            activeFirestoreListeners[laptop.id] = listOf(requestReg, resultReg)
        }
    }

    private fun setupLocalWebSockets(localLaptops: List<Laptop>) {
        val currentIds = localLaptops.map { it.id }.toSet()
        
        // Disconnect WebSockets for devices no longer local or offline
        val keysToRemove = activeWebSockets.keys.filter { it !in currentIds }
        for (key in keysToRemove) {
            activeWebSockets[key]?.close(1000, "Clean switch")
            activeWebSockets.remove(key)
        }

        // Connect to online local devices
        for (laptop in localLaptops) {
            if (laptop.id in activeWebSockets) continue

            Log.d(TAG, "Opening WebSocket background channel to local laptop: ${laptop.name}")
            val wsUrl = "ws://${laptop.ipAddress}:${laptop.port}/ws/permissions"
            val request = Request.Builder().url(wsUrl).build()

            val wsListener = object : WebSocketListener() {
                override fun onMessage(webSocket: WebSocket, text: String) {
                    try {
                        val json = JSONObject(text)
                        val type = json.optString("type")
                        val data = json.optJSONObject("data") ?: return

                        if (type == "permission_request") {
                            val req = parseRequestFromJson(data, laptop.id)
                            if (req != null) {
                                managerScope.launch {
                                    handleIncomingRequest(req)
                                }
                            }
                        } else if (type == "download_result") {
                            val reqId = data.optString("requestId")
                            val status = data.optString("status")
                            val sha256 = data.optString("sha256")
                            val riskLevel = data.optString("riskLevel")
                            val details = data.optString("details")
                            val filename = data.optString("resourceName", "Resource")

                            managerScope.launch {
                                saveAndNotifyScanResult(reqId, filename, status, sha256, riskLevel, details)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error reading WebSocket message: ${e.message}")
                    }
                }

                override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                    Log.w(TAG, "WebSocket failure for laptop ${laptop.name}: ${t.message}")
                    activeWebSockets.remove(laptop.id)
                }

                override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                    activeWebSockets.remove(laptop.id)
                }
            }

            val ws = okHttpClient.newWebSocket(request, wsListener)
            activeWebSockets[laptop.id] = ws
        }
    }

    suspend fun handleIncomingRequest(request: PermissionRequest) {
        if (validator.isValid(request)) {
            request.status = PermissionStatus.PENDING
            receivePermissionRequestUseCase(request)
            notificationManager.sendPermissionRequestNotification(request)
            Log.i(TAG, "Incoming permission request approved for validation: ${request.requestId}")
        } else {
            Log.w(TAG, "Invalid or expired request rejected: ${request.requestId}")
        }
    }

    private suspend fun saveAndNotifyScanResult(
        requestId: String,
        filename: String,
        status: String,
        sha256: String,
        riskLevel: String,
        details: String
    ) {
        // Save scan outcome to SQLite Room DB
        val download = DownloadResultEntity(
            requestId = requestId,
            status = if (status == "SAFE") "COMPLETED" else "QUARANTINED",
            filePath = if (status == "SAFE") "C:\\downloads\\approved\\$filename" else "C:\\quarantine\\$filename",
            completedAt = System.currentTimeMillis()
        )
        val scan = SecurityScanResultEntity(
            requestId = requestId,
            status = status,
            sha256 = sha256,
            riskLevel = riskLevel,
            details = details
        )
        repository.insertDownloadResult(download)
        repository.insertSecurityScanResult(scan)

        // Trigger android heads-up notifications
        if (status == "SAFE") {
            notificationManager.sendDownloadSuccessNotification(
                requestId = requestId,
                filename = filename,
                sizeMbText = "12 MB",
                sha256 = sha256
            )
        } else {
            notificationManager.sendDownloadQuarantinedNotification(
                requestId = requestId,
                filename = filename,
                reason = details
            )
        }
        
        // Also update request table status
        repository.updateRequestStatus(
            requestId = requestId,
            status = if (status == "SAFE") PermissionStatus.APPROVED else PermissionStatus.DENIED
        )
    }

    // --- Parsing Helpers ---
    private fun parseRequestFromMap(map: Map<String, Any>, laptopId: String): PermissionRequest? {
        return try {
            val reqId = map["requestId"] as? String ?: return null
            val resId = map["resourceId"] as? String ?: return null
            val resName = map["resourceName"] as? String ?: return null
            val resType = map["resourceType"] as? String ?: "FILE"
            val fileSize = (map["fileSize"] as? Number)?.toLong() ?: 0L
            val op = map["requestedOperation"] as? String ?: "DOWNLOAD"
            val created = (map["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            val expires = (map["expiresAt"] as? Number)?.toLong() ?: (created + 300_000)
            val nonce = map["requestNonce"] as? String ?: ""

            PermissionRequest(
                requestId = reqId,
                laptopId = laptopId,
                resource = ResourceRequest(
                    resourceId = resId,
                    name = resName,
                    type = runCatching { ResourceType.valueOf(resType) }.getOrDefault(ResourceType.FILE),
                    sizeBytes = fileSize,
                    path = ""
                ),
                requestedOperation = runCatching { PermissionType.valueOf(op) }.getOrDefault(PermissionType.DOWNLOAD),
                createdAt = created,
                expiresAt = expires,
                requestNonce = nonce,
                status = PermissionStatus.PENDING
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun parseRequestFromJson(json: JSONObject, laptopId: String): PermissionRequest? {
        return try {
            val reqId = json.getString("requestId")
            val resId = json.getString("resourceId")
            val resName = json.getString("resourceName")
            val resType = json.optString("resourceType", "FILE")
            val fileSize = json.optLong("fileSize", 0L)
            val op = json.optString("requestedOperation", "DOWNLOAD")
            val created = json.optLong("createdAt", System.currentTimeMillis())
            val expires = json.optLong("expiresAt", created + 300_000)
            val nonce = json.optString("requestNonce", "")

            PermissionRequest(
                requestId = reqId,
                laptopId = laptopId,
                resource = ResourceRequest(
                    resourceId = resId,
                    name = resName,
                    type = runCatching { ResourceType.valueOf(resType) }.getOrDefault(ResourceType.FILE),
                    sizeBytes = fileSize,
                    path = ""
                ),
                requestedOperation = runCatching { PermissionType.valueOf(op) }.getOrDefault(PermissionType.DOWNLOAD),
                createdAt = created,
                expiresAt = expires,
                requestNonce = nonce,
                status = PermissionStatus.PENDING
            )
        } catch (e: Exception) {
            null
        }
    }
}
