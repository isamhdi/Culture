package com.culture.tracker.ui.garden.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.PhotoRepository
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
    ) { plants, genetics, environments, photos ->
        PlantsUiState(plants, genetics, environments, photos.associate { it.plantId to it.filePath })
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
            )
        }
    }

    fun archivePlant(plant: Plant) {
        viewModelScope.launch { repository.archivePlant(plant) }
    }
}
