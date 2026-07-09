package com.culture.tracker.data.repository

import com.culture.tracker.data.local.dao.EnvironmentDao
import com.culture.tracker.data.local.dao.EnvironmentLogDao
import com.culture.tracker.data.local.dao.GeneticsDao
import com.culture.tracker.data.local.dao.HeightMeasurementDao
import com.culture.tracker.data.local.dao.PhaseHistoryDao
import com.culture.tracker.data.local.dao.PlantDao
import com.culture.tracker.data.local.dao.PlantLogDao
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentLog
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.HeightMeasurement
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.local.entity.PlantLog
import com.culture.tracker.domain.model.GrowthPhase
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class GardenRepository(
    private val plantDao: PlantDao,
    private val geneticsDao: GeneticsDao,
    private val environmentDao: EnvironmentDao,
    private val phaseHistoryDao: PhaseHistoryDao,
    private val heightMeasurementDao: HeightMeasurementDao,
    private val plantLogDao: PlantLogDao,
    private val environmentLogDao: EnvironmentLogDao,
) {
    fun observePlants(): Flow<List<Plant>> = plantDao.observeActive()
    fun observeArchivedPlants(): Flow<List<Plant>> = plantDao.observeArchived()
    fun observePlant(id: Long): Flow<Plant?> = plantDao.observeById(id)
    fun observeGenetics(): Flow<List<Genetics>> = geneticsDao.observeAll()
    fun observeEnvironments(): Flow<List<Environment>> = environmentDao.observeAll()
    fun observeEnvironment(id: Long): Flow<Environment?> = environmentDao.observeById(id)
    fun observePhaseHistory(plantId: Long): Flow<List<PhaseHistory>> = phaseHistoryDao.observeForPlant(plantId)
    fun observeAllOpenPhases(): Flow<List<PhaseHistory>> = phaseHistoryDao.observeAllOpenPhases()
    fun observeHeightHistory(plantId: Long): Flow<List<HeightMeasurement>> = heightMeasurementDao.observeForPlant(plantId)
    fun observeLatestHeights(): Flow<List<HeightMeasurement>> = heightMeasurementDao.observeLatestPerPlant()

    suspend fun plantsWithWateringSchedule(): List<Plant> = plantDao.getPlantsWithWateringSchedule()
    suspend fun plantsWithFertilizingSchedule(): List<Plant> = plantDao.getPlantsWithFertilizingSchedule()

    suspend fun createGenetics(genetics: Genetics): Long = geneticsDao.upsert(genetics)
    suspend fun updateGenetics(genetics: Genetics) = geneticsDao.update(genetics)
    suspend fun deleteGenetics(genetics: Genetics) = geneticsDao.delete(genetics)

    /** Insère les variétés de référence si la table est vide (premier lancement). */
    suspend fun seedDefaultGeneticsIfEmpty(seeds: List<Genetics>) {
        if (geneticsDao.count() == 0) {
            seeds.forEach { geneticsDao.upsert(it) }
        }
    }

    suspend fun createEnvironment(environment: Environment): Long = environmentDao.upsert(environment)
    suspend fun updateEnvironment(environment: Environment) = environmentDao.update(environment)
    suspend fun deleteEnvironment(environment: Environment) = environmentDao.delete(environment)

    suspend fun createPlant(plant: Plant, initialHeightCm: Double? = null): Long {
        val plantId = plantDao.upsert(plant)
        phaseHistoryDao.upsert(
            PhaseHistory(plantId = plantId, phase = plant.currentPhase, startDate = plant.startDate),
        )
        if (initialHeightCm != null) {
            heightMeasurementDao.upsert(HeightMeasurement(plantId = plantId, date = plant.startDate, heightCm = initialHeightCm))
        }
        return plantId
    }

    suspend fun updatePlant(plant: Plant) = plantDao.update(plant)
    suspend fun archivePlant(plant: Plant) = plantDao.update(plant.copy(archived = true))
    suspend fun unarchivePlant(plant: Plant) = plantDao.update(plant.copy(archived = false))

    suspend fun addHeightMeasurement(plantId: Long, date: LocalDate, heightCm: Double) {
        heightMeasurementDao.upsert(HeightMeasurement(plantId = plantId, date = date, heightCm = heightCm))
    }

    /** Change de phase, en clôturant la précédente et en autorisant une date rétroactive. */
    suspend fun changePhase(plantId: Long, newPhase: GrowthPhase, effectiveDate: LocalDate) {
        val history = phaseHistoryDao.getForPlant(plantId)
        val openPhase = history.lastOrNull { it.endDate == null }
        if (openPhase != null) {
            phaseHistoryDao.update(openPhase.copy(endDate = effectiveDate))
        }
        phaseHistoryDao.upsert(PhaseHistory(plantId = plantId, phase = newPhase, startDate = effectiveDate))
        plantDao.getById(plantId)?.let { plantDao.update(it.copy(currentPhase = newPhase)) }
    }

    /**
     * Permet de corriger rétroactivement la date de début d'une phase existante.
     * Si c'est la toute première phase (germination), la date de début de la plante
     * est synchronisée pour que le compteur de jours reste juste.
     */
    suspend fun editPhaseHistoryDate(phaseHistory: PhaseHistory, newStartDate: LocalDate) {
        phaseHistoryDao.update(phaseHistory.copy(startDate = newStartDate))
        val allHistory = phaseHistoryDao.getForPlant(phaseHistory.plantId)
        val firstEntry = allHistory.minByOrNull { it.id }
        if (firstEntry?.id == phaseHistory.id) {
            plantDao.getById(phaseHistory.plantId)?.let { plantDao.update(it.copy(startDate = newStartDate)) }
        }
    }

    /**
     * Supprime la phase en cours (par glissement) et fait revenir la plante à la phase précédente,
     * qui redevient "ouverte" (endDate = null). Ne fait rien s'il n'y a pas de phase antérieure.
     */
    suspend fun revertToPreviousPhase(currentOpenPhase: PhaseHistory) {
        val history = phaseHistoryDao.getForPlant(currentOpenPhase.plantId).sortedBy { it.id }
        val idx = history.indexOfFirst { it.id == currentOpenPhase.id }
        if (idx <= 0) return
        val previous = history[idx - 1]
        phaseHistoryDao.delete(currentOpenPhase)
        phaseHistoryDao.update(previous.copy(endDate = null))
        plantDao.getById(currentOpenPhase.plantId)?.let { plantDao.update(it.copy(currentPhase = previous.phase)) }
    }

    fun observePlantLogs(plantId: Long): Flow<List<PlantLog>> = plantLogDao.observeForPlant(plantId)
    suspend fun addPlantLog(log: PlantLog): Long = plantLogDao.upsert(log)
    suspend fun deletePlantLog(log: PlantLog) = plantLogDao.delete(log)

    fun observeEnvironmentLogs(environmentId: Long): Flow<List<EnvironmentLog>> = environmentLogDao.observeForEnvironment(environmentId)
    suspend fun addEnvironmentLog(log: EnvironmentLog): Long = environmentLogDao.upsert(log)
    suspend fun deleteEnvironmentLog(log: EnvironmentLog) = environmentLogDao.delete(log)
}
