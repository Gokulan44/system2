package com.systemmonitor.features.intrusion.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.features.intrusion.data.entity.IntrusionEventEntity
import com.systemmonitor.features.intrusion.data.repository.IntrusionRepository
import com.systemmonitor.features.intrusion.security.PhotoDecryptor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class IntrusionViewModel @Inject constructor(
    private val intrusionRepository: IntrusionRepository,
    private val photoDecryptor: PhotoDecryptor
) : ViewModel() {

    // Expose all intrusion events in descending order of time
    val events: StateFlow<List<IntrusionEventEntity>> = intrusionRepository.allEvents
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Expose unread count badge state
    val unreadCount: StateFlow<Int> = intrusionRepository.unreadCount
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    fun markAsRead(eventId: String) {
        viewModelScope.launch {
            intrusionRepository.markAsRead(eventId)
        }
    }

    fun deleteEvent(eventId: String) {
        viewModelScope.launch {
            intrusionRepository.deleteEvent(eventId)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            intrusionRepository.clearAllEvents()
        }
    }

    /**
     * Helper to decrypt the encrypted base64 payload and return the unencrypted Bitmap.
     */
    fun decryptIntruderPhoto(encryptedB64: String?, expectedHash: String?): Bitmap? {
        if (encryptedB64.isNullOrEmpty()) return null
        return photoDecryptor.decryptAndVerify(encryptedB64, expectedHash)
    }
}
