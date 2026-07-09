package com.culture.tracker.data.local

import androidx.room.TypeConverter
import com.culture.tracker.domain.model.ActionType
import com.culture.tracker.domain.model.EnvironmentMeasurementType
import com.culture.tracker.domain.model.GrowthPhase
import com.culture.tracker.domain.model.PlantMeasurementType
import com.culture.tracker.domain.model.PropagationType
import java.time.LocalDate
import java.time.LocalDateTime

class Converters {
    @TypeConverter
    fun fromLocalDate(value: LocalDate?): String? = value?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? = value?.let(LocalDate::parse)

    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime?): String? = value?.toString()

    @TypeConverter
    fun toLocalDateTime(value: String?): LocalDateTime? = value?.let(LocalDateTime::parse)

    @TypeConverter
    fun fromPropagationType(value: PropagationType?): String? = value?.name

    @TypeConverter
    fun toPropagationType(value: String?): PropagationType? = value?.let(PropagationType::valueOf)

    @TypeConverter
    fun fromGrowthPhase(value: GrowthPhase?): String? = value?.name

    @TypeConverter
    fun toGrowthPhase(value: String?): GrowthPhase? = value?.let(GrowthPhase::valueOf)

    @TypeConverter
    fun fromActionType(value: ActionType?): String? = value?.name

    @TypeConverter
    fun toActionType(value: String?): ActionType? = value?.let(ActionType::valueOf)

    @TypeConverter
    fun fromPlantMeasurementType(value: PlantMeasurementType?): String? = value?.name

    @TypeConverter
    fun toPlantMeasurementType(value: String?): PlantMeasurementType? = value?.let(PlantMeasurementType::valueOf)

    @TypeConverter
    fun fromEnvironmentMeasurementType(value: EnvironmentMeasurementType?): String? = value?.name

    @TypeConverter
    fun toEnvironmentMeasurementType(value: String?): EnvironmentMeasurementType? = value?.let(EnvironmentMeasurementType::valueOf)
}
