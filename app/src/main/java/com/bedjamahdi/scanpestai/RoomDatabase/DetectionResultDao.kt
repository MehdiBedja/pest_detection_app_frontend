package com.bedjamahdi.scanpestai.RoomDatabase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.bedjamahdi.scanpestai.data.user.DetectionNoteUpdate
import com.bedjamahdi.scanpestai.data.user.DetectionWithBoundingBoxes

@Dao
interface DetectionResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDetectionResult(result: DetectionResult): Long

    @Query("SELECT * FROM detection_results WHERE userId = :userId and isDeleted = 0")
    suspend fun getDetectionsByUser(userId: Int?): List<DetectionWithBoundingBoxes>

    @Query("UPDATE detection_results SET isSynced = :synced WHERE id = :detectionId")
    suspend fun updateSyncStatus(detectionId: Int, synced: Boolean)

    @Delete
    suspend fun deleteDetection(result: DetectionResult)

    @Query("SELECT * FROM detection_results WHERE detectionDate BETWEEN :startDate AND :endDate and isDeleted = 0 ORDER BY detectionDate DESC")
    fun getDetectionsByDate(startDate: Long, endDate: Long): LiveData<List<DetectionResult>>

    @Transaction
    @Query("SELECT * FROM detection_results WHERE userId = :userId AND isDeleted = 0 ORDER BY detectionDate DESC")
    suspend fun getDetectionsSortedDesc(userId: Int?): List<DetectionWithBoundingBoxes>

    @Transaction
    @Query("SELECT * FROM detection_results WHERE userId = :userId ORDER BY detectionDate ASC")
    suspend fun getDetectionsSortedAsc(userId: Int?): List<DetectionWithBoundingBoxes>

    @Transaction
    @Query("SELECT * FROM detection_results WHERE id = :detectionId AND isDeleted = 0")
    suspend fun getDetectionById(detectionId: Int): DetectionWithBoundingBoxes?

    // Delete a single detection by its ID
    @Query("UPDATE detection_results SET isDeleted = 1 WHERE id = :detectionId")
    suspend fun deleteDetectionById(detectionId: Int)

    // Delete all detections for a specific user
    @Query("UPDATE detection_results SET isDeleted = 1 WHERE userId = :userId")
    suspend fun deleteAllDetectionsForUser(userId: Int)


    @Query("""
    UPDATE detection_results 
    SET isDeleted = 1
    WHERE id IN (
        SELECT detectionId FROM bounding_boxes 
        WHERE clsName = :pestName
    ) AND userId = :userId
""")
    suspend fun deleteDetectionsByPestNameForUser(pestName: String, userId: Int)


    @Query("UPDATE detection_results SET isDeleted =1  WHERE userId = :userId AND id IN (SELECT detectionId FROM bounding_boxes WHERE clsName = :pestName)")
    suspend fun deleteDetectionsByPestName(userId: Int?, pestName: String)


    @Query("UPDATE detection_results SET note = :note, updatedAt = :updatedAt WHERE id = :detectionId")
    suspend fun setNoteForDetection(detectionId: Int, note: String, updatedAt: Long)


    @Query("UPDATE detection_results SET note = NULL WHERE id = :detectionId")
    suspend fun deleteNoteForDetection(detectionId: Int)


    @Query("SELECT serverId FROM detection_results WHERE serverId IS NOT NULL AND userId = :userId and isDeleted = 0")
    suspend fun getServerIdsForUser(userId: Int): List<String>


    @Transaction
    @Query("""
    SELECT * FROM detection_results
    WHERE serverId IN (:serverIds)
    AND userId = :userId AND isDeleted =0
""")
    suspend fun getDetectionsWithBoundingBoxesByServerIds(
        serverIds: List<String>,
        userId: Int
    ): List<DetectionWithBoundingBoxes>


    @Query("SELECT serverId FROM detection_results WHERE isDeleted = 1 and userId = :userId")
    suspend fun getSoftDeletedServerIds(userId : Int): List<String>



    @Query("DELETE FROM detection_results WHERE serverId IN (:serverIds)")
    suspend fun deleteDetectionsByServerIds(serverIds: List<String>)


    @Query("SELECT serverId, updatedAt, note FROM detection_results WHERE userId = :userId AND isDeleted = 0 AND serverId IS NOT NULL")
    suspend fun getDetectionNotesForSync(userId: Int?): List<DetectionNoteUpdate>


    @Query("UPDATE detection_results SET note = :note, updatedAt = :updatedAt WHERE serverId = :serverId")
    suspend fun updateNoteAndUpdatedAtByServerId(serverId: String, note: String, updatedAt: Long)



}