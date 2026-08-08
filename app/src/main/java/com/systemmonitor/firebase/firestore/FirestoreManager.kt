package com.systemmonitor.firebase.firestore

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Generic Firestore write helper. Feature-specific sync classes (BatterySync,
 * SecuritySync, ...) depend on this rather than touching FirebaseFirestore
 * directly, so retry/backoff/offline behavior lives in one place.
 */
@Singleton
class FirestoreManager @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    suspend fun setDocument(collectionPath: String, documentId: String, data: Map<String, Any?>) {
        firestore.collection(collectionPath).document(documentId).set(data).await()
    }

    suspend fun addToCollection(collectionPath: String, data: Map<String, Any?>): String {
        val ref = firestore.collection(collectionPath).add(data).await()
        return ref.id
    }
}
