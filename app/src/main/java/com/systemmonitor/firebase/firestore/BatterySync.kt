package com.systemmonitor.firebase.firestore

import com.google.firebase.auth.FirebaseAuth
import com.systemmonitor.core.Constants
import com.systemmonitor.repository.BatterySummary
import javax.inject.Inject

/**
 * Deliberately syncs a rollup, not every raw reading — see the note in
 * BatteryRepository.getSummarySince(). Keeps Firestore writes cheap and
 * avoids leaking a minute-by-minute activity trace off-device.
 */
class BatterySync @Inject constructor(
    private val firestoreManager: FirestoreManager,
    private val auth: FirebaseAuth
) {
    suspend fun pushSummary(deviceId: String, summary: BatterySummary) {
        val uid = auth.currentUser?.uid ?: return
        val path = "${Constants.COLLECTION_USERS}/$uid/${Constants.COLLECTION_DEVICES}/$deviceId/${Constants.COLLECTION_BATTERY_SUMMARIES}"
        firestoreManager.addToCollection(
            collectionPath = path,
            data = mapOf(
                "periodStart" to summary.periodStart,
                "averageLevelPercent" to summary.averageLevelPercent,
                "averageTemperatureCelsius" to summary.averageTemperatureCelsius,
                "syncedAt" to System.currentTimeMillis()
            )
        )
    }
}
