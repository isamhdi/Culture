package com.culture.tracker.data.backup

import android.content.Context
import android.net.Uri
import com.culture.tracker.data.local.AppDatabase
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val DB_ENTRY_NAME = "culture.db"
private const val PHOTOS_ENTRY_PREFIX = "photos/"

/**
 * Sauvegarde/restauration locale : copie brute du fichier SQLite de Room et du dossier photos
 * dans/depuis une archive zip choisie par l'utilisateur (Storage Access Framework). Rien n'est
 * envoyé où que ce soit, le fichier reste sous le contrôle de l'utilisateur.
 */
class BackupRepository(
    private val context: Context,
    private val database: AppDatabase,
) {
    suspend fun export(destination: Uri): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            // Force l'écriture de toutes les pages WAL dans le fichier principal avant de le copier,
            // sans avoir à fermer la connexion Room (qui reste utilisée par le reste de l'appli).
            database.query("PRAGMA wal_checkpoint(FULL)", null).close()

            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            val photosDir = File(context.filesDir, "photos")

            val out = context.contentResolver.openOutputStream(destination)
                ?: error("Impossible d'ouvrir le fichier de destination")
            out.use { stream ->
                ZipOutputStream(stream).use { zip ->
                    zip.putNextEntry(ZipEntry(DB_ENTRY_NAME))
                    dbFile.inputStream().use { it.copyTo(zip) }
                    zip.closeEntry()

                    photosDir.listFiles()?.forEach { file ->
                        if (file.isFile) {
                            zip.putNextEntry(ZipEntry(PHOTOS_ENTRY_PREFIX + file.name))
                            file.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                        }
                    }
                }
            }
        }
    }

    /**
     * Remplace toutes les données actuelles par celles du fichier de sauvegarde. Un redémarrage
     * complet de l'application est nécessaire après un import réussi (la connexion Room sous-jacente
     * est fermée ici et ne doit plus être utilisée par le process courant).
     */
    suspend fun import(source: Uri): Result<Unit> = runCatching {
        withContext(Dispatchers.IO) {
            val tempDir = File(context.cacheDir, "restore_tmp").apply {
                deleteRecursively()
                mkdirs()
            }
            val tempDbFile = File(tempDir, DB_ENTRY_NAME)
            val tempPhotosDir = File(tempDir, "photos").apply { mkdirs() }
            var foundDb = false

            val input = context.contentResolver.openInputStream(source)
                ?: error("Impossible de lire le fichier sélectionné")
            input.use { stream ->
                ZipInputStream(stream).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        val name = entry.name
                        when {
                            name == DB_ENTRY_NAME -> {
                                tempDbFile.outputStream().use { zip.copyTo(it) }
                                foundDb = true
                            }
                            name.startsWith(PHOTOS_ENTRY_PREFIX) && !entry.isDirectory -> {
                                val fileName = name.removePrefix(PHOTOS_ENTRY_PREFIX)
                                // Protection contre le zip-slip : on ignore toute entrée qui tenterait
                                // de sortir du dossier photos.
                                if (fileName.isNotBlank() && !fileName.contains("..") && !fileName.contains('/')) {
                                    File(tempPhotosDir, fileName).outputStream().use { zip.copyTo(it) }
                                }
                            }
                        }
                        zip.closeEntry()
                        entry = zip.nextEntry
                    }
                }
            }

            if (!foundDb) {
                tempDir.deleteRecursively()
                error("Ce fichier n'est pas une sauvegarde Pousse valide.")
            }

            database.close()

            val dbFile = context.getDatabasePath(AppDatabase.DATABASE_NAME)
            dbFile.parentFile?.mkdirs()
            File(dbFile.path + "-wal").delete()
            File(dbFile.path + "-shm").delete()
            File(dbFile.path + "-journal").delete()
            tempDbFile.copyTo(dbFile, overwrite = true)

            val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
            photosDir.listFiles()?.forEach { it.delete() }
            tempPhotosDir.listFiles()?.forEach { it.copyTo(File(photosDir, it.name), overwrite = true) }

            tempDir.deleteRecursively()
        }
    }
}
