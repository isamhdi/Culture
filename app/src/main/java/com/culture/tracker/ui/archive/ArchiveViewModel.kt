package com.culture.tracker.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.repository.GardenRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val plants: List<Plant> = emptyList(),
    val genetics: List<Genetics> = emptyList(),
)

class ArchiveViewModel(private val repository: GardenRepository) : ViewModel() {
    val uiState: StateFlow<ArchiveUiState> = combine(
        repository.observeArchivedPlants(),
        repository.observeGenetics(),
    ) { plants, genetics -> ArchiveUiState(plants, genetics) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ArchiveUiState())

    fun unarchive(plant: Plant) {
        viewModelScope.launch { repository.unarchivePlant(plant) }
    }
}
