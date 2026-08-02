package com.example.security

import android.content.Context
import android.util.Log
import com.example.data.db.AppDatabase
import com.example.data.db.entities.AuditLogEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

/**
 * Secure Audit Logger for capturing all sensitive application actions
 * (batch deletions, database exports, payment authorizations, biometric events)
 * into an encrypted local Room database table (`audit_logs`) secured with SQLCipher 256-bit AES.
 */
object SecureAuditLogger {

    private const val TAG = "SecureAuditLogger"
    private val loggerScope = CoroutineScope(Dispatchers.IO)

    // Current logged-in staff identifier
    var currentUserId: String = "STAFF_JEN_8841"

    /**
     * Captures a sensitive action with a timestamped user ID and records it to the local encrypted Room database.
     */
    fun recordAction(
        context: Context,
        action: String,
        category: String,
        detail: String,
        userId: String = currentUserId
    ) {
        val hwFingerprint = HardwareBinder.getHardwareFingerprint(context)
        val entity = AuditLogEntity(
            userId = userId,
            action = action,
            category = category,
            detail = detail,
            deviceFingerprint = hwFingerprint
        )

        loggerScope.launch {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                db.auditLogDao().insertAuditLog(entity)
                Log.d(TAG, "Recorded sensitive audit log in encrypted DB: [$category] $action - $detail (User: $userId)")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to write audit log to encrypted Room database", e)
            }
        }
    }

    /**
     * Returns a reactive Flow of all recorded audit logs ordered by timestamp descending.
     */
    fun getAllAuditLogsFlow(context: Context): Flow<List<AuditLogEntity>> {
        return AppDatabase.getDatabase(context.applicationContext)
            .auditLogDao()
            .getAllAuditLogsFlow()
            .flowOn(Dispatchers.IO)
    }

    /**
     * Returns recent audit logs for synchronous/coroutine reporting.
     */
    suspend fun getRecentAuditLogs(context: Context, limit: Int = 100): List<AuditLogEntity> {
        return try {
            AppDatabase.getDatabase(context.applicationContext)
                .auditLogDao()
                .getRecentAuditLogs(limit)
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching audit logs from encrypted DB", e)
            emptyList()
        }
    }

    /**
     * Seeds initial realistic audit log entries if the audit_logs table is empty.
     */
    fun seedInitialAuditLogsIfEmpty(context: Context) {
        loggerScope.launch {
            try {
                val db = AppDatabase.getDatabase(context.applicationContext)
                val existing = db.auditLogDao().getRecentAuditLogs(1)
                if (existing.isEmpty()) {
                    val now = System.currentTimeMillis()
                    val hw = HardwareBinder.getHardwareFingerprint(context)
                    val initialLogs = listOf(
                        AuditLogEntity(
                            timestamp = now - (3600000 * 48),
                            userId = "SYSTEM_SECURITY_VAULT",
                            action = "SQLCIPHER_VAULT_INITIALIZED",
                            category = "CRYPTOGRAPHIC_SECURITY",
                            detail = "SQLCipher 256-bit AES-CBC local database passphrase created & hardware key bound",
                            deviceFingerprint = hw
                        ),
                        AuditLogEntity(
                            timestamp = now - (3600000 * 24),
                            userId = "COOP_MGR_THERESA",
                            action = "BATCH_DELETED",
                            category = "SENSITIVE_DATA_MUTATION",
                            detail = "Deleted corrupted batch BATCH-NIM-2026-008 from local database",
                            deviceFingerprint = hw
                        ),
                        AuditLogEntity(
                            timestamp = now - (3600000 * 12),
                            userId = "STAFF_JEN_8841",
                            action = "DATABASE_EXPORTED",
                            category = "COMPLIANCE_EXPORT",
                            detail = "Exported 5 local Room DB produce records to CSV audit spreadsheet",
                            deviceFingerprint = hw
                        ),
                        AuditLogEntity(
                            timestamp = now - (3600000 * 3),
                            userId = "AGENT_LOFA_88",
                            action = "MOMO_PAYOUT_INITIATED",
                            category = "FINANCIAL_TRANSACTION",
                            detail = "Initiated LRD 61,200 payout for batch BATCH-LOF-2026-014",
                            deviceFingerprint = hw
                        ),
                        AuditLogEntity(
                            timestamp = now - 1800000,
                            userId = "STAFF_JEN_8841",
                            action = "BIOMETRIC_AUTH_VERIFIED",
                            category = "AUTHENTICATION",
                            detail = "Co-op staff fingerprint scan verified successfully",
                            deviceFingerprint = hw
                        )
                    )
                    db.auditLogDao().insertAuditLogs(initialLogs)
                    Log.d(TAG, "Seeded initial audit logs into encrypted Room DB")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to seed initial audit logs", e)
            }
        }
    }
}
