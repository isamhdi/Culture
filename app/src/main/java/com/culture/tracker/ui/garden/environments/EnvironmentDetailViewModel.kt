package com.culture.tracker.ui.garden.environments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentLog
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.local.entity.SensorSource
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.SensorRepository
import com.culture.tracker.data.repository.SettingsRepository
import com.culture.tracker.domain.model.EnvironmentMeasurementType
import com.culture.tracker.domain.model.SensorSourceType
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
    val sensorSource: SensorSource? = null,
    val networkSensorsEnabled: Boolean = false,
)

class EnvironmentDetailViewModel(
    private val environmentId: Long,
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
    private val sensorRepository: SensorRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val uiState: StateFlow<EnvironmentDetailUiState> = combine(
        gardenRepository.observeEnvironment(environmentId),
        calendarRepository.observeReadings(environmentId),
        gardenRepository.observeEnvironmentLogs(environmentId),
        sensorRepository.observeForEnvironment(environmentId),
        settingsRepository.settings,
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        EnvironmentDetailUiState(
            environment = values[0] as Environment?,
            readings = (values[1] as List<EnvironmentReading>).sortedBy { it.recordedAt },
            logs = values[2] as List<EnvironmentLog>,
            sensorSource = values[3] as SensorSource?,
            networkSensorsEnabled = (values[4] as com.culture.tracker.data.repository.AppSettings).networkSensorsEnabled,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), EnvironmentDetailUiState())

    fun recordReading(temperatureCelsius: Double, humidityPercent: Double, recordedAt: LocalDateTime) {
        viewModelScope.launch {
            calendarRepository.recordReading(
                EnvironmentReading(
                    environmentId = environmentId,
                    recordedAt = recordedAt,
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

    fun saveSensorSource(
        type: SensorSourceType,
        name: String,
        baseUrl: String,
        accessToken: String?,
        temperatureEntityId: String?,
        humidityEntityId: String?,
    ) {
        viewModelScope.launch {
            val existing = uiState.value.sensorSource
            sensorRepository.saveSource(
                SensorSource(
                    id = existing?.id ?: 0,
                    environmentId = environmentId,
                    type = type,
                    name = name,
                    baseUrl = baseUrl,
                    accessToken = accessToken,
                    temperatureEntityId = temperatureEntityId,
                    humidityEntityId = humidityEntityId,
                ),
            )
        }
    }

    fun testSensorSource() {
        val source = uiState.value.sensorSource ?: return
        viewModelScope.launch { sensorRepository.fetchNow(source) }
    }

    fun deleteSensorSource() {
        val source = uiState.value.sensorSource ?: return
        viewModelScope.launch { sensorRepository.deleteSource(source) }
    }
}
