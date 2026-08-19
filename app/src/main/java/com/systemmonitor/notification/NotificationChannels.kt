package com.systemmonitor.notification

object NotificationChannels {
    const val SECURITY_CHANNEL_ID = "security_alerts_channel"
    const val SECURITY_CHANNEL_NAME = "Security Alerts"
    const val SECURITY_CHANNEL_DESC = "Notifications for security threats and intrusion events."

    const val DEVICE_CHANNEL_ID = "device_status_channel"
    const val DEVICE_CHANNEL_NAME = "Device Status Updates"
    const val DEVICE_CHANNEL_DESC = "Notifications for connected laptops and device status."

    const val VAULT_CHANNEL_ID = "vault_security_channel"
    const val VAULT_CHANNEL_NAME = "Secure Vault Updates"
    const val VAULT_CHANNEL_DESC = "Notifications for secure vault status and integrity scans."

    const val APPLOCK_CHANNEL_ID = "applock_alerts_channel"
    const val APPLOCK_CHANNEL_NAME = "AppLock Alerts"
    const val APPLOCK_CHANNEL_NAME_SHORT = "AppLock"
    const val APPLOCK_CHANNEL_DESC = "Notifications for locked apps and unlock history."

    const val NETWORK_CHANNEL_ID = "network_alerts_channel"
    const val NETWORK_CHANNEL_NAME = "Network Alerts"
    const val NETWORK_CHANNEL_DESC = "Notifications for Wi-Fi and network security updates."
}
