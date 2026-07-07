package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fertilizers")
data class Fertilizer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val npk: String? = null,
    val notes: String? = null,
)
