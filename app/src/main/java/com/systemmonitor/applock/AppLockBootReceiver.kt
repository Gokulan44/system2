package com.systemmonitor.applock

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AppLockBootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            context?.let { ctx ->
                val prefs = com.systemmonitor.applock.settings.AppLockPreferences(ctx)
                if (prefs.getSettings().startAfterReboot) {
                    val serviceIntent = Intent(ctx, AppLockService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        ctx.startForegroundService(serviceIntent)
                    } else {
                        ctx.startService(serviceIntent)
                    }
                }
            }
        }
    }
} 
