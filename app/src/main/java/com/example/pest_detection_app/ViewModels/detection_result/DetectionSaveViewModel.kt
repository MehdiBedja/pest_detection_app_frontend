package com.example.pest_detection_app.ViewModels.detection_result

import android.app.Application
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pest_detection_app.RoomDatabase.BoundingBox
import com.example.pest_detection_app.RoomDatabase.BoundingBoxDao
import com.example.pest_detection_app.RoomDatabase.DetectionResult
import com.example.pest_detection_app.RoomDatabase.DetectionResultDao
import com.example.pest_detection_app.data.user.DetectionWithBoundingBoxes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DetectionSaveViewModel(
    application: Application, // Pass Application as a constructor parameter
    private val detectionResultDao: DetectionResultDao,
    private val boundingBoxDao: BoundingBoxDao
) : AndroidViewModel(application) {

    fun saveDetection(
        userId: Int,
        imageUri: String,
        boundingBoxes: List<com.example.pest_detection_app.model.BoundingBox>,
        inferenceTime: Long
    ) {
        val currentTime = System.currentTimeMillis()
        val contentResolver = getApplication<Application>().contentResolver
        val uri = Uri.parse(imageUri)

        viewModelScope.launch {
            try {
                // ✅ Only request persistable permission if the image comes from the gallery
                if (imageUri.startsWith("content://com.android.providers.media.documents/") ||
                    imageUri.startsWith("content://com.android.externalstorage.documents/") ||
                    imageUri.startsWith("content://com.android.providers.downloads.documents/")) {

                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                    Log.d("DetectionSaveViewModel", "✅ Persistable permission granted for gallery image: $uri")
                } else {
                    Log.d("DetectionSaveViewModel", "📸 Camera image detected, no persistable permission needed: $uri")
                }
            } catch (e: SecurityException) {
                Log.e("DetectionSaveViewModel", "❌ Failed to take persistable URI permission", e)
            }

            val detectionResult = DetectionResult(
                userId = userId,
                imageUri = imageUri,
                timestamp = inferenceTime,
                isSynced = false,
                detectionDate = currentTime
            )
            val detectionId = detectionResultDao.insertDetectionResult(detectionResult)

            val roomBoundingBoxes = boundingBoxes.map {
                BoundingBox(
                    detectionId = detectionId.toInt(),
                    x1 = it.x1, y1 = it.y1, x2 = it.x2, y2 = it.y2,
                    cx = it.cx, cy = it.cy, w = it.w, h = it.h,
                    cnf = it.cnf, cls = it.cls, clsName = it.clsName
                )
            }

            boundingBoxDao.insertBoundingBoxes(roomBoundingBoxes)
        }
    }


    /*   suspend fun getDetResults(userId: Int?): List<DetectionWithBoundingBoxes> {
           return withContext(Dispatchers.IO) {
               val detections = detectionResultDao.getDetectionsByUser(userId)
               detections.map { detection ->
                   val boxes = boundingBoxDao.getBoundingBoxesByImage(detection.id)
                   DetectionWithBoundingBoxes(detection, boxes)
               }
           }
       }

     */

    private val _detections = MutableStateFlow<List<DetectionWithBoundingBoxes>>(emptyList())
    val detections: StateFlow<List<DetectionWithBoundingBoxes>> = _detections


    private val _detections1 = MutableStateFlow<List<DetectionWithBoundingBoxes>>(emptyList())
    val detections1: StateFlow<List<DetectionWithBoundingBoxes>> = _detections1

    private var allDetections: List<DetectionWithBoundingBoxes> = emptyList()

    fun getSortedDetections(userId: Int?, isDescending: Boolean) = viewModelScope.launch {
        allDetections = detectionResultDao.getDetectionsByUser(userId)  // Load all user's detections
        applyFilters(null, isDescending)  // Default to no filtering
    }

    fun getDetectionsByPestName(pestName: String?, isDescending: Boolean) = viewModelScope.launch {
        applyFilters(pestName, isDescending)
    }

    private fun applyFilters(pestName: String?, isDescending: Boolean) {
        var results = allDetections

        if (!pestName.isNullOrEmpty() && pestName != "None") {
            results = results.filter { detection ->
                detection.boundingBoxes.any { it.clsName == pestName }
            }
        }

        // Apply sorting after filtering
        results = if (isDescending) {
            results.sortedByDescending { it.detection.detectionDate }
        } else {
            results.sortedBy { it.detection.detectionDate }
        }

        _detections.value = results
    }

    private val _detection = MutableStateFlow<DetectionWithBoundingBoxes?>(null)
    val detection: StateFlow<DetectionWithBoundingBoxes?> = _detection

    fun getDetectionById(detectionId: Int) {
        viewModelScope.launch {
            val result = detectionResultDao.getDetectionById(detectionId)
            _detection.value = result  // Store in StateFlow
        }
    }


    // 🔹 Delete a single detection by ID
    fun deleteDetection(detectionId: Int, userId: Int, isDescending: Boolean) = viewModelScope.launch {
        detectionResultDao.deleteDetectionById(detectionId)
        getSortedDetections(userId, isDescending)  // ✅ Refresh only the user's detections
    }

    // 🔹 Delete all detections for a user
    fun deleteAllDetections(userId: Int, isDescending: Boolean) = viewModelScope.launch {
        detectionResultDao.deleteAllDetectionsForUser(userId)
        getSortedDetections(userId, isDescending)  // ✅ Refresh only the user's detections
    }



    fun deleteDetectionsByPestName(userId: Int?, pestName: String) = viewModelScope.launch {
        detectionResultDao.deleteDetectionsByPestName(userId, pestName)
        getSortedDetections(userId, isDescending = true)  // Refresh the list
    }


    fun setNoteForDetection(detectionId: Int, note: String) {
        viewModelScope.launch {
            detectionResultDao.setNoteForDetection(detectionId, note)
        }
    }

    fun deleteNoteForDetection(detectionId: Int) {
        viewModelScope.launch {
            detectionResultDao.deleteNoteForDetection(detectionId)
        }
    }

    fun getRecentDetections(userId: Int) {
        viewModelScope.launch {
            _detections1.value =   detectionResultDao.getDetectionsSortedDesc(userId)
        }}



}