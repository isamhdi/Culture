package com.culture.tracker.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.culture.tracker.data.local.entity.CalendarAction
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.local.entity.Fertilizer
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.domain.model.GrowthPhase
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PredictedAction(val plantId: Long, val plantName: String, val actionType: ActionType, val date: LocalDate)

data class CalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val actionsInMonth: List<CalendarAction> = emptyList(),
    val plants: List<Plant> = emptyList(),
    val environments: List<Environment> = emptyList(),
    val fertilizers: List<Fertilizer> = emptyList(),
    val predictedActions: List<PredictedAction> = emptyList(),
) {
    val actionsForSelectedDay: List<CalendarAction> get() = actionsInMonth.filter { it.date == selectedDate }
    val predictedForSelectedDay: List<PredictedAction> get() = predictedActions.filter { it.date == selectedDate }
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class CalendarViewModel(
    private val gardenRepository: GardenRepository,
    private val calendarRepository: CalendarRepository,
) : ViewModel() {

    private val visibleMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(LocalDate.now())

    private val actionsInMonth = visibleMonth.flatMapLatest { month ->
        calendarRepository.observeActionsBetween(month.atDay(1), month.atEndOfMonth())
    }

    private data class CalendarBaseData(
        val month: YearMonth,
        val selected: LocalDate,
        val actions: List<CalendarAction>,
        val plants: List<Plant>,
        val environments: List<Environment>,
        val fertilizers: List<Fertilizer>,
    )

    val uiState: StateFlow<CalendarUiState> = combine(
        visibleMonth,
        selectedDate,
        actionsInMonth,
        gardenRepository.observePlants(),
        gardenRepository.observeEnvironments(),
        calendarRepository.observeFertilizers(),
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        CalendarBaseData(
            month = values[0] as YearMonth,
            selected = values[1] as LocalDate,
            actions = values[2] as List<CalendarAction>,
            plants = values[3] as List<Plant>,
            environments = values[4] as List<Environment>,
            fertilizers = values[5] as List<Fertilizer>,
        )
    }.map { base ->
        CalendarUiState(
            visibleMonth = base.month,
            selectedDate = base.selected,
            actionsInMonth = base.actions,
            plants = base.plants,
            environments = base.environments,
            fertilizers = base.fertilizers,
            predictedActions = computePredictedActions(base.plants, base.month, base.actions),
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    private suspend fun computePredictedActions(
        plants: List<Plant>,
        month: YearMonth,
        actionsInMonth: List<CalendarAction>,
    ): List<PredictedAction> {
        val rangeStart = month.atDay(1)
        val rangeEnd = month.atEndOfMonth()
        val result = mutableListOf<PredictedAction>()
        for (plant in plants) {
            plant.wateringIntervalDays?.let { interval ->
                val last = calendarRepository.lastActionDate(plant.id, ActionType.ARROSAGE) ?: plant.startDate
                dueDatesIn(last, interval, rangeStart, rangeEnd).forEach { date ->
                    result += PredictedAction(plant.id, plant.name, ActionType.ARROSAGE, date)
                }
            }
            plant.fertilizingIntervalDays?.let { interval ->
                val last = calendarRepository.lastActionDate(plant.id, ActionType.ENGRAIS) ?: plant.startDate
                dueDatesIn(last, interval, rangeStart, rangeEnd).forEach { date ->
                    result += PredictedAction(plant.id, plant.name, ActionType.ENGRAIS, date)
                }
            }
        }
        // N'affiche pas une action prévue si elle a déjà été faite ce jour-là.
        return result.filterNot { predicted ->
            actionsInMonth.any { it.plantId == predicted.plantId && it.actionType == predicted.actionType && it.date == predicted.date }
        }
    }

    private fun dueDatesIn(lastDate: LocalDate, intervalDays: Int, rangeStart: LocalDate, rangeEnd: LocalDate): List<LocalDate> {
        if (intervalDays <= 0) return emptyList()
        val dates = mutableListOf<LocalDate>()
        var next = lastDate.plusDays(intervalDays.toLong())
        while (next.isBefore(rangeStart)) {
            next = next.plusDays(intervalDays.toLong())
        }
        while (!next.isAfter(rangeEnd)) {
            dates += next
            next = next.plusDays(intervalDays.toLong())
        }
        return dates
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

    fun addAction(plantId: Long, actionType: ActionType, date: LocalDate, fertilizerId: Long?, notes: String?) {
        viewModelScope.launch {
            calendarRepository.addAction(
                CalendarAction(plantId = plantId, actionType = actionType, date = date, fertilizerId = fertilizerId, notes = notes),
            )
        }
    }

    fun deleteAction(action: CalendarAction) {
        viewModelScope.launch { calendarRepository.deleteAction(action) }
    }

    fun createFertilizer(name: String, npk: String?, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = calendarRepository.createFertilizer(Fertilizer(name = name, npk = npk))
            onCreated(id)
        }
    }

    fun changePlantPhase(plantId: Long, phase: GrowthPhase, effectiveDate: LocalDate) {
        viewModelScope.launch { gardenRepository.changePhase(plantId, phase, effectiveDate) }
    }

    fun recordReading(environmentId: Long, temperatureCelsius: Double, humidityPercent: Double, recordedAt: LocalDateTime) {
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
}
