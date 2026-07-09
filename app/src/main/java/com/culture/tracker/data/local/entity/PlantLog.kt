package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.culture.tracker.domain.model.PlantMeasurementType
import java.time.LocalDate

/** Relevé libre sur une plante : une note, une mesure typée (ex. pH, EC), ou les deux à la fois. */
@Entity(
    tableName = "plant_logs",
    foreignKeys = [
        ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("plantId")],
)
data class PlantLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val date: LocalDate,
    val note: String? = null,
    val measurementType: PlantMeasurementType? = null,
    val measurementValue: Double? = null,
)
