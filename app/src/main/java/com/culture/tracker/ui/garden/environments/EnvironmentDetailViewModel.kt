package com.culture.tracker.ui.garden.environments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import java.time.LocalDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EnvironmentDetailUiState(
    val environment: Environment? = null,
    val readings: List<EnvironmentReading> = emptyList(),
)

class EnvironmentDetailViewModel(
    private val environmentId: Long,
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    val uiState: StateFlow<EnvironmentDetailUiState> = combine(
        gardenRepository.observeEnvironment(environmentId),
        calendarRepository.observeReadings(environmentId),
    ) { environment, readings ->
        EnvironmentDetailUiState(environment, readings.sortedBy { it.recordedAt })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnvironmentDetailUiState())

    fun recordReading(temperatureCelsius: Double, humidityPercent: Double) {
        viewModelScope.launch {
            calendarRepository.recordReading(
                EnvironmentReading(
                    environmentId = environmentId,
                    recordedAt = LocalDateTime.now(),
                    temperatureCelsius = temperatureCelsius,
                    humidityPercent = humidityPercent,
                ),
            )
        }
    }
}
