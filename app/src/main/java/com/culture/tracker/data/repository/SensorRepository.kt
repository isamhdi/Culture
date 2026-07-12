package com.culture.tracker.data.repository

import com.culture.tracker.data.local.dao.SensorSourceDao
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.local.entity.SensorSource
import com.culture.tracker.data.network.SensorFetchResult
import com.culture.tracker.data.network.SensorReadingFetcher
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class SensorRepository(
    private val sensorSourceDao: SensorSourceDao,
    private val calendarRepository: CalendarRepository,
    private val settingsRepository: SettingsRepository,
) {
    fun observeForEnvironment(environmentId: Long): Flow<SensorSource?> = sensorSourceDao.observeForEnvironment(environmentId)

    suspend fun saveSource(source: SensorSource): Long = sensorSourceDao.upsert(source)

    suspend fun deleteSource(source: SensorSource) = sensorSourceDao.delete(source)

    /**
     * Relève un capteur maintenant (bouton "Tester"/"Relever" ou worker périodique).
     * Un relevé n'est enregistré dans l'historique de l'environnement que si température ET
     * humidité ont pu être lues (le modèle de données existant exige les deux) ; si un seul
     * des deux a répondu, c'est signalé dans [SensorSource.lastError] sans rien enregistrer.
     */
    suspend fun fetchNow(source: SensorSource): Result<SensorFetchResult> {
        if (!settingsRepository.settings.first().networkSensorsEnabled) {
            return Result.failure(IllegalStateException("Les capteurs externes ne sont pas activés dans les Réglages."))
        }
        val now = LocalDateTime.now()
        val result = SensorReadingFetcher.fetch(source)
        result.fold(
            onSuccess = { fetched ->
                val bothPresent = fetched.temperatureCelsius != null && fetched.humidityPercent != null
                val error = when {
                    fetched.partialError != null -> fetched.partialError
                    !bothPresent -> "Relevé partiel (température ou humidité manquante) — rien enregistré."
                    else -> null
                }
                sensorSourceDao.update(source.copy(lastFetchAt = now, lastFetchSuccess = bothPresent, lastError = error))
                if (bothPresent) {
                    calendarRepository.recordReading(
                        EnvironmentReading(
                            environmentId = source.environmentId,
                            recordedAt = now,
                            temperatureCelsius = fetched.temperatureCelsius!!,
                            humidityPercent = fetched.humidityPercent!!,
                        ),
                    )
                }
            },
            onFailure = { error ->
                sensorSourceDao.update(source.copy(lastFetchAt = now, lastFetchSuccess = false, lastError = error.message ?: "Erreur inconnue"))
            },
        )
        return result
    }

    /** Relève tous les capteurs configurés ; appelé par le worker périodique. Ne fait rien si le consentement réseau est désactivé. */
    suspend fun fetchAll() {
        if (!settingsRepository.settings.first().networkSensorsEnabled) return
        sensorSourceDao.getAll().forEach { fetchNow(it) }
    }
}
