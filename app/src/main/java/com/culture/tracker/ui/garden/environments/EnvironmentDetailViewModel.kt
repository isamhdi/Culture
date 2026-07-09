package com.culture.tracker.ui.garden.environments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentLog
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.domain.model.EnvironmentMeasurementType
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class EnvironmentDetailUiState(
    val environment: Environment? = null,
    val readings: List<EnvironmentReading> = emptyList(),
    val logs: List<EnvironmentLog> = emptyList(),
)

class EnvironmentDetailViewModel(
    private val environmentId: Long,
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    val uiState: StateFlow<EnvironmentDetailUiState> = combine(
        gardenRepository.observeEnvironment(environmentId),
        calendarRepository.observeReadings(environmentId),
        gardenRepository.observeEnvironmentLogs(environmentId),
    ) { environment, readings, logs ->
        EnvironmentDetailUiState(environment, readings.sortedBy { it.recordedAt }, logs)
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

    fun addEnvironmentLog(date: LocalDate, note: String?, measurementType: EnvironmentMeasurementType?, measurementValue: Double?) {
        viewModelScope.launch {
            gardenRepository.addEnvironmentLog(
                EnvironmentLog(environmentId = environmentId, date = date, note = note, measurementType = measurementType, measurementValue = measurementValue),
            )
        }
    }

    fun deleteEnvironmentLog(log: EnvironmentLog) {
        viewModelScope.launch { gardenRepository.deleteEnvironmentLog(log) }
    }
}
