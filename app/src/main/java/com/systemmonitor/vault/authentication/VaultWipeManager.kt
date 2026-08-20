package com.systemmonitor.vault.authentication

import android.content.Context
import android.util.Log
import com.systemmonitor.vault.database.VaultFileDao
import com.systemmonitor.vault.encryption.VaultKeyManager
import com.systemmonitor.vault.storage.VaultPathManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Coordinates a full, irreversible panic wipe of the vault.
 *
 * IMPORTANT ORDERING: files must be considered gone the moment the master
 * key is destroyed (they become permanently undecryptable ciphertext at
 * that instant), so we treat key destruction as the point of no return and
 * do it FIRST. Everything after that is just tidying up now-useless data —
 * if any of those later steps fail partway through, the vault is still
 * safely and irreversibly wiped from the user's perspective, just with
 * some leftover garbage on disk/DB that a future cleanup pass can catch.
 *
 * Doing it in the opposite order (delete files first, destroy key last)
 * would leave a window where a crash mid-wipe could leave the key intact
 * with only some files deleted — the least safe possible partial state
 * for a "panic wipe" feature to fail into.
 */
@Singleton
class VaultWipeManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val keyManager: VaultKeyManager,
    private val pathManager: VaultPathManager,
    private val fileDao: VaultFileDao,
    private val lockoutManager: LockoutManager
) {
    companion object {
        private const val TAG = "VaultWipeManager"
    }

    /**
     * Performs the full wipe. Always "succeeds" from the caller's
     * perspective in the sense that the key is destroyed first and that
     * step is not allowed to silently fail — everything after is
     * best-effort cleanup, logged but not fatal.
     */
    suspend fun wipeVault() = withContext(Dispatchers.IO) {
        // 1. Point of no return: destroy the master key. Every encrypted
        // file in the vault is now permanently unreadable.
        keyManager.resetMasterKey()
        Log.w(TAG, "Master key destroyed — vault wipe in progress")

        // 2. Best-effort cleanup below. None of these failing changes the
        // outcome (the vault is already unrecoverable), but skipping them
        // would leave orphaned ciphertext files and DB rows pointing at
        // data that can never be opened again.

        try {
            val deletedAny = pathManager.encryptedDir.listFiles()?.all { it.delete() } ?: true
            if (!deletedAny) {
                Log.w(TAG, "Some encrypted files could not be deleted from disk (now-inert ciphertext left behind)")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear encrypted file directory", e)
        }

        try {
            pathManager.tempDir.listFiles()?.forEach { it.delete() }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear temp directory", e)
        }

        try {
            fileDao.deleteAllFiles()
        } catch (e: Exception) {
            // NOTE: VaultFileDao needs a `deleteAllFiles()` @Query/@Delete
            // method if it doesn't already have one (e.g.
            // "DELETE FROM vault_files"). Without it this call won't
            // compile — add it alongside your other VaultFileDao methods.
            Log.w(TAG, "Failed to clear vault_files table", e)
        }

        // 3. Clear all authentication credentials, not just the PIN — a
        // panic wipe should force full re-setup, not leave a pattern or
        // password still valid for an empty vault.
        try {
            context.getSharedPreferences("secure_vault_prefs", Context.MODE_PRIVATE)
                .edit().clear().apply()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to clear auth credential prefs", e)
        }

        // 4. Reset lockout/attempt counters last, so a crash earlier in
        // this function doesn't erase evidence that a wipe was in progress
        // (harmless either way, but keeps behavior predictable on retry).
        lockoutManager.resetAttempts()

        Log.w(TAG, "Vault wipe complete")
    }
}
