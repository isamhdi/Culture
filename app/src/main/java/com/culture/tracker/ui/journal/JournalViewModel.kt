package com.culture.tracker.ui.journal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.CalendarAction
import com.culture.tracker.data.local.entity.Fertilizer
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.domain.model.ActionType
import java.time.LocalDate
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class JournalUiState(
    val recentActions: List<CalendarAction> = emptyList(),
    val plants: List<Plant> = emptyList(),
    val fertilizers: List<Fertilizer> = emptyList(),
)

class JournalViewModel(
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    val uiState: StateFlow<JournalUiState> = combine(
        calendarRepository.observeActionsBetween(LocalDate.now().minusYears(2), LocalDate.now().plusYears(1)),
        gardenRepository.observePlants(),
        calendarRepository.observeFertilizers(),
    ) { actions, plants, fertilizers ->
        JournalUiState(actions.sortedByDescending { it.date }, plants, fertilizers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), JournalUiState())

    fun deleteAction(action: CalendarAction) {
        viewModelScope.launch { calendarRepository.deleteAction(action) }
    }

    fun addAction(plantId: Long, actionType: ActionType, date: LocalDate, fertilizerId: Long?, notes: String?) {
        viewModelScope.launch {
            calendarRepository.addAction(
                CalendarAction(plantId = plantId, actionType = actionType, date = date, fertilizerId = fertilizerId, notes = notes),
            )
            if (actionType == ActionType.DECES) {
                uiState.value.plants.firstOrNull { it.id == plantId }?.let { gardenRepository.archivePlant(it) }
            }
        }
    }
}
