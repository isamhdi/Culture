package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(
    tableName = "plant_photos",
    foreignKeys = [
        ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("plantId")],
)
data class PlantPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val filePath: String,
    val takenAt: LocalDateTime,
    val caption: String? = null,
)
