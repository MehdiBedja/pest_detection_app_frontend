package com.example.pest_detection_app.RoomDatabase

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


object DatabaseManager {
    @Volatile
    private var instance: PestDetectionDatabase? = null

    fun getDatabase(context: Context): PestDetectionDatabase {
        return instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                PestDetectionDatabase::class.java,
                "pestDetectionDatabase"
            )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7) // Added migration
                .build()
                .also { instance = it }
        }
    }
    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // 1. Add the new column with a default value (use Unix timestamp format)
            database.execSQL("ALTER TABLE detection_results ADD COLUMN detectionDate INTEGER NOT NULL DEFAULT 0")

            // 2. If you want, update existing rows with a reasonable timestamp (e.g., the time the migration is performed)
            val currentTime = System.currentTimeMillis()
            database.execSQL("UPDATE detection_results SET detectionDate = $currentTime WHERE detectionDate = 0")
        }
    }

    // 🔹 Migration 5 -> 6 (Adding "note" field)
    private val MIGRATION_5_6 = object : Migration(5, 6) {
        override fun migrate(database: SupportSQLiteDatabase) {
            database.execSQL("ALTER TABLE detection_results ADD COLUMN note TEXT DEFAULT NULL")
        }
    }


    private val MIGRATION_6_7 = object : Migration(6, 7) {
        override fun migrate(database: SupportSQLiteDatabase) {
            // Add the serverId field to the detection_results table
            database.execSQL("ALTER TABLE detection_results ADD COLUMN serverId TEXT NOT NULL DEFAULT ''")
        }
    }


    fun userDao(context: Context): UserDao = getDatabase(context).userDao()
    fun detectionResultDao(context: Context): DetectionResultDao = getDatabase(context).detectionResultDao()
    fun boundingBoxDao(context: Context): BoundingBoxDao = getDatabase(context).boundingBoxDao()
}