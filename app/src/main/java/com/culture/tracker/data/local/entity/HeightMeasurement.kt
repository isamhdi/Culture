package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "height_measurements",
    foreignKeys = [
        ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("plantId")],
)
data class HeightMeasurement(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val date: LocalDate,
    val heightCm: Double,
)
