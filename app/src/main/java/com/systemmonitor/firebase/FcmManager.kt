package com.systemmonitor.firebase

import android.content.Context
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FcmManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val messaging: FirebaseMessaging
        get() = FirebaseMessaging.getInstance()

    suspend fun getFcmToken(): String? {
        return try {
            messaging.token.await()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun subscribeToTopic(topic: String): Boolean {
        return try {
            messaging.subscribeToTopic(topic).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun unsubscribeFromTopic(topic: String): Boolean {
        return try {
            messaging.unsubscribeFromTopic(topic).await()
            true
        } catch (e: Exception) {
            false
        }
    }
}
