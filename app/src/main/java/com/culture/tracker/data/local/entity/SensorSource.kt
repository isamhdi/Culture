package com.culture.tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.culture.tracker.domain.model.SensorSourceType
import java.time.LocalDateTime

/**
 * Capteur externe (Home Assistant ou webhook générique) attribué à un environnement.
 * Un seul par environnement. Ne fait jamais d'appel réseau seul : c'est [SensorRepository]
 * qui décide, en fonction du consentement utilisateur (AppSettings.networkSensorsEnabled),
 * si une requête peut réellement partir.
 */
@Entity(
    tableName = "sensor_sources",
    foreignKeys = [
        ForeignKey(entity = Environment::class, parentColumns = ["id"], childColumns = ["environmentId"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [Index("environmentId", unique = true)],
)
data class SensorSource(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val environmentId: Long,
    val type: SensorSourceType,
    val name: String,
    /** URL de base Home Assistant, ou URL complète du webhook selon [type]. */
    val baseUrl: String,
    val accessToken: String? = null,
    val temperatureEntityId: String? = null,
    val humidityEntityId: String? = null,
    val lastFetchAt: LocalDateTime? = null,
    val lastFetchSuccess: Boolean? = null,
    val lastError: String? = null,
)
