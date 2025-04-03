package com.example.pest_detection_app.RoomDatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase


@Database(
    entities = [
        User::class,
        DetectionResult::class,
        BoundingBox::class
    ],
    version = 5 ,
    exportSchema = false
)
abstract class PestDetectionDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun detectionResultDao(): DetectionResultDao
    abstract fun boundingBoxDao(): BoundingBoxDao
}

