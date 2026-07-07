package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "genetics")
data class Genetics(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val breeder: String? = null,
    val notes: String? = null,
    // Durées de phase (en jours) spécifiques à cette variété ; null = utiliser la durée par défaut.
    val germinationDays: Int? = null,
    val croissanceDays: Int? = null,
    val floraisonDays: Int? = null,
    val sechageDays: Int? = null,
    val maturationDays: Int? = null,
)
