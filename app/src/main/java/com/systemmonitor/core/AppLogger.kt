package com.systemmonitor.core

import android.util.Log
import com.systemmonitor.BuildConfig

/**
 * Thin wrapper around Log so we can strip logging in release builds
 * and later swap in Crashlytics/Timber without touching call sites.
 */
object AppLogger {

    private const val GLOBAL_TAG = "SystemMonitor"

    fun d(tag: String, message: String) {
        if (BuildConfig.DEBUG) Log.d("$GLOBAL_TAG:$tag", message)
    }

    fun i(tag: String, message: String) {
        Log.i("$GLOBAL_TAG:$tag", message)
    }

    fun w(tag: String, message: String, throwable: Throwable? = null) {
        Log.w("$GLOBAL_TAG:$tag", message, throwable)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$GLOBAL_TAG:$tag", message, throwable)
        // TODO: forward to Crashlytics once FirebaseModule wires it in
    }
}
