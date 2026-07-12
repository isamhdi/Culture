package com.culture.tracker.ui.home

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * Messages d'accueil courts, tirés au sort selon le jour de la semaine.
 * Quand un prénom est renseigné, quelques variantes nominatives s'ajoutent au tirage
 * (dont une dédiée au dimanche) pour l'inclure "de temps en temps" sans systématisme.
 */
private val messagesByDay: Map<DayOfWeek, List<String>> = mapOf(
    DayOfWeek.MONDAY to listOf(
        "Nouvelle semaine, nouvelles pousses. C'est parti 🌱",
        "Lundi productif en perspective pour le jardin.",
        "On attaque la semaine — un œil sur tes plantes ?",
        "Nouvelle semaine, même discipline : arrosage, observation, patience.",
    ),
    DayOfWeek.TUESDAY to listOf(
        "Un petit tour du jardin aujourd'hui ?",
        "Mardi calme, parfait pour observer sans intervenir.",
        "Comment se portent tes plantes aujourd'hui ?",
        "Petit check-up du mardi, ça ne mange pas de pain.",
    ),
    DayOfWeek.WEDNESDAY to listOf(
        "Mi-semaine, mi-chemin. Tes plantes tiennent le rythme.",
        "Mercredi : le bon jour pour rattraper un relevé oublié.",
        "La semaine avance, le jardin aussi.",
        "Milieu de semaine — tout pousse comme prévu ?",
    ),
    DayOfWeek.THURSDAY to listOf(
        "Presque le week-end — pense à checker l'arrosage.",
        "Jeudi, dernière ligne droite avant le repos.",
        "Un jeudi productif pour toi et pour tes plantes.",
        "Plus qu'un jour avant le week-end — et tes plantes le sentent aussi.",
    ),
    DayOfWeek.FRIDAY to listOf(
        "Vendredi ! Le jardin aussi mérite sa pause.",
        "Fin de semaine — bilan rapide sur le jardin ?",
        "Vendredi, l'occasion de faire le point avant le week-end.",
        "Dernier jour de la semaine, premier jour du repos du jardin.",
    ),
    DayOfWeek.SATURDAY to listOf(
        "Journée cool, jardin cool. Profites-en pour observer tes plantes de près.",
        "Samedi tranquille, parfait pour une photo de tes plantes.",
        "Pas d'obligation aujourd'hui, juste le plaisir de jardiner.",
        "Le week-end commence bien avec un petit tour au jardin.",
    ),
    DayOfWeek.SUNDAY to listOf(
        "Dimanche tranquille ☕🌿 Un bon jour pour prendre le temps d'admirer le travail de la semaine.",
        "Dimanche douceur — pose-toi un instant avec tes plantes.",
        "Repos bien mérité. Le jardin veille sur lui-même aujourd'hui 🌙",
        "Un dimanche cocooning, entouré de verdure 🍃",
    ),
)

private val namedTemplates: List<String> = listOf(
    "Bonjour {name}, prêt·e pour une nouvelle journée de pousse ?",
    "Salut {name} ! Tes plantes t'attendent.",
    "Bonjour {name}, comment va le jardin aujourd'hui ?",
    "{name}, ton jardin pense à toi 🌱",
)

private const val SUNDAY_NAMED_TEMPLATE = "Bonjour {name}, profite bien de ce dimanche 🌿"

fun homeGreeting(date: LocalDate, userName: String?): String {
    val dayMessages = messagesByDay.getValue(date.dayOfWeek)
    val pool = if (userName.isNullOrBlank()) {
        dayMessages
    } else {
        val named = if (date.dayOfWeek == DayOfWeek.SUNDAY) namedTemplates + SUNDAY_NAMED_TEMPLATE else namedTemplates
        dayMessages + named.map { it.replace("{name}", userName) }
    }
    return pool.random()
}
