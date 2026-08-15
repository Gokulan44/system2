package com.systemmonitor.features.notifications

import com.systemmonitor.features.remotepermission.domain.model.PermissionRequest
import com.systemmonitor.features.intrusion.data.entity.IntrusionEventEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationManager @Inject constructor(
    private val requestNotification: PermissionRequestNotification,
    private val resultNotification: DownloadResultNotification,
    private val intrusionNotification: IntrusionNotification,
    private val telemetryNotification: SystemTelemetryNotification
) {
    fun sendPermissionRequestNotification(request: PermissionRequest) {
        requestNotification.showNotification(request)
    }

    fun sendDownloadSuccessNotification(requestId: String, filename: String, sizeMbText: String, sha256: String) {
        resultNotification.showSafeNotification(requestId, filename, sizeMbText, sha256)
    }

    fun sendDownloadQuarantinedNotification(requestId: String, filename: String, reason: String) {
        resultNotification.showQuarantinedNotification(requestId, filename, reason)
    }

    fun sendIntrusionNotification(event: IntrusionEventEntity) {
        intrusionNotification.showIntrusionNotification(event)
    }

    fun sendBatteryAlert(level: Int) {
        telemetryNotification.showBatteryAlert(level)
    }

    fun sendStorageAlert(percent: Int) {
        telemetryNotification.showStorageAlert(percent)
    }

    fun sendNetworkAlert(status: String) {
        telemetryNotification.showNetworkAlert(status)
    }
}
