package com.culture.tracker.ui.tools

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

data class StageDatesUiState(val genetics: List<Genetics> = emptyList())

class StageDatesViewModel(private val repository: GardenRepository) : ViewModel() {

    val uiState: StateFlow<StageDatesUiState> = repository.observeGenetics()
        .map { StageDatesUiState(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), StageDatesUiState())

    fun createGenetics(name: String, breeder: String?, durations: Map<GrowthPhase, Int?>, onCreated: (Genetics) -> Unit) {
        viewModelScope.launch {
            val genetics = Genetics(
                name = name,
                breeder = breeder,
                germinationDays = durations[GrowthPhase.GERMINATION],
                croissanceDays = durations[GrowthPhase.CROISSANCE],
                floraisonDays = durations[GrowthPhase.FLORAISON],
                sechageDays = durations[GrowthPhase.SECHAGE],
                maturationDays = durations[GrowthPhase.MATURATION],
            )
            val id = repository.createGenetics(genetics)
            onCreated(genetics.copy(id = id))
        }
    }
}
