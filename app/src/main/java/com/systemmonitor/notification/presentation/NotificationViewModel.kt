package com.systemmonitor.notification.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.systemmonitor.notification.data.NotificationDao
import com.systemmonitor.notification.data.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationCenterUiState(
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadCount: Int = 0
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationDao: NotificationDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationCenterUiState())
    val uiState: StateFlow<NotificationCenterUiState> = _uiState.asStateFlow()

    init {
        combine(
            notificationDao.getAllNotifications(),
            notificationDao.getUnreadNotifications()
        ) { all, unread ->
            NotificationCenterUiState(
                notifications = all,
                unreadCount = unread.size
            )
        }.onEach { _uiState.value = it }.launchIn(viewModelScope)
    }

    fun markAsRead(id: String) {
        viewModelScope.launch {
            notificationDao.markAsRead(id)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            notificationDao.markAllAsRead()
        }
    }

    fun deleteNotification(id: String) {
        viewModelScope.launch {
            notificationDao.deleteNotification(id)
        }
    }

    fun clearAll() {
        viewModelScope.launch {
            notificationDao.clearAllNotifications()
        }
    }
}
