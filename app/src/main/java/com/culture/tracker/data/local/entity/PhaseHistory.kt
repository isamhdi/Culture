package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.culture.tracker.domain.model.GrowthPhase
import java.time.LocalDate

@Entity(
    tableName = "phase_history",
    foreignKeys = [
        ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("plantId")],
)
data class PhaseHistory(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val phase: GrowthPhase,
    val startDate: LocalDate,
    val endDate: LocalDate? = null,
    val notes: String? = null,
)
