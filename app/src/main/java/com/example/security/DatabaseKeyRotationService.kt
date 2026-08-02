package com.example.security

import android.content.Context
import android.util.Base64
import android.util.Log
import com.example.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class KeyRotationResult(
    val isSuccess: Boolean,
    val oldVersion: Int,
    val newVersion: Int,
    val timestamp: Long,
    val message: String
)

data class KeyRotationStatus(
    val currentVersion: Int,
    val lastRotationTimestamp: Long,
    val rotationIntervalDays: Int,
    val daysRemaining: Int,
    val isDueForRotation: Boolean,
    val totalRotationsPerformed: Int,
    val formattedLastRotationDate: String
)

/**
 * Service managing periodic and on-demand SQLCipher database key rotation.
 * Re-encrypts the Room local database in-place via SQLCipher PRAGMA rekey
 * without data loss, updating the secure vault and recording audit events.
 */
object DatabaseKeyRotationService {

    private const val TAG = "KeyRotationService"

    /**
     * Gets the current rotation status & schedule metrics for UI and security telemetry.
     */
    fun getRotationStatus(context: Context): KeyRotationStatus {
        val currentVersion = DatabaseEncryptionManager.getKeyVersion(context)
        val lastTs = DatabaseEncryptionManager.getLastRotationTimestamp(context)
        val intervalDays = DatabaseEncryptionManager.getRotationIntervalDays(context)
        val rotationCount = DatabaseEncryptionManager.getRotationCount(context)

        val elapsedMs = System.currentTimeMillis() - lastTs
        val elapsedDays = TimeUnit.MILLISECONDS.toDays(elapsedMs).toInt()
        val daysRemaining = (intervalDays - elapsedDays).coerceAtLeast(0)
        val isDue = elapsedDays >= intervalDays

        val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US)
        val formattedDate = dateFormat.format(Date(lastTs))

        return KeyRotationStatus(
            currentVersion = currentVersion,
            lastRotationTimestamp = lastTs,
            rotationIntervalDays = intervalDays,
            daysRemaining = daysRemaining,
            isDueForRotation = isDue,
            totalRotationsPerformed = rotationCount,
            formattedLastRotationDate = formattedDate
        )
    }

    /**
     * Updates the periodic key rotation policy (e.g. 30, 60, 90 days).
     */
    fun setRotationIntervalDays(context: Context, days: Int) {
        DatabaseEncryptionManager.setRotationIntervalDays(context, days)
        Log.d(TAG, "Database key rotation interval updated to $days days")
    }

    /**
     * Executes in-place SQLCipher database key rotation and re-encryption without data loss.
     */
    suspend fun rotateDatabaseKey(
        context: Context,
        operatorUserId: String = "STAFF_SECURITY_ADMIN"
    ): KeyRotationResult = withContext(Dispatchers.IO) {
        val oldVersion = DatabaseEncryptionManager.getKeyVersion(context)
        Log.i(TAG, "Starting SQLCipher database key rotation from version $oldVersion...")

        try {
            // 1. Generate fresh 256-bit cryptographically secure key
            val newKeyBytes = ByteArray(32)
            SecureRandom().nextBytes(newKeyBytes)
            val newPassphraseBase64 = Base64.encodeToString(newKeyBytes, Base64.NO_WRAP)

            // 2. Obtain writable database connection from current AppDatabase instance
            val db = AppDatabase.getDatabase(context.applicationContext)
            val writableDb = db.openHelper.writableDatabase

            // 3. Re-key SQLCipher database pages using PRAGMA rekey
            val escapedPassphrase = newPassphraseBase64.replace("'", "''")
            writableDb.execSQL("PRAGMA rekey = '$escapedPassphrase';")

            // 4. Update passphrase and key metadata in secure vault shared preferences
            DatabaseEncryptionManager.updatePassphrase(context.applicationContext, newKeyBytes)
            val newVersion = DatabaseEncryptionManager.getKeyVersion(context.applicationContext)

            // 5. Close old connection and reset singleton instance
            AppDatabase.closeAndResetInstance()

            // 6. Verify accessibility with new key by re-opening database
            val newDbInstance = AppDatabase.getDatabase(context.applicationContext)
            val testQuery = newDbInstance.openHelper.readableDatabase.query("SELECT count(*) FROM sqlite_master;")
            testQuery.close()

            val successMsg = "SQLCipher database key successfully rotated from v$oldVersion to v$newVersion without data loss."
            Log.i(TAG, successMsg)

            // 7. Record secure audit log
            SecureAuditLogger.recordAction(
                context = context,
                action = "SQLCIPHER_KEY_ROTATED",
                category = "CRYPTOGRAPHIC_SECURITY",
                detail = "Database key rotated (v$oldVersion -> v$newVersion). SQLCipher 256-bit AES re-encryption verified.",
                userId = operatorUserId
            )

            KeyRotationResult(
                isSuccess = true,
                oldVersion = oldVersion,
                newVersion = newVersion,
                timestamp = System.currentTimeMillis(),
                message = successMsg
            )
        } catch (e: Exception) {
            val errorMsg = "SQLCipher key rotation failed: ${e.message}"
            Log.e(TAG, errorMsg, e)

            SecureAuditLogger.recordAction(
                context = context,
                action = "SQLCIPHER_KEY_ROTATION_FAILED",
                category = "CRYPTOGRAPHIC_SECURITY",
                detail = "Failed key rotation attempt for v$oldVersion: ${e.message}",
                userId = operatorUserId
            )

            KeyRotationResult(
                isSuccess = false,
                oldVersion = oldVersion,
                newVersion = oldVersion,
                timestamp = System.currentTimeMillis(),
                message = errorMsg
            )
        }
    }

    /**
     * Checks whether the key rotation policy expiration has been reached and auto-triggers rotation.
     */
    suspend fun checkAndRotateIfDue(context: Context): KeyRotationResult? {
        val status = getRotationStatus(context)
        return if (status.isDueForRotation) {
            Log.i(TAG, "Key rotation is due (${status.daysRemaining} days remaining). Auto-executing key rotation...")
            rotateDatabaseKey(context, operatorUserId = "AUTO_ROTATION_SERVICE")
        } else {
            null
        }
    }
}
