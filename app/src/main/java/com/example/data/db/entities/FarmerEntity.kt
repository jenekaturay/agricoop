package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "farmers")
data class FarmerEntity(
    @PrimaryKey
    val id: String,
    val cooperativeId: String,
    val cooperativeName: String,
    val nationalId: String,
    val fullName: String,
    val phoneNumber: String,
    val momoNumber: String,
    val gender: String, // 'FEMALE', 'MALE', 'OTHER'
    val yearOfBirth: Int,
    val isYouth: Boolean,
    val seedCuttingsAllocated: Int, // TME 419 cuttings
    val totalBatchesDelivered: Int,
    val totalEarningsLrd: Double
)
