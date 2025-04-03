package com.example.pest_detection_app.data.user

import androidx.room.Embedded
import androidx.room.Relation
import com.example.pest_detection_app.RoomDatabase.BoundingBox
import com.example.pest_detection_app.RoomDatabase.DetectionResult


data class DetectionWithBoundingBoxes(
    @Embedded val detection: DetectionResult,

    @Relation(
        parentColumn = "id", // Primary key of DetectionResult
        entityColumn = "detectionId" // Foreign key in BoundingBox table
    )
    val boundingBoxes: List<BoundingBox>
)