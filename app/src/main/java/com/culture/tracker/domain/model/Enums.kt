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
    AUTRE("Autre", 0xFF6B7280, 0xFF8B8D98),
}
