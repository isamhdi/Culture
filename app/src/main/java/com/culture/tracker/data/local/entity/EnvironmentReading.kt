package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "environment_readings",
    foreignKeys = [
        ForeignKey(entity = Environment::class, parentColumns = ["id"], childColumns = ["environmentId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("environmentId"), Index("recordedAt")],
)
data class EnvironmentReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val environmentId: Long,
    val recordedAt: LocalDateTime,
    val temperatureCelsius: Double,
    val humidityPercent: Double,
)
