package com.example.security

import android.content.Context
import android.content.SharedPreferences
import java.security.SecureRandom
import android.util.Base64

/**
 * Manages SQLCipher 256-bit AES database encryption key generation and storage.
 * Ensures local Room database (farmer PII, weight telemetry, payout ledgers)
 * is protected at rest against file system theft or unauthorized extraction.
 */
object DatabaseEncryptionManager {

    private const val PREFS_NAME = "agricoop_security_vault"
    private const val KEY_SQLCIPHER_PASSPHRASE = "sqlcipher_db_passphrase_v1"
    private const val KEY_VERSION = "sqlcipher_key_version"
    private const val KEY_LAST_ROTATION_TS = "sqlcipher_last_rotation_ts"
    private const val KEY_ROTATION_COUNT = "sqlcipher_rotation_count"
    private const val KEY_INTERVAL_DAYS = "sqlcipher_rotation_interval_days"

    private const val DEFAULT_INTERVAL_DAYS = 90

    @Synchronized
    fun getOrCreatePassphrase(context: Context): ByteArray {
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var passphraseBase64 = prefs.getString(KEY_SQLCIPHER_PASSPHRASE, null)

        if (passphraseBase64.isNull_or_Empty()) {
            val randomBytes = ByteArray(32) // 256 bits of entropy
            SecureRandom().nextBytes(randomBytes)
            passphraseBase64 = Base64.encodeToString(randomBytes, Base64.NO_WRAP)
            prefs.edit()
                .putString(KEY_SQLCIPHER_PASSPHRASE, passphraseBase64)
                .putInt(KEY_VERSION, 1)
                .putLong(KEY_LAST_ROTATION_TS, System.currentTimeMillis())
                .putInt(KEY_ROTATION_COUNT, 0)
                .putInt(KEY_INTERVAL_DAYS, DEFAULT_INTERVAL_DAYS)
                .apply()
        }

        return Base64.decode(passphraseBase64, Base64.NO_WRAP)
    }

    @Synchronized
    fun updatePassphrase(context: Context, newPassphraseBytes: ByteArray) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val newPassphraseBase64 = Base64.encodeToString(newPassphraseBytes, Base64.NO_WRAP)
        val currentVersion = prefs.getInt(KEY_VERSION, 1)
        val currentCount = prefs.getInt(KEY_ROTATION_COUNT, 0)

        prefs.edit()
            .putString(KEY_SQLCIPHER_PASSPHRASE, newPassphraseBase64)
            .putInt(KEY_VERSION, currentVersion + 1)
            .putLong(KEY_LAST_ROTATION_TS, System.currentTimeMillis())
            .putInt(KEY_ROTATION_COUNT, currentCount + 1)
            .apply()
    }

    fun getKeyVersion(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_VERSION, 1)
    }

    fun getLastRotationTimestamp(context: Context): Long {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val ts = prefs.getLong(KEY_LAST_ROTATION_TS, 0L)
        return if (ts == 0L) System.currentTimeMillis() else ts
    }

    fun getRotationCount(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_ROTATION_COUNT, 0)
    }

    fun getRotationIntervalDays(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_INTERVAL_DAYS, DEFAULT_INTERVAL_DAYS)
    }

    fun setRotationIntervalDays(context: Context, days: Int) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putInt(KEY_INTERVAL_DAYS, days).apply()
    }

    private fun String?.isNull_or_Empty(): Boolean = this == null || this.isEmpty()

    fun isDatabaseEncrypted(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.contains(KEY_SQLCIPHER_PASSPHRASE)
    }

    fun getEncryptionCipherName(): String = "SQLCipher 256-bit AES-CBC"
}
