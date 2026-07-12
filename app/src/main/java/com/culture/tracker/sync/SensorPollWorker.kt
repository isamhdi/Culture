package com.culture.tracker.sync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.culture.tracker.data.repository.SensorRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/** Relève périodiquement les capteurs externes configurés (voir [SensorRepository.fetchAll]). */
class SensorPollWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params), KoinComponent {

    private val sensorRepository: SensorRepository by inject()

    override suspend fun doWork(): Result {
        sensorRepository.fetchAll()
        return Result.success()
    }
}
