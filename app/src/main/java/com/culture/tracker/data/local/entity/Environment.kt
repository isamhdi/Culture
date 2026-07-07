package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "environments")
data class Environment(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val lightHoursPerDay: Double,
    val sizeDescription: String? = null,
    val materialDescription: String? = null,
    val lightingType: String? = null,
    val lightingPowerWatts: Int? = null,
    val lightingSpectrum: String? = null,
    val lightingModel: String? = null,
)
