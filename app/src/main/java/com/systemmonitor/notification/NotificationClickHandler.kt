package com.systemmonitor.notification

import android.content.Context
import android.content.Intent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationClickHandler @Inject constructor() {
    fun handleNotificationClick(context: Context, actionData: String?): Intent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: Intent()
        actionData?.let {
            intent.putExtra("navigation_destination", it)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        return intent
    }
}
