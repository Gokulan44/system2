package com.systemmonitor.core

object Constants {
    const val DATABASE_NAME = "system_monitor.db"

    // WorkManager unique work names
    const val WORK_BATTERY_MONITOR = "work_battery_monitor"
    const val WORK_FIREBASE_SYNC = "work_firebase_sync"

    // WorkManager intervals
    const val BATTERY_POLL_INTERVAL_MINUTES = 15L
    const val SYNC_INTERVAL_MINUTES = 60L

    // Firestore collection paths
    const val COLLECTION_USERS = "users"
    const val COLLECTION_DEVICES = "devices"
    const val COLLECTION_BATTERY_SUMMARIES = "battery_summaries"

    // Battery health thresholds (percent)
    const val BATTERY_LOW_THRESHOLD = 20
    const val BATTERY_CRITICAL_THRESHOLD = 10
    const val BATTERY_TEMP_WARNING_CELSIUS = 40.0
}
