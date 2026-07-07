package com.culture.tracker.data.repository

import com.culture.tracker.data.local.dao.EnvironmentDao
import com.culture.tracker.data.local.dao.GeneticsDao
import com.culture.tracker.data.local.dao.PhaseHistoryDao
import com.culture.tracker.data.local.dao.PlantDao
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.domain.model.GrowthPhase
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow

class GardenRepository(
    private val plantDao: PlantDao,
    private val geneticsDao: GeneticsDao,
    private val environmentDao: EnvironmentDao,
    private val phaseHistoryDao: PhaseHistoryDao,
) {
    fun observePlants(): Flow<List<Plant>> = plantDao.observeActive()
    fun observePlant(id: Long): Flow<Plant?> = plantDao.observeById(id)
    fun observeGenetics(): Flow<List<Genetics>> = geneticsDao.observeAll()
    fun observeEnvironments(): Flow<List<Environment>> = environmentDao.observeAll()
    fun observeEnvironment(id: Long): Flow<Environment?> = environmentDao.observeById(id)
    fun observePhaseHistory(plantId: Long): Flow<List<PhaseHistory>> = phaseHistoryDao.observeForPlant(plantId)

    suspend fun plantsWithWateringSchedule(): List<Plant> = plantDao.getPlantsWithWateringSchedule()
    suspend fun plantsWithFertilizingSchedule(): List<Plant> = plantDao.getPlantsWithFertilizingSchedule()

    suspend fun createGenetics(genetics: Genetics): Long = geneticsDao.upsert(genetics)

    suspend fun createEnvironment(environment: Environment): Long = environmentDao.upsert(environment)
    suspend fun updateEnvironment(environment: Environment) = environmentDao.update(environment)
    suspend fun deleteEnvironment(environment: Environment) = environmentDao.delete(environment)

    suspend fun createPlant(plant: Plant): Long {
        val plantId = plantDao.upsert(plant)
        phaseHistoryDao.upsert(
            PhaseHistory(plantId = plantId, phase = plant.currentPhase, startDate = plant.startDate),
        )
        return plantId
    }

    suspend fun updatePlant(plant: Plant) = plantDao.update(plant)
    suspend fun archivePlant(plant: Plant) = plantDao.update(plant.copy(archived = true))

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
}
