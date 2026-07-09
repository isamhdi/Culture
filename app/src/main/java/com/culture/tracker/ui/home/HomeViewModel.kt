package com.culture.tracker.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.CalendarAction
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.local.entity.Fertilizer
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.HeightMeasurement
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.local.entity.PlantLog
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.PhotoRepository
import com.culture.tracker.domain.MoonPhase
import com.culture.tracker.domain.MoonPhaseCalculator
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.domain.model.PlantMeasurementType
import java.time.DayOfWeek
import java.time.LocalDate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GrowthRiskFactor(val label: String, val detail: String)

data class HomeUiState(
    val selectedDate: LocalDate = LocalDate.now(),
    val moonPhase: MoonPhase = MoonPhaseCalculator.phaseFor(LocalDate.now()),
    val actionsInWeek: List<CalendarAction> = emptyList(),
    val riskFactors: List<GrowthRiskFactor> = emptyList(),
    val plants: List<Plant> = emptyList(),
    val environments: List<Environment> = emptyList(),
    val thumbnails: Map<Long, String> = emptyMap(),
    val genetics: List<Genetics> = emptyList(),
    val openPhaseByPlant: Map<Long, PhaseHistory> = emptyMap(),
    val latestHeightByPlant: Map<Long, Double> = emptyMap(),
    val todayScheduledCount: Int = 0,
    val todayDoneCount: Int = 0,
    val fertilizers: List<Fertilizer> = emptyList(),
) {
    val actionsToday: Int get() = actionsInWeek.count { it.date == LocalDate.now() }
    val todayCompletionRatio: Float
        get() = if (todayScheduledCount == 0) 1f else (todayDoneCount.toFloat() / todayScheduledCount.toFloat()).coerceIn(0f, 1f)
}

private const val IDEAL_TEMP_MIN = 18.0
private const val IDEAL_TEMP_MAX = 28.0
private const val IDEAL_HUMIDITY_MIN = 40.0
private const val IDEAL_HUMIDITY_MAX = 70.0

class HomeViewModel(
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
    private val photoRepository: PhotoRepository,
) : ViewModel() {

    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val actionsInWeek = run {
        val monday = LocalDate.now().with(DayOfWeek.MONDAY)
        calendarRepository.observeActionsBetween(monday, monday.plusDays(6))
    }

    private val riskFactors = combine(
        gardenRepository.observeEnvironments(),
        calendarRepository.observeLatestReadings(),
        gardenRepository.observePlants(),
    ) { environments, readings, plants -> computeRiskFactors(environments.associateBy { it.id }, readings, plants) }

    val uiState: StateFlow<HomeUiState> = combine(
        selectedDate,
        actionsInWeek,
        riskFactors,
        gardenRepository.observePlants(),
        gardenRepository.observeEnvironments(),
        photoRepository.observeLatestPerPlant(),
        gardenRepository.observeGenetics(),
        gardenRepository.observeAllOpenPhases(),
        gardenRepository.observeLatestHeights(),
        calendarRepository.observeFertilizers(),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val selected = values[0] as LocalDate
        val actions = values[1] as List<CalendarAction>
        val risks = values[2] as List<GrowthRiskFactor>
        val plants = values[3] as List<Plant>
        val environments = values[4] as List<Environment>
        val photos = values[5] as List<com.culture.tracker.data.local.entity.PlantPhoto>
        val genetics = values[6] as List<Genetics>
        val openPhases = values[7] as List<PhaseHistory>
        val heights = values[8] as List<HeightMeasurement>
        val fertilizers = values[9] as List<Fertilizer>
        HomeUiState(
            selectedDate = selected,
            moonPhase = MoonPhaseCalculator.phaseFor(selected),
            actionsInWeek = actions,
            riskFactors = risks,
            plants = plants,
            environments = environments,
            thumbnails = photos.associate { it.plantId to it.filePath },
            genetics = genetics,
            openPhaseByPlant = openPhases.associateBy { it.plantId },
            latestHeightByPlant = heights.associate { it.plantId to it.heightCm },
            fertilizers = fertilizers,
        )
    }.map { base ->
        val (scheduled, done) = computeTodayProgress(base.plants, base.actionsInWeek.filter { it.date == LocalDate.now() })
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

    fun addPlantLog(plantId: Long, date: LocalDate, note: String?, measurementType: PlantMeasurementType?, measurementValue: Double?) {
        viewModelScope.launch {
            gardenRepository.addPlantLog(
                PlantLog(plantId = plantId, date = date, note = note, measurementType = measurementType, measurementValue = measurementValue),
            )
        }
    }

    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
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
