package com.example.pest_detection_app.RoomDatabase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.pest_detection_app.data.user.DetectionWithBoundingBoxes

@Dao
interface DetectionResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionResult(result: DetectionResult): Long

    @Query("SELECT * FROM detection_results WHERE userId = :userId")
    suspend fun getDetectionsByUser(userId: Int?): List<DetectionWithBoundingBoxes>

    @Query("UPDATE detection_results SET isSynced = :synced WHERE id = :detectionId")
    suspend fun updateSyncStatus(detectionId: Int, synced: Boolean)

    @Delete
    suspend fun deleteDetection(result: DetectionResult)

    @Query("SELECT * FROM detection_results WHERE detectionDate BETWEEN :startDate AND :endDate ORDER BY detectionDate DESC")
    fun getDetectionsByDate(startDate: Long, endDate: Long): LiveData<List<DetectionResult>>

    @Transaction
    @Query("SELECT * FROM detection_results WHERE userId = :userId ORDER BY detectionDate DESC")
    suspend fun getDetectionsSortedDesc(userId: Int?): List<DetectionWithBoundingBoxes>

    @Transaction
    @Query("SELECT * FROM detection_results WHERE userId = :userId ORDER BY detectionDate ASC")
    suspend fun getDetectionsSortedAsc(userId: Int?): List<DetectionWithBoundingBoxes>

    @Transaction
    @Query("SELECT * FROM detection_results WHERE id = :detectionId")
    suspend fun getDetectionById(detectionId: Int): DetectionWithBoundingBoxes?

    // Delete a single detection by its ID
    @Query("DELETE FROM detection_results WHERE id = :detectionId")
    suspend fun deleteDetectionById(detectionId: Int)

    // Delete all detections for a specific user
    @Query("DELETE FROM detection_results WHERE userId = :userId")
    suspend fun deleteAllDetectionsForUser(userId: Int)


    @Query("""
    DELETE FROM detection_results
    WHERE id IN (
        SELECT detectionId FROM bounding_boxes 
        WHERE clsName = :pestName
    ) 
    AND userId = :userId
""")
    suspend fun deleteDetectionsByPestNameForUser(pestName: String, userId: Int)


    @Query("DELETE FROM detection_results WHERE userId = :userId AND id IN (SELECT detectionId FROM bounding_boxes WHERE clsName = :pestName)")
    suspend fun deleteDetectionsByPestName(userId: Int?, pestName: String)


    @Query("UPDATE detection_results SET note = :note WHERE id = :detectionId")
    suspend fun setNoteForDetection(detectionId: Int, note: String)

    @Query("UPDATE detection_results SET note = NULL WHERE id = :detectionId")
    suspend fun deleteNoteForDetection(detectionId: Int)





}