package com.culture.tracker.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.culture.tracker.data.local.dao.PlantPhotoDao
import com.culture.tracker.data.local.entity.PlantPhoto
import java.io.File
import java.time.LocalDateTime
import kotlinx.coroutines.flow.Flow

class PhotoRepository(
    private val appContext: Context,
    private val plantPhotoDao: PlantPhotoDao,
) {
    fun observeForPlant(plantId: Long): Flow<List<PlantPhoto>> = plantPhotoDao.observeForPlant(plantId)
    fun observeLatestPerPlant(): Flow<List<PlantPhoto>> = plantPhotoDao.observeLatestPerPlant()

    private val photosDir: File
        get() = File(appContext.filesDir, "photos").apply { mkdirs() }

    /** Crée un fichier vide et son URI content:// (via FileProvider) pour une capture caméra. */
    fun createPhotoCaptureTarget(): Pair<File, Uri> {
        val file = File(photosDir, "IMG_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", file)
        return file to uri
    }

    suspend fun savePhotoRecord(plantId: Long, file: File, caption: String? = null): Long =
        plantPhotoDao.insert(
            PlantPhoto(plantId = plantId, filePath = file.absolutePath, takenAt = LocalDateTime.now(), caption = caption),
        )

    suspend fun deletePhoto(photo: PlantPhoto) {
        File(photo.filePath).delete()
        plantPhotoDao.delete(photo)
    }
}
