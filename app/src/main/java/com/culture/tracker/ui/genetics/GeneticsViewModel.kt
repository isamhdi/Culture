package com.culture.tracker.ui.genetics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.domain.model.GrowthPhase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GeneticsUiState(val genetics: List<Genetics> = emptyList())

class GeneticsViewModel(private val repository: GardenRepository) : ViewModel() {

    val uiState: StateFlow<GeneticsUiState> = repository.observeGenetics()
        .map { GeneticsUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), GeneticsUiState())

    fun createGenetics(name: String, breeder: String?, durations: Map<GrowthPhase, Int?>) {
        viewModelScope.launch {
            repository.createGenetics(buildGenetics(Genetics(name = name, breeder = breeder), durations))
        }
    }

    fun updateGenetics(existing: Genetics, name: String, breeder: String?, durations: Map<GrowthPhase, Int?>) {
        viewModelScope.launch {
            repository.updateGenetics(buildGenetics(existing.copy(name = name, breeder = breeder), durations))
        }
    }

    fun deleteGenetics(genetics: Genetics) {
        viewModelScope.launch { repository.deleteGenetics(genetics) }
    }

    private fun buildGenetics(base: Genetics, durations: Map<GrowthPhase, Int?>): Genetics = base.copy(
        germinationDays = durations[GrowthPhase.GERMINATION],
        croissanceDays = durations[GrowthPhase.CROISSANCE],
        floraisonDays = durations[GrowthPhase.FLORAISON],
        sechageDays = durations[GrowthPhase.SECHAGE],
        maturationDays = durations[GrowthPhase.MATURATION],
    )
}
