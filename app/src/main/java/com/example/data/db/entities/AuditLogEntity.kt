package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Encrypted Local Audit Log Table for tracking all sensitive actions
 * (batch deletions, database exports, biometric failures, payout triggers)
 * in the SQLCipher 256-bit AES Room database for internal security reviews.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val userId: String,
    val action: String,
    val category: String,
    val detail: String,
    val deviceFingerprint: String = ""
)
