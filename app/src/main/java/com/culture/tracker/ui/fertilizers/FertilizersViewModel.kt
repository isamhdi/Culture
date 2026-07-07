package com.culture.tracker.ui.fertilizers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Fertilizer
import com.culture.tracker.data.repository.CalendarRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FertilizersUiState(val fertilizers: List<Fertilizer> = emptyList())

class FertilizersViewModel(private val repository: CalendarRepository) : ViewModel() {

    val uiState: StateFlow<FertilizersUiState> = repository.observeFertilizers()
        .map { FertilizersUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), FertilizersUiState())

    fun createFertilizer(name: String, npk: String?, notes: String?) {
        viewModelScope.launch { repository.createFertilizer(Fertilizer(name = name, npk = npk, notes = notes)) }
    }

    fun updateFertilizer(existing: Fertilizer, name: String, npk: String?, notes: String?) {
        viewModelScope.launch { repository.updateFertilizer(existing.copy(name = name, npk = npk, notes = notes)) }
    }

    fun deleteFertilizer(fertilizer: Fertilizer) {
        viewModelScope.launch { repository.deleteFertilizer(fertilizer) }
    }
}
