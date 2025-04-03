package com.example.pest_detection_app.RoomDatabase

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.pest_detection_app.data.user.DetectionWithBoundingBoxes

@Dao
interface BoundingBoxDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBoundingBoxes(boxes: List<BoundingBox>)

    @Query("SELECT * FROM bounding_boxes WHERE detectionId = :detectionId")
    suspend fun getBoundingBoxesByImage(detectionId: Int): List<BoundingBox>

    @Delete
    suspend fun deleteBoundingBox(box: BoundingBox)

    @Transaction
    @Query("""
    SELECT * FROM detection_results 
    WHERE id IN (SELECT detectionId FROM bounding_boxes WHERE clsName = :pestName)
""")
    suspend fun getDetectionsByPestName(pestName: String): List<DetectionWithBoundingBoxes>

}
