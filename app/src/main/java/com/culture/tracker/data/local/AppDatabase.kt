package com.culture.tracker.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.culture.tracker.data.local.dao.CalendarActionDao
import com.culture.tracker.data.local.dao.EnvironmentDao
import com.culture.tracker.data.local.dao.EnvironmentLogDao
import com.culture.tracker.data.local.dao.EnvironmentReadingDao
import com.culture.tracker.data.local.dao.FertilizerDao
import com.culture.tracker.data.local.dao.GeneticsDao
import com.culture.tracker.data.local.dao.HeightMeasurementDao
import com.culture.tracker.data.local.dao.PhaseHistoryDao
import com.culture.tracker.data.local.dao.PlantDao
import com.culture.tracker.data.local.dao.PlantLogDao
import com.culture.tracker.data.local.dao.PlantPhotoDao
import com.culture.tracker.data.local.entity.CalendarAction
import com.culture.tracker.data.local.entity.Environment
import com.culture.tracker.data.local.entity.EnvironmentLog
import com.culture.tracker.data.local.entity.EnvironmentReading
import com.culture.tracker.data.local.entity.Fertilizer
import com.culture.tracker.data.local.entity.Genetics
import com.culture.tracker.data.local.entity.HeightMeasurement
import com.culture.tracker.data.local.entity.PhaseHistory
import com.culture.tracker.data.local.entity.Plant
import com.culture.tracker.data.local.entity.PlantLog
import com.culture.tracker.data.local.entity.PlantPhoto

@Database(
    entities = [
        Genetics::class,
        Environment::class,
        Plant::class,
        PhaseHistory::class,
        CalendarAction::class,
        Fertilizer::class,
        EnvironmentReading::class,
        PlantPhoto::class,
        HeightMeasurement::class,
        PlantLog::class,
        EnvironmentLog::class,
    ],
    version = 5,
    exportSchema = true,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun geneticsDao(): GeneticsDao
    abstract fun environmentDao(): EnvironmentDao
    abstract fun plantDao(): PlantDao
    abstract fun phaseHistoryDao(): PhaseHistoryDao
    abstract fun calendarActionDao(): CalendarActionDao
    abstract fun fertilizerDao(): FertilizerDao
    abstract fun environmentReadingDao(): EnvironmentReadingDao
    abstract fun plantPhotoDao(): PlantPhotoDao
    abstract fun heightMeasurementDao(): HeightMeasurementDao
    abstract fun plantLogDao(): PlantLogDao
    abstract fun environmentLogDao(): EnvironmentLogDao

    companion object {
        const val DATABASE_NAME = "culture.db"
    }
}
