package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.culture.tracker.domain.model.ActionType
import java.time.LocalDate

@Entity(
    tableName = "calendar_actions",
    foreignKeys = [
        ForeignKey(entity = Plant::class, parentColumns = ["id"], childColumns = ["plantId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Fertilizer::class, parentColumns = ["id"], childColumns = ["fertilizerId"], onDelete = ForeignKey.SET_NULL),
    ],
    indices = [Index("plantId"), Index("fertilizerId"), Index("date")],
)
data class CalendarAction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val plantId: Long,
    val actionType: ActionType,
    val date: LocalDate,
    val fertilizerId: Long? = null,
    val notes: String? = null,
)
