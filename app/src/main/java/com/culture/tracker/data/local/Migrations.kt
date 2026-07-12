package com.culture.tracker.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrations Room explicites, écrites à partir d'un diff des schémas exportés dans app/schemas/
 * (room.schemaLocation) pour chaque incrément de AppDatabase.version déjà publié. Objectif : une
 * mise à jour de l'appli (nouvel APK installé par-dessus l'ancien) ne doit plus jamais effacer les
 * données de l'utilisateur, y compris quand le schéma change. À chaque nouvelle incrémentation de
 * version, ajouter ici la migration correspondante et l'inclure dans ALL_MIGRATIONS.
 */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE genetics ADD COLUMN germinationDays INTEGER")
        db.execSQL("ALTER TABLE genetics ADD COLUMN semisDays INTEGER")
        db.execSQL("ALTER TABLE genetics ADD COLUMN croissanceDays INTEGER")
        db.execSQL("ALTER TABLE genetics ADD COLUMN floraisonDays INTEGER")
        db.execSQL("ALTER TABLE genetics ADD COLUMN sechageDays INTEGER")
        db.execSQL("ALTER TABLE genetics ADD COLUMN maturationDays INTEGER")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `height_measurements` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`plantId` INTEGER NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`heightCm` REAL NOT NULL, " +
                "FOREIGN KEY(`plantId`) REFERENCES `plants`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_height_measurements_plantId` ON `height_measurements` (`plantId`)")
        db.execSQL("ALTER TABLE genetics DROP COLUMN semisDays")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `plant_logs` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`plantId` INTEGER NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`note` TEXT, " +
                "`measurementType` TEXT, " +
                "`measurementValue` REAL, " +
                "FOREIGN KEY(`plantId`) REFERENCES `plants`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_plant_logs_plantId` ON `plant_logs` (`plantId`)")
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `environment_logs` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`environmentId` INTEGER NOT NULL, " +
                "`date` TEXT NOT NULL, " +
                "`note` TEXT, " +
                "`measurementType` TEXT, " +
                "`measurementValue` REAL, " +
                "FOREIGN KEY(`environmentId`) REFERENCES `environments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_environment_logs_environmentId` ON `environment_logs` (`environmentId`)")
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE plants ADD COLUMN medium TEXT")
        db.execSQL("ALTER TABLE plants ADD COLUMN mediumDescription TEXT")
    }
}

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS `sensor_sources` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`environmentId` INTEGER NOT NULL, " +
                "`type` TEXT NOT NULL, " +
                "`name` TEXT NOT NULL, " +
                "`baseUrl` TEXT NOT NULL, " +
                "`accessToken` TEXT, " +
                "`temperatureEntityId` TEXT, " +
                "`humidityEntityId` TEXT, " +
                "`lastFetchAt` TEXT, " +
                "`lastFetchSuccess` INTEGER, " +
                "`lastError` TEXT, " +
                "FOREIGN KEY(`environmentId`) REFERENCES `environments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)",
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_sensor_sources_environmentId` ON `sensor_sources` (`environmentId`)")
    }
}

val ALL_MIGRATIONS = arrayOf(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
