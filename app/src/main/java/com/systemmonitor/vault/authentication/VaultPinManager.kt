package com.systemmonitor.vault.authentication

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VaultPinManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val lockoutManager: LockoutManager,
    private val wipeManager: VaultWipeManager
) {
    private val prefs = context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val PBKDF2_ALGORITHM = "PBKDF2WithHmacSHA256"
        private const val ITERATIONS = 120_000
        private const val KEY_LENGTH_BITS = 256
        private const val SALT_SIZE_BYTES = 16

        private const val KEY_PIN_HASH = "vault_pin_hash"      // PBKDF2 hex digest (current format)
        private const val KEY_PIN_SALT = "vault_pin_salt"      // hex salt, present only for PBKDF2 entries
        private const val KEY_PIN_HASH_LEGACY = "vault_pin_hash_legacy" // old unsalted SHA-256 hex digest
    }

    fun isPinSetup(): Boolean {
        return prefs.contains(KEY_PIN_HASH) || prefs.contains(KEY_PIN_HASH_LEGACY)
    }

    fun setupPin(pin: String): Boolean {
        if (pin.length < 4) return false
        storePinPbkdf2(pin)
        lockoutManager.resetAttempts()
        return true
    }

    suspend fun authenticate(pin: String): AuthenticationResult {
        if (lockoutManager.isLockedOut()) {
            return AuthenticationResult.LockedOut(lockoutManager.getRemainingCooldownMs())
        }

        val matches: Boolean = when {
            prefs.contains(KEY_PIN_HASH) -> authenticateCurrentFormat(pin)
            prefs.contains(KEY_PIN_HASH_LEGACY) -> {
                val legacyMatch = authenticateLegacyFormat(pin)
                if (legacyMatch) {
                    // Silent migration: upgrade this user's stored credential to
                    // PBKDF2 + fresh salt now that we've verified the PIN via the
                    // old scheme. Runs once, on their next successful unlock.
                    storePinPbkdf2(pin)
                    prefs.edit().remove(KEY_PIN_HASH_LEGACY).apply()
                }
                legacyMatch
            }
            else -> return AuthenticationResult.Error("No PIN configured")
        }

        if (matches) {
            lockoutManager.resetAttempts()
            return AuthenticationResult.Success
        }

        return when (val failure = lockoutManager.recordFailedAttempt()) {
            is FailedAttemptResult.WipeTriggered -> {
                // Point of no return — the vault and all its credentials are
                // about to be destroyed. Run it and report a distinct result
                // so the UI can show a clear "vault wiped" message instead
                // of a normal invalid-PIN error.
                wipeManager.wipeVault()
                AuthenticationResult.VaultWiped(
                    "Too many failed attempts. The vault has been permanently wiped for security."
                )
            }
            is FailedAttemptResult.LockedOut ->
                AuthenticationResult.LockedOut(failure.cooldownMs)
            is FailedAttemptResult.AttemptsRemaining ->
                AuthenticationResult.InvalidCredentials(failure.remaining)
        }
    }

    // ---- current format (PBKDF2 + salt) ----

    private fun storePinPbkdf2(pin: String) {
        val salt = generateSalt()
        val hash = hashPinPbkdf2(pin, salt)
        prefs.edit()
            .putString(KEY_PIN_HASH, hash)
            .putString(KEY_PIN_SALT, salt.toHex())
            .apply()
    }

    private fun authenticateCurrentFormat(pin: String): Boolean {
        val savedHash = prefs.getString(KEY_PIN_HASH, null) ?: return false
        val saltHex = prefs.getString(KEY_PIN_SALT, null) ?: return false
        val hash = hashPinPbkdf2(pin, saltHex.fromHex())
        return MessageDigest.isEqual(
            hash.toByteArray(Charsets.US_ASCII),
            savedHash.toByteArray(Charsets.US_ASCII)
        )
    }

    private fun hashPinPbkdf2(pin: String, salt: ByteArray): String {
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH_BITS)
        val factory = SecretKeyFactory.getInstance(PBKDF2_ALGORITHM)
        val hashBytes = factory.generateSecret(spec).encoded
        return hashBytes.toHex()
    }

    // ---- legacy format (unsalted single-round SHA-256) — read-only, for migration ----

    private fun authenticateLegacyFormat(pin: String): Boolean {
        val savedHash = prefs.getString(KEY_PIN_HASH_LEGACY, null) ?: return false
        val hash = hashPinLegacySha256(pin)
        return MessageDigest.isEqual(
            hash.toByteArray(Charsets.US_ASCII),
            savedHash.toByteArray(Charsets.US_ASCII)
        )
    }

    private fun hashPinLegacySha256(pin: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(pin.toByteArray())
        return digest.toHex()
    }

    // ---- helpers ----

    private fun generateSalt(): ByteArray {
        val salt = ByteArray(SALT_SIZE_BYTES)
        SecureRandom().nextBytes(salt)
        return salt
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun String.fromHex(): ByteArray =
        chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}