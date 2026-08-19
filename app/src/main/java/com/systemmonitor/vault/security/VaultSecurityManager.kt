package com.systemmonitor.vault.security

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultSecurityManager @Inject constructor(
    val screenshotProtection: ScreenshotProtection,
    val clipboardProtection: ClipboardProtection,
    val screenLockManager: ScreenLockManager,
    val fileIntegrityManager: FileIntegrityManager
)
