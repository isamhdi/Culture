package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.culture.tracker.domain.model.EnvironmentMeasurementType
import java.time.LocalDate

/** Relevé libre sur un environnement : une note, une mesure typée (ex. CO2, VPD), ou les deux à la fois. */
@Entity(
    tableName = "environment_logs",
    foreignKeys = [
        ForeignKey(entity = Environment::class, parentColumns = ["id"], childColumns = ["environmentId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("environmentId")],
)
data class EnvironmentLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val environmentId: Long,
    val date: LocalDate,
    val note: String? = null,
    val measurementType: EnvironmentMeasurementType? = null,
    val measurementValue: Double? = null,
)
