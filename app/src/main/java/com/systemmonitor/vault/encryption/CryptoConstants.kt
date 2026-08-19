package com.systemmonitor.vault.encryption

object CryptoConstants {
    const val ANDROID_KEYSTORE = "AndroidKeyStore"
    const val MASTER_KEY_ALIAS = "SystemMonitorVaultMasterKey"
    const val AES_GCM_NOPADDING = "AES/GCM/NoPadding"
    const val IV_SIZE_BYTES = 12
    const val TAG_SIZE_BITS = 128
    const val KEY_SIZE_BITS = 256
    const val BUFFER_SIZE_BYTES = 8192
}
