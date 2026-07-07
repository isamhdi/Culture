package com.culture.tracker.domain.model

enum class PropagationType(val label: String) {
    GRAINE("Graine"),
    BOUTURE("Bouture"),
}

enum class GrowthPhase(val label: String) {
    GERMINATION("Germination"),
    SEMIS("Semis"),
    CROISSANCE("Croissance"),
    FLORAISON("Floraison"),
    SECHAGE("Séchage"),
    MATURATION("Maturation"),
}

enum class ActionType(val label: String, val colorHex: Long, val darkColorHex: Long) {
    ARROSAGE("Arrosage", 0xFF2A78D6, 0xFF3987E5),
    ENGRAIS("Engrais", 0xFF008300, 0xFF008300),
    COUPE("Coupe", 0xFFE34948, 0xFFE66767),
    PINCH("Pinch", 0xFFEB6834, 0xFFD95926),
    REMPOTAGE("Rempotage", 0xFF4A3AA7, 0xFF9085E9),
    AUTRE("Autre", 0xFF898781, 0xFF898781),
}
