package com.culture.tracker.domain.model

enum class PropagationType(val label: String) {
    GRAINE("Graine"),
    BOUTURE("Bouture"),
}

enum class GrowthPhase(val label: String, val typicalDurationDays: Int) {
    GERMINATION("Germination", 14),
    CROISSANCE("Croissance", 35),
    FLORAISON("Floraison", 56),
    SECHAGE("Séchage", 10),
    MATURATION("Maturation", 14),
}

enum class ActionType(val label: String, val colorHex: Long, val darkColorHex: Long) {
    ARROSAGE("Arrosage", 0xFF0284C7, 0xFF38BDF8),
    ENGRAIS("Engrais", 0xFF16A34A, 0xFF4ADE80),
    COUPE("Coupe", 0xFFDC2626, 0xFFE85D5D),
    PINCH("Pinch", 0xFFEA580C, 0xFFFB923C),
    REMPOTAGE("Rempotage", 0xFF7C3AED, 0xFFA78BFA),
    REPULSIF("Répulsif", 0xFFB45309, 0xFFFBBF24),
    FORMATION("Formation", 0xFF0D9488, 0xFF2DD4BF),
    CHANGEMENT_ENVIRONNEMENT("Chgt. environnement", 0xFF475569, 0xFF94A3B8),
    RINCAGE("Rinçage", 0xFF0891B2, 0xFF22D3EE),
    RECOLTE("Récolte", 0xFFCA8A04, 0xFFFDE047),
    DECES("Décès", 0xFF7F1D1D, 0xFFB91C1C),
    AUTRE("Autre", 0xFF6B7280, 0xFF8B8D98),
}

/** Type de mesure ponctuelle relevée sur une plante (relevé libre, hors suivi de hauteur dédié). */
enum class PlantMeasurementType(val label: String, val unit: String) {
    HEIGHT("Hauteur", "cm"),
    TDS("TDS", "ppm"),
    PH("pH", ""),
    EC("EC", "mS/cm"),
    WATER_TEMPERATURE("Température de l'eau", "°C"),
    PPFD("PPFD", "µmol/m²/s"),
}

/** Type de mesure ponctuelle relevée sur un environnement (relevé libre, hors temp/humidité dédiées). */
enum class EnvironmentMeasurementType(val label: String, val unit: String) {
    HUMIDITY("Humidité", "%"),
    TEMPERATURE("Température", "°C"),
    OUTSIDE_TEMPERATURE("Température extérieure", "°C"),
    LIGHT_DISTANCE("Distance lumière", "cm"),
    CO2("CO2", "ppm"),
    PRECIPITATION("Précipitations", "mm"),
    AVERAGE_PPFD("PPFD moyen", "µmol/m²/s"),
    VPD("VPD", "kPa"),
}
