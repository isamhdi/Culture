package com.culture.tracker.ui.garden.environments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.repository.GardenRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EnvironmentsViewModel(private val repository: GardenRepository) : ViewModel() {

    val environments: StateFlow<List<Environment>> = repository.observeEnvironments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun createEnvironment(
        name: String,
        lightHoursPerDay: Double,
        sizeDescription: String?,
        materialDescription: String?,
        lightingType: String?,
        lightingPowerWatts: Int?,
        lightingSpectrum: String?,
        lightingModel: String?,
    ) {
        viewModelScope.launch {
            repository.createEnvironment(
                Environment(
                    name = name,
                    lightHoursPerDay = lightHoursPerDay,
                    sizeDescription = sizeDescription,
                    materialDescription = materialDescription,
                    lightingType = lightingType,
                    lightingPowerWatts = lightingPowerWatts,
                    lightingSpectrum = lightingSpectrum,
                    lightingModel = lightingModel,
                ),
            )
        }
    }

    fun deleteEnvironment(environment: Environment) {
        viewModelScope.launch { repository.deleteEnvironment(environment) }
    }
}
