package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "hub_operations")
data class HubOperationEntity(
    @PrimaryKey
    val id: String,
    val hubName: String,
    val county: String,
    val solarCapacityKw: Double,
    val flashDryerActive: Boolean,
    val dailyRawTons: Double,
    val hqcfYieldTons: Double,
    val industrialStarchTons: Double,
    val animalFeedTons: Double,
    val siftingMeshPassed: Boolean, // 100-mesh QA pass
    val moistureContentPct: Double, // Target < 10%
    val activeCargoTrikes: Int
)
