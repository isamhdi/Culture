package com.culture.tracker.di

import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.culture.tracker.data.local.AppDatabase
import com.culture.tracker.data.repository.CalendarRepository
import com.culture.tracker.data.repository.GardenRepository
import com.culture.tracker.data.repository.PhotoRepository
import com.culture.tracker.data.repository.SettingsRepository
import com.culture.tracker.notifications.NotificationHelper
import com.culture.tracker.ui.archive.ArchiveViewModel
import com.culture.tracker.ui.calendar.CalendarViewModel
import com.culture.tracker.ui.garden.environments.EnvironmentDetailViewModel
import com.culture.tracker.ui.garden.environments.EnvironmentsViewModel
import com.culture.tracker.ui.garden.plants.PlantDetailViewModel
import com.culture.tracker.ui.garden.plants.PlantsViewModel
import com.culture.tracker.ui.genetics.GeneticsViewModel
import com.culture.tracker.ui.home.HomeViewModel
import com.culture.tracker.ui.journal.JournalViewModel
import com.culture.tracker.ui.fertilizers.FertilizersViewModel
import com.culture.tracker.ui.settings.SettingsViewModel
import com.culture.tracker.ui.tools.StageDatesViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val android.content.Context.dataStore by preferencesDataStore(name = "culture_settings")

val appModule = module {
    single {
        Room.databaseBuilder(get(), AppDatabase::class.java, AppDatabase.DATABASE_NAME)
            .fallbackToDestructiveMigration(true)
            .build()
    }
    single { get<AppDatabase>().geneticsDao() }
    single { get<AppDatabase>().environmentDao() }
    single { get<AppDatabase>().plantDao() }
    single { get<AppDatabase>().phaseHistoryDao() }
    single { get<AppDatabase>().calendarActionDao() }
    single { get<AppDatabase>().fertilizerDao() }
    single { get<AppDatabase>().environmentReadingDao() }
    single { get<AppDatabase>().plantPhotoDao() }
    single { get<AppDatabase>().heightMeasurementDao() }
    single { get<AppDatabase>().plantLogDao() }
    single { get<AppDatabase>().environmentLogDao() }

    single { get<android.content.Context>().dataStore }

    single { GardenRepository(get(), get(), get(), get(), get(), get(), get()) }
    single { CalendarRepository(get(), get(), get()) }
    single { PhotoRepository(get(), get()) }
    single { SettingsRepository(get()) }
    single { NotificationHelper(get()) }

    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { PlantsViewModel(get(), get()) }
    viewModel { (plantId: Long) -> PlantDetailViewModel(plantId, get(), get(), get()) }
    viewModel { EnvironmentsViewModel(get()) }
    viewModel { (environmentId: Long) -> EnvironmentDetailViewModel(environmentId, get(), get()) }
    viewModel { CalendarViewModel(get(), get()) }
    viewModel { JournalViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { GeneticsViewModel(get()) }
    viewModel { StageDatesViewModel(get()) }
    viewModel { FertilizersViewModel(get()) }
    viewModel { ArchiveViewModel(get()) }
}
