package com.culture.tracker.domain

import com.culture.tracker.data.local.entity.Genetics

/**
 * Sélection de variétés connues avec des durées de phase indicatives (en jours),
 * proposée comme point de départ dans l'outil "Dates de stade". Insérée une seule
 * fois en base au premier lancement ; l'utilisateur peut ensuite les modifier,
 * les supprimer ou en ajouter d'autres, exactement comme ses propres variétés.
 */
val predefinedGenetics: List<Genetics> = listOf(
    Genetics(
        name = "White Widow",
        notes = "Variété de référence (durées indicatives)",
        germinationDays = 6,
        croissanceDays = 28,
        floraisonDays = 63,
        sechageDays = 10,
        maturationDays = 14,
    ),
    Genetics(
        name = "Purple Haze",
        notes = "Variété de référence (durées indicatives)",
        germinationDays = 7,
        croissanceDays = 35,
        floraisonDays = 70,
        sechageDays = 12,
        maturationDays = 21,
    ),
    Genetics(
        name = "Northern Lights",
        notes = "Variété de référence (durées indicatives)",
        germinationDays = 5,
        croissanceDays = 21,
        floraisonDays = 49,
        sechageDays = 10,
        maturationDays = 14,
    ),
    Genetics(
        name = "Amnesia Haze",
        notes = "Variété de référence (durées indicatives)",
        germinationDays = 7,
        croissanceDays = 35,
        floraisonDays = 77,
        sechageDays = 12,
        maturationDays = 21,
    ),
    Genetics(
        name = "OG Kush",
        notes = "Variété de référence (durées indicatives)",
        germinationDays = 6,
        croissanceDays = 28,
        floraisonDays = 56,
        sechageDays = 10,
        maturationDays = 14,
    ),
    Genetics(
        name = "Critical Mass",
        notes = "Variété de référence (durées indicatives)",
        germinationDays = 5,
        croissanceDays = 24,
        floraisonDays = 49,
        sechageDays = 10,
        maturationDays = 14,
    ),
)
