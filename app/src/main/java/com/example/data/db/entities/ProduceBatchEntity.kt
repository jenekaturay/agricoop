package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "produce_batches")
data class ProduceBatchEntity(
    @PrimaryKey
    val id: String,
    val batchCode: String, // e.g., BATCH-NIM-2026-001
    val farmerId: String,
    val farmerName: String,
    val cooperativeName: String,
    val cropType: String, // 'CASSAVA', 'YAM', 'SWEET_POTATO'
    val weightKg: Double,
    val starchPercentage: Double,
    val moisturePercentage: Double,
    val pricePerKgLrd: Double,
    val totalPayoutLrd: Double,
    val latitude: Double,
    val longitude: Double,
    val locationName: String,
    val payoutStatus: String, // 'PENDING', 'PROCESSING', 'PAID', 'FAILED'
    val momoTransactionRef: String,
    val isSynced: Boolean,
    val timestamp: Long
)
