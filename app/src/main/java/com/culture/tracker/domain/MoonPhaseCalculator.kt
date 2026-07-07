package com.culture.tracker.domain

import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.floor

enum class MoonPhase(val label: String, val emoji: String) {
    NOUVELLE_LUNE("Nouvelle lune", "🌑"),
    PREMIER_CROISSANT("Premier croissant", "🌒"),
    PREMIER_QUARTIER("Premier quartier", "🌓"),
    LUNE_GIBBEUSE_CROISSANTE("Lune gibbeuse croissante", "🌔"),
    PLEINE_LUNE("Pleine lune", "🌕"),
    LUNE_GIBBEUSE_DECROISSANTE("Lune gibbeuse décroissante", "🌖"),
    DERNIER_QUARTIER("Dernier quartier", "🌗"),
    DERNIER_CROISSANT("Dernier croissant", "🌘"),
}

/**
 * Calcul purement local (aucun appel réseau) basé sur la durée moyenne du mois synodique
 * et une nouvelle lune de référence connue (6 janvier 2000).
 */
object MoonPhaseCalculator {
    private const val SYNODIC_MONTH_DAYS = 29.53058867
    private val REFERENCE_NEW_MOON = LocalDate.of(2000, 1, 6)

    fun phaseFor(date: LocalDate): MoonPhase {
        val daysSinceReference = REFERENCE_NEW_MOON.until(date, java.time.temporal.ChronoUnit.DAYS).toDouble() +
            (date.atStartOfDay(ZoneOffset.UTC).toEpochSecond() % 86400) / 86400.0 - 0.5
        val age = (daysSinceReference.mod(SYNODIC_MONTH_DAYS))
        val index = floor(age / (SYNODIC_MONTH_DAYS / 8.0)).toInt().coerceIn(0, 7)
        return MoonPhase.entries[index]
    }

    fun illuminationFraction(date: LocalDate): Double {
        val daysSinceReference = REFERENCE_NEW_MOON.until(date, java.time.temporal.ChronoUnit.DAYS).toDouble()
        val age = daysSinceReference.mod(SYNODIC_MONTH_DAYS)
        return (1 - kotlin.math.cos(2 * Math.PI * age / SYNODIC_MONTH_DAYS)) / 2.0
    }
}
