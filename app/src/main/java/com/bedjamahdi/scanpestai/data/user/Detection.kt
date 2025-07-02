package com.bedjamahdi.scanpestai.data.user

import androidx.room.Embedded
import androidx.room.Relation
import com.bedjamahdi.scanpestai.RoomDatabase.BoundingBox
import com.bedjamahdi.scanpestai.RoomDatabase.DetectionResult
import com.google.gson.annotations.SerializedName


data class DetectionWithBoundingBoxes(
    @Embedded val detection: DetectionResult,

    @Relation(
        parentColumn = "id", // Primary key of DetectionResult
        entityColumn = "detectionId" // Foreign key in BoundingBox table
    )
    val boundingBoxes: List<BoundingBox>
)

data class ServerIdsRequest(
    @SerializedName("ids") val ids: List<String>
)


data class ServerIdsRequestDel(
    val server_ids: List<String>
)

data class SoftDeletedDetectionsResponse(
    @SerializedName("deleted_ids")
    val deletedIds: List<String>
)



data class SyncDetectionsResponse(
    @SerializedName("detections_to_send") val detectionsToSend: List<DetectionToSend>,
    @SerializedName("detections_needed_from_phone") val detectionsNeededFromPhone: List<String>
)

data class DetectionToSend(
    @SerializedName("detection") val detection: Detection,
    @SerializedName("boundingBoxes") val boundingBoxes: List<BoundingBoxDto>
)

data class Detection(
    @SerializedName("id") val id: Int,
    @SerializedName("userId") val userId: Int,
    @SerializedName("serverId") val serverId: String,
    @SerializedName("imageUri") val imageUri: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("isSynced") val isSynced: Boolean,
    @SerializedName("detectionDate") val detectionDate: Long,
    @SerializedName("note") val note: String?
)

data class BoundingBoxDto(
    @SerializedName("id") val id: Int,
    @SerializedName("detectionId") val detectionId: Int,
    @SerializedName("x1") val x1: Float,
    @SerializedName("y1") val y1: Float,
    @SerializedName("x2") val x2: Float,
    @SerializedName("y2") val y2: Float,
    @SerializedName("cx") val cx: Float,
    @SerializedName("cy") val cy: Float,
    @SerializedName("w") val w: Float,
    @SerializedName("h") val h: Float,
    @SerializedName("cnf") val cnf: Float,
    @SerializedName("cls") val cls: Int,
    @SerializedName("clsName") val clsName: String
)



data class DetectionNoteUpdate(
    val serverId: String,
    val updatedAt: Long?,
    val note: String?
)


data class DetectionNoteUpdate1(
    val serverId: String,
    val updatedAt1: Long?,
    val note: String?
)


data class NotesSyncRequest(
    val detections: List<DetectionNoteUpdate>
)


data class SyncDetectionsResponse1(
    val detections: List<DetectionNoteUpdate>
)



