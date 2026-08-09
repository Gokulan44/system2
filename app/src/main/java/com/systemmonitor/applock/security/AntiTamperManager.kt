package com.systemmonitor.applock.security

import android.app.admin.DevicePolicyManager
import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AntiTamperManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isDeviceAdminActive(): Boolean {
        val dpm = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
        return dpm?.activeAdmins?.any { it.packageName == context.packageName } == true
    }
}
