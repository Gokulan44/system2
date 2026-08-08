package com.systemmonitor.core

import android.content.Context
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generates and persists a stable per-install device ID (not tied to
 * hardware identifiers like IMEI, which require special permissions and
 * are unnecessary here — this only needs to be unique per install).
 */
@Singleton
class DeviceIdManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs by lazy {
        context.getSharedPreferences("device_identity", Context.MODE_PRIVATE)
    }

    fun getDeviceId(): String {
        prefs.getString(KEY_DEVICE_ID, null)?.let { return it }
        val newId = UUID.randomUUID().toString()
        prefs.edit { putString(KEY_DEVICE_ID, newId) }
        return newId
    }

    companion object {
        private const val KEY_DEVICE_ID = "device_id"
    }
}
