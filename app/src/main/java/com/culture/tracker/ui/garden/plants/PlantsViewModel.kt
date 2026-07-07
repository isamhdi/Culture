package com.culture.tracker.ui.garden.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.HeightMeasurement
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.PhotoRepository
import com.culture.tracker.domain.defaultHeightCmFor
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.domain.model.PropagationType
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlantsUiState(
    val plants: List<Plant> = emptyList(),
    val genetics: List<Genetics> = emptyList(),
    val environments: List<Environment> = emptyList(),
    val thumbnails: Map<Long, String> = emptyMap(),
    val openPhaseByPlant: Map<Long, PhaseHistory> = emptyMap(),
    val latestHeightByPlant: Map<Long, Double> = emptyMap(),
)

class PlantsViewModel(
    private val repository: GardenRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    val uiState: StateFlow<PlantsUiState> = combine(
        repository.observePlants(),
        repository.observeGenetics(),
        repository.observeEnvironments(),
        photoRepository.observeLatestPerPlant(),
        repository.observeAllOpenPhases(),
        repository.observeLatestHeights(),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val plants = values[0] as List<Plant>
        val genetics = values[1] as List<Genetics>
        val environments = values[2] as List<Environment>
        val photos = values[3] as List<com.culture.tracker.data.local.entity.PlantPhoto>
        val openPhases = values[4] as List<PhaseHistory>
        val heights = values[5] as List<HeightMeasurement>
        PlantsUiState(
            plants = plants,
            genetics = genetics,
            environments = environments,
            thumbnails = photos.associate { it.plantId to it.filePath },
            openPhaseByPlant = openPhases.associateBy { it.plantId },
            latestHeightByPlant = heights.associate { it.plantId to it.heightCm },
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlantsUiState())

    fun createGenetics(name: String, breeder: String?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = repository.createGenetics(Genetics(name = name, breeder = breeder))
            onCreated(id)
        }
    }

    fun createPlant(
        name: String,
        propagationType: PropagationType,
        geneticsId: Long?,
        environmentId: Long?,
        startingPhase: GrowthPhase,
        startDate: LocalDate,
        wateringIntervalDays: Int?,
        fertilizingIntervalDays: Int?,
    ) {
        viewModelScope.launch {
            repository.createPlant(
                Plant(
                    name = name,
                    propagationType = propagationType,
                    geneticsId = geneticsId,
                    environmentId = environmentId,
                    currentPhase = startingPhase,
                    startDate = startDate,
                    wateringIntervalDays = wateringIntervalDays,
                    fertilizingIntervalDays = fertilizingIntervalDays,
                ),
                initialHeightCm = defaultHeightCmFor(startingPhase),
            )
        }
    }

    fun archivePlant(plant: Plant) {
        viewModelScope.launch { repository.archivePlant(plant) }
    }
}
