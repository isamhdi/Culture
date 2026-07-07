package com.culture.tracker.ui.garden.plants

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.CalendarAction
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.Fertilizer
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.HeightMeasurement
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.local.entity.PlantPhoto
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.PhotoRepository
import com.culture.tracker.domain.model.GrowthPhase
import java.io.File
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PlantDetailUiState(
    val plant: Plant? = null,
    val phaseHistory: List<PhaseHistory> = emptyList(),
    val actions: List<CalendarAction> = emptyList(),
    val photos: List<PlantPhoto> = emptyList(),
    val genetics: List<Genetics> = emptyList(),
    val environments: List<Environment> = emptyList(),
    val fertilizers: List<Fertilizer> = emptyList(),
    val heightHistory: List<HeightMeasurement> = emptyList(),
)

class PlantDetailViewModel(
    private val plantId: Long,
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    val uiState: StateFlow<PlantDetailUiState> = combine(
        gardenRepository.observePlant(plantId),
        gardenRepository.observePhaseHistory(plantId),
        calendarRepository.observeActionsForPlant(plantId),
        photoRepository.observeForPlant(plantId),
        gardenRepository.observeGenetics(),
        gardenRepository.observeEnvironments(),
        calendarRepository.observeFertilizers(),
        gardenRepository.observeHeightHistory(plantId),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        PlantDetailUiState(
            plant = values[0] as Plant?,
            phaseHistory = values[1] as List<PhaseHistory>,
            actions = values[2] as List<CalendarAction>,
            photos = values[3] as List<PlantPhoto>,
            genetics = values[4] as List<Genetics>,
            environments = values[5] as List<Environment>,
            fertilizers = values[6] as List<Fertilizer>,
            heightHistory = values[7] as List<HeightMeasurement>,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), PlantDetailUiState())

    fun createPhotoCaptureTarget(): Pair<File, android.net.Uri> = photoRepository.createPhotoCaptureTarget()

    fun savePhoto(file: File, caption: String? = null) {
        viewModelScope.launch { photoRepository.savePhotoRecord(plantId, file, caption) }
    }

    fun deletePhoto(photo: PlantPhoto) {
        viewModelScope.launch { photoRepository.deletePhoto(photo) }
    }

    fun editPhaseHistoryDate(phaseHistory: PhaseHistory, newStartDate: LocalDate) {
        viewModelScope.launch { gardenRepository.editPhaseHistoryDate(phaseHistory, newStartDate) }
    }

    fun archivePlant() {
        viewModelScope.launch { uiState.value.plant?.let { gardenRepository.archivePlant(it) } }
    }

    fun advanceToNextPhase() {
        val plant = uiState.value.plant ?: return
        val currentIndex = GrowthPhase.entries.indexOf(plant.currentPhase)
        val nextPhase = GrowthPhase.entries.getOrNull(currentIndex + 1) ?: return
        viewModelScope.launch { gardenRepository.changePhase(plantId, nextPhase, LocalDate.now()) }
    }

    /** Supprime la phase en cours (glissement dans l'historique) et revient à la phase précédente. */
    fun revertToPreviousPhase(openPhase: PhaseHistory) {
        viewModelScope.launch { gardenRepository.revertToPreviousPhase(openPhase) }
    }

    fun addHeightMeasurement(heightCm: Double, date: LocalDate) {
        viewModelScope.launch { gardenRepository.addHeightMeasurement(plantId, date, heightCm) }
    }

    fun createGenetics(name: String, breeder: String?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = gardenRepository.createGenetics(Genetics(name = name, breeder = breeder))
            onCreated(id)
        }
    }

    fun updatePlant(
        name: String,
        geneticsId: Long?,
        environmentId: Long?,
        wateringIntervalDays: Int?,
        fertilizingIntervalDays: Int?,
        startDate: LocalDate,
        phase: GrowthPhase,
    ) {
        val current = uiState.value.plant ?: return
        viewModelScope.launch {
            if (phase != current.currentPhase) {
                gardenRepository.changePhase(plantId, phase, LocalDate.now())
            }
            gardenRepository.updatePlant(
                current.copy(
                    name = name,
                    geneticsId = geneticsId,
                    environmentId = environmentId,
                    wateringIntervalDays = wateringIntervalDays,
                    fertilizingIntervalDays = fertilizingIntervalDays,
                    startDate = startDate,
                    currentPhase = phase,
                ),
            )
            // Garde la première entrée d'historique (germination) synchronisée avec la date de début.
            val firstEntry = uiState.value.phaseHistory.minByOrNull { it.id }
            if (firstEntry != null && firstEntry.startDate != startDate) {
                gardenRepository.editPhaseHistoryDate(firstEntry, startDate)
            }
        }
    }
}
