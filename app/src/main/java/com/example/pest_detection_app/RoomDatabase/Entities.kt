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
    val userId: Int, // Foreign key to User
    val serverId: String, // New field for server-side unique identifier
    val imageUri: String, // Local image path
    val timestamp: Long, // Time of detection
    val isSynced: Boolean = false ,// Sync status with cloud
    val detectionDate: Long, // New field (stores date as Unix timestamp)
    val note: String? = null ,
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
