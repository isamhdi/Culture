package com.culture.tracker.domain

import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.domain.model.GrowthPhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** Valeur personnalisée (en jours) définie pour [phase] sur cette génétique, ou null si elle utilise la valeur par défaut. */
fun Genetics?.overrideFor(phase: GrowthPhase): Int? = when (phase) {
    GrowthPhase.GERMINATION -> this?.germinationDays
    GrowthPhase.CROISSANCE -> this?.croissanceDays
    GrowthPhase.FLORAISON -> this?.floraisonDays
    GrowthPhase.SECHAGE -> this?.sechageDays
    GrowthPhase.MATURATION -> this?.maturationDays
}

/** Durée (en jours) de [phase] pour cette génétique : la valeur personnalisée si définie, sinon la valeur par défaut. */
fun Genetics?.durationFor(phase: GrowthPhase): Int = overrideFor(phase) ?: phase.typicalDurationDays

/** Progression dans la phase en cours : jours écoulés, durée totale estimée, jours restants (peut être négatif = retard). */
data class PhaseProgress(val daysInPhase: Long, val totalDurationDays: Int, val remainingDays: Long) {
    val fraction: Float get() = if (totalDurationDays <= 0) 0f else (daysInPhase.toFloat() / totalDurationDays.toFloat()).coerceIn(0f, 1f)
}

fun phaseProgressOf(phaseStartDate: LocalDate, currentPhase: GrowthPhase, genetics: Genetics?): PhaseProgress {
    val daysInPhase = ChronoUnit.DAYS.between(phaseStartDate, LocalDate.now())
    val total = genetics.durationFor(currentPhase)
    return PhaseProgress(daysInPhase, total, total - daysInPhase)
}

/** Hauteur de départ plausible (en cm) selon la phase choisie à la création d'une plante. */
fun defaultHeightCmFor(phase: GrowthPhase): Double = when (phase) {
    GrowthPhase.GERMINATION -> 1.0
    GrowthPhase.CROISSANCE -> 20.0
    GrowthPhase.FLORAISON, GrowthPhase.SECHAGE, GrowthPhase.MATURATION -> 60.0
}
