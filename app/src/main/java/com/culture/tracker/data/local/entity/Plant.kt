package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.domain.model.PropagationType
import java.time.LocalDate

@Entity(
    tableName = "plants",
    foreignKeys = [
        ForeignKey(entity = Genetics::class, parentColumns = ["id"], childColumns = ["geneticsId"], onDelete = ForeignKey.SET_NULL),
        ForeignKey(entity = Environment::class, parentColumns = ["id"], childColumns = ["environmentId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("geneticsId"), Index("environmentId")],
)
data class Plant(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val propagationType: PropagationType,
    val geneticsId: Long?,
    val environmentId: Long?,
    val currentPhase: GrowthPhase,
    val startDate: LocalDate,
    val wateringIntervalDays: Int? = null,
    val fertilizingIntervalDays: Int? = null,
    val archived: Boolean = false,
)
