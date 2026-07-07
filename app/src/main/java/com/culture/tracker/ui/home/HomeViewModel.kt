package com.culture.tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.CalendarAction
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.PhotoRepository
import com.culture.tracker.domain.MoonPhase
import com.culture.tracker.domain.MoonPhaseCalculator
import com.culture.tracker.domain.model.ActionType
import java.time.LocalDate
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class GrowthRiskFactor(val label: String, val detail: String)

data class HomeUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val moonPhase: MoonPhase = MoonPhaseCalculator.phaseFor(LocalDate.now()),
    val actionsInMonth: List<CalendarAction> = emptyList(),
    val riskFactors: List<GrowthRiskFactor> = emptyList(),
    val plants: List<Plant> = emptyList(),
    val environments: List<Environment> = emptyList(),
    val thumbnails: Map<Long, String> = emptyMap(),
    val todayScheduledCount: Int = 0,
    val todayDoneCount: Int = 0,
) {
    val actionsToday: Int get() = actionsInMonth.count { it.date == LocalDate.now() }
    val todayCompletionRatio: Float
        get() = if (todayScheduledCount == 0) 1f else (todayDoneCount.toFloat() / todayScheduledCount.toFloat()).coerceIn(0f, 1f)
}

private const val IDEAL_TEMP_MIN = 18.0
private const val IDEAL_TEMP_MAX = 28.0
private const val IDEAL_HUMIDITY_MIN = 40.0
private const val IDEAL_HUMIDITY_MAX = 70.0

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val actionsInMonth = visibleMonth.flatMapLatest { month ->
        calendarRepository.observeActionsBetween(month.atDay(1), month.atEndOfMonth())
    }

    private val riskFactors = combine(
        gardenRepository.observeEnvironments(),
        calendarRepository.observeLatestReadings(),
        gardenRepository.observePlants(),
    ) { environments, readings, plants -> computeRiskFactors(environments.associateBy { it.id }, readings, plants) }

    val uiState: StateFlow<HomeUiState> = combine(
        visibleMonth,
        selectedDate,
        actionsInMonth,
        riskFactors,
        gardenRepository.observePlants(),
        gardenRepository.observeEnvironments(),
        photoRepository.observeLatestPerPlant(),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val month = values[0] as YearMonth
        val selected = values[1] as LocalDate
        val actions = values[2] as List<CalendarAction>
        val risks = values[3] as List<GrowthRiskFactor>
        val plants = values[4] as List<Plant>
        val environments = values[5] as List<Environment>
        val photos = values[6] as List<com.culture.tracker.data.local.entity.PlantPhoto>
        HomeUiState(
            visibleMonth = month,
            selectedDate = selected,
            moonPhase = MoonPhaseCalculator.phaseFor(selected),
            actionsInMonth = actions,
            riskFactors = risks,
            plants = plants,
            environments = environments,
            thumbnails = photos.associate { it.plantId to it.filePath },
        )
    }.map { base ->
        val (scheduled, done) = computeTodayProgress(base.plants, base.actionsInMonth.filter { it.date == LocalDate.now() })
        base.copy(todayScheduledCount = scheduled, todayDoneCount = done)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HomeUiState())

    private suspend fun computeTodayProgress(plants: List<Plant>, actionsToday: List<CalendarAction>): Pair<Int, Int> {
        val today = LocalDate.now()
        var scheduled = 0
        var done = 0
        for (plant in plants) {
            plant.wateringIntervalDays?.let { interval ->
                val last = calendarRepository.lastActionDate(plant.id, ActionType.ARROSAGE) ?: plant.startDate
                if (!last.plusDays(interval.toLong()).isAfter(today)) {
                    scheduled++
                    if (actionsToday.any { it.plantId == plant.id && it.actionType == ActionType.ARROSAGE }) done++
                }
            }
            plant.fertilizingIntervalDays?.let { interval ->
                val last = calendarRepository.lastActionDate(plant.id, ActionType.ENGRAIS) ?: plant.startDate
                if (!last.plusDays(interval.toLong()).isAfter(today)) {
                    scheduled++
                    if (actionsToday.any { it.plantId == plant.id && it.actionType == ActionType.ENGRAIS }) done++
                }
            }
        }
        return scheduled to done
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
    }

    fun onPreviousMonth() {
        visibleMonth.value = visibleMonth.value.minusMonths(1)
    }

    fun onNextMonth() {
        visibleMonth.value = visibleMonth.value.plusMonths(1)
    }

    private fun computeRiskFactors(
        environmentsById: Map<Long, com.culture.tracker.data.local.entity.Environment>,
        readings: List<EnvironmentReading>,
        plants: List<Plant>,
    ): List<GrowthRiskFactor> {
        val factors = mutableListOf<GrowthRiskFactor>()
        for (reading in readings) {
            val envName = environmentsById[reading.environmentId]?.name ?: continue
            if (reading.temperatureCelsius < IDEAL_TEMP_MIN || reading.temperatureCelsius > IDEAL_TEMP_MAX) {
                factors += GrowthRiskFactor(
                    "Température hors plage — $envName",
                    "${reading.temperatureCelsius}°C relevé, idéal $IDEAL_TEMP_MIN-$IDEAL_TEMP_MAX°C",
                )
            }
            if (reading.humidityPercent < IDEAL_HUMIDITY_MIN || reading.humidityPercent > IDEAL_HUMIDITY_MAX) {
                factors += GrowthRiskFactor(
                    "Humidité hors plage — $envName",
                    "${reading.humidityPercent}% relevé, idéal $IDEAL_HUMIDITY_MIN-$IDEAL_HUMIDITY_MAX%",
                )
            }
        }
        val today = LocalDate.now()
        for (plant in plants) {
            val lightHours = environmentsById[plant.environmentId]?.lightHoursPerDay
            if (lightHours != null && lightHours <= 0.0) {
                factors += GrowthRiskFactor("Pas de lumière — ${plant.name}", "Vérifier le cycle d'éclairage")
            }
        }
        return factors
    }
}
