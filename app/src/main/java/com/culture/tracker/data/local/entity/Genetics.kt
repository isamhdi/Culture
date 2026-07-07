package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genetics")
data class Genetics(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breeder: String? = null,
    val notes: String? = null,
)
