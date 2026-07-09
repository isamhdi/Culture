package com.culture.tracker.ui.garden.plants

import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.domain.model.GrowthPhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class PlantStatusFilter(val label: String) {
    ALL("Toutes"),
    GROWING("En croissance"),
    HARVESTED("Récoltées"),
}

enum class PlantGroupBy(val label: String) {
    STAGE("Stade"),
    NONE("Aucun"),
    ENVIRONMENT("Environnement"),
}

enum class PlantSortBy(val label: String) {
    NAME_ASC("Nom ▲"),
    NAME_DESC("Nom ▼"),
    AGE_ASC("Âge ▲"),
    AGE_DESC("Âge ▼"),
}

data class PlantFilters(
    val status: PlantStatusFilter = PlantStatusFilter.ALL,
    val groupBy: PlantGroupBy = PlantGroupBy.NONE,
    val sortBy: PlantSortBy = PlantSortBy.NAME_ASC,
    val stages: Set<GrowthPhase> = emptySet(),
    val environments: Set<Long> = emptySet(),
) {
    val activeCount: Int
        get() = (if (status != PlantStatusFilter.ALL) 1 else 0) + stages.size + environments.size
}

private val harvestedPhases = setOf(GrowthPhase.SECHAGE, GrowthPhase.MATURATION)

fun applyPlantFilters(plants: List<Plant>, filters: PlantFilters): List<Plant> {
    var result = plants.filter { plant ->
        when (filters.status) {
            PlantStatusFilter.ALL -> true
            PlantStatusFilter.GROWING -> plant.currentPhase !in harvestedPhases
            PlantStatusFilter.HARVESTED -> plant.currentPhase in harvestedPhases
        }
    }
    if (filters.stages.isNotEmpty()) {
        result = result.filter { it.currentPhase in filters.stages }
    }
    if (filters.environments.isNotEmpty()) {
        result = result.filter { it.environmentId in filters.environments }
    }
    result = when (filters.sortBy) {
        PlantSortBy.NAME_ASC -> result.sortedBy { it.name.lowercase() }
        PlantSortBy.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
        PlantSortBy.AGE_ASC -> result.sortedByDescending { it.startDate }
        PlantSortBy.AGE_DESC -> result.sortedBy { it.startDate }
    }
    return result
}

fun groupPlants(plants: List<Plant>, groupBy: PlantGroupBy, environments: List<Environment>): List<Pair<String, List<Plant>>> =
    when (groupBy) {
        PlantGroupBy.NONE -> listOf("" to plants)
        PlantGroupBy.STAGE -> plants.groupBy { it.currentPhase.label }.toList().sortedBy { (_, list) -> list.first().currentPhase.ordinal }
        PlantGroupBy.ENVIRONMENT -> plants.groupBy { plant ->
            environments.firstOrNull { it.id == plant.environmentId }?.name ?: "Sans environnement"
        }.toList()
    }

fun plantAgeDays(plant: Plant): Long = ChronoUnit.DAYS.between(plant.startDate, LocalDate.now())
