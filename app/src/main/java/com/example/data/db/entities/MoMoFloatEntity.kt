package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "momo_floats")
data class MoMoFloatEntity(
    @PrimaryKey
    val id: String,
    val hubLocation: String, // 'Ganta Regional Hub' or 'Voinjama Main Agent Node'
    val orangeMoMoFloatLrd: Double,
    val mtnMoMoFloatLrd: Double,
    val isSufficientFloat: Boolean,
    val lastRefreshedTime: Long
)
