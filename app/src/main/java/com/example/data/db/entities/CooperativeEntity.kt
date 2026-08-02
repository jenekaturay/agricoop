package com.example.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cooperatives")
data class CooperativeEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val county: String, // 'Nimba' or 'Lofa'
    val district: String,
    val leadPerson: String,
    val phone: String,
    val memberCount: Int
)
