package com.example.pest_detection_app.RoomDatabase

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: Int,  // ID from authentication system
    val username: String,
    val email: String,
    val last_name: String?,
    val first_name: String?,
    val phone_number: String?,
    val date_of_birth: String?,
    val date_joined: String?,
    val profile_picture: String?
)



@Entity(
    tableName = "detection_results",
    foreignKeys = [ForeignKey(
        entity = User::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class DetectionResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val serverId: String,
    val imageUri: String,
    val timestamp: Long,
    val isSynced: Boolean = false,
    val detectionDate: Long,
    val note: String? = null,
    val updatedAt: Long? = null, // 🆕 Timestamp of last update
    val isDeleted: Boolean = false // 🆕 For soft delete
)



@Entity(
    tableName = "bounding_boxes",
    foreignKeys = [ForeignKey(
        entity = DetectionResult::class,
        parentColumns = ["id"],
        childColumns = ["detectionId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["detectionId"])] // Update index name
)
data class BoundingBox(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val detectionId: Int,  // Foreign key linking to DetectionResult
    val x1: Float,
    val y1: Float,
    val x2: Float,
    val y2: Float,
    val cx: Float,
    val cy: Float,
    val w: Float,
    val h: Float,
    val cnf: Float,
    val cls: Int,
    val clsName: String
)
