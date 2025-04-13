package com.example.pest_detection_app.ViewModels.detection_result

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.RoomDatabase.BoundingBox
import com.example.pest_detection_app.RoomDatabase.BoundingBoxDao
import com.example.pest_detection_app.RoomDatabase.DetectionResult
import com.example.pest_detection_app.RoomDatabase.DetectionResultDao
import com.example.pest_detection_app.data.user.DetectionWithBoundingBoxes
import com.example.pest_detection_app.endpoint.detections.detectionEndpoint
import com.example.pest_detection_app.endpoint.user.userEndpoint
import com.example.pest_detection_app.network.url
import com.example.pest_detection_app.preferences.Globals
import com.example.pest_detection_app.repository.detection.DetectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID

class DetectionSaveViewModel(
    application: Application, // Pass Application as a constructor parameter
    private val detectionResultDao: DetectionResultDao,
    private val boundingBoxDao: BoundingBoxDao
) : AndroidViewModel(application) {


    val endpoint = detectionEndpoint.createEndpoint()

    val detRepository by lazy { DetectionRepository(endpoint) }  // ✅ Inject the repo

    private val _saveStatus = MutableStateFlow<Boolean?>(null)
    val saveStatus: StateFlow<Boolean?> = _saveStatus




    fun saveDetection(
        userId: Int,
        imageUri: String,
        boundingBoxes: List<com.example.pest_detection_app.model.BoundingBox>,
        inferenceTime: Long
    ) {

        val localGeneratedServerId = UUID.randomUUID().toString()

        val currentTime = System.currentTimeMillis()
        val contentResolver = getApplication<Application>().contentResolver
        val uri = Uri.parse(imageUri)

        viewModelScope.launch {
            try {
                // ✅ Request persistable permission if needed
                if (imageUri.startsWith("content://com.android.providers.media.documents/") ||
                    imageUri.startsWith("content://com.android.externalstorage.documents/") ||
                    imageUri.startsWith("content://com.android.providers.downloads.documents/")) {

                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                    Log.d("DetectionSaveViewModel", "✅ Persistable permission granted for gallery image: $uri")
                } else {
                    Log.d("DetectionSaveViewModel", "📸 Camera image detected, no persistable permission needed: $uri")
                }

                // ✅ Save DetectionResult
                val detectionResult = DetectionResult(
                    userId = userId,
                    imageUri = imageUri,
                    timestamp = inferenceTime,
                    isSynced = false,
                    detectionDate = currentTime,
                    serverId = localGeneratedServerId // use this for sync reference later

                )
                val detectionId = detectionResultDao.insertDetectionResult(detectionResult)

                // ✅ Save BoundingBoxes
                val roomBoundingBoxes = boundingBoxes.map {
                    BoundingBox(
                        detectionId = detectionId.toInt(),
                        x1 = it.x1, y1 = it.y1, x2 = it.x2, y2 = it.y2,
                        cx = it.cx, cy = it.cy, w = it.w, h = it.h,
                        cnf = it.cnf, cls = it.cls, clsName = it.clsName
                    )
                }

                boundingBoxDao.insertBoundingBoxes(roomBoundingBoxes)

                _saveStatus.value = true  // ✅ Notify success
            } catch (e: Exception) {
                Log.e("DetectionSaveViewModel", "❌ Error saving detection or bounding boxes", e)
                _saveStatus.value = false  // ❌ Notify failure
            }
        }
    }


    fun resetSaveStatus() {
        _saveStatus.value = null
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


    fun syncLocalServerIdsWithCloud(userId: Int) {
        viewModelScope.launch {
            try {
                val authToken = Globals.savedToken ?: ""  // Get the token from Globals
                val localServerIds = detectionResultDao.getServerIdsForUser(userId)
                val response = detRepository.sendIds(localServerIds, authToken)

                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        Log.d("SYNC", " ${body}")
                    }


                    body?.detectionsToSend?.forEach { detectionWithBoxes ->
                        val detection = detectionWithBoxes.detection
                        val boxes = detectionWithBoxes.boundingBoxes
                        Log.d("IMAGE_DOWNLOAD", "🖼️ Attempting to download image from URL: ${detection.imageUri}")

                        // ⬇️ 1️⃣ Download the image from server and save locally
                        val localImagePath = downloadAndSaveImage(
                            context = MyApp.getContext(), // Make sure you have access to application context
                            imageUrl = if (detection.imageUri.startsWith("http")) {
                                detection.imageUri
                            } else {
                                url.trimEnd('/') + "/" + detection.imageUri.trimStart('/')
                            },
                            imageFileName = "${UUID.randomUUID()}.png"
                        )

                        // ⬇️ 2️⃣ Insert detection result using the local image path
                        val localDetectionId = detectionResultDao.insertDetectionResult(
                            DetectionResult(
                                userId = detection.userId,
                                imageUri = localImagePath ?: "", // fallback to empty string if failed
                                timestamp = detection.timestamp,
                                isSynced = detection.isSynced,
                                detectionDate = detection.detectionDate,
                                serverId = detection.serverId,
                                note = detection.note
                            )
                        )

                        // ⬇️ 3️⃣ Insert bounding boxes
                        val roomBoxes = boxes.map { box ->
                            BoundingBox(
                                detectionId = localDetectionId.toInt(),
                                x1 = box.x1,
                                y1 = box.y1,
                                x2 = box.x2,
                                y2 = box.y2,
                                cx = box.cx,
                                cy = box.cy,
                                w = box.w,
                                h = box.h,
                                cnf = box.cnf,
                                cls = box.cls,
                                clsName = box.clsName
                            )
                        }

                        boundingBoxDao.insertBoundingBoxes(roomBoxes)
                    }

                    if (body != null) {
                        Log.d("SYNC", "✅ Sync success! ${body.detectionsToSend.size} detections saved.")
                    }
                } else {
                    Log.e("SYNC", "❌ Server returned error: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SYNC", "❌ Exception during sync", e)
            }
        }
    }



    suspend fun downloadAndSaveImage(
        context: Context,
        imageUrl: String,
        imageFileName: String = UUID.randomUUID().toString() + ".jpg"
    ): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connect()

            val inputStream = BufferedInputStream(url.openStream())
            val file = File(context.filesDir, imageFileName)
            val outputStream = FileOutputStream(file)

            val data = ByteArray(1024)
            var count: Int
            while (inputStream.read(data).also { count = it } != -1) {
                outputStream.write(data, 0, count)
            }

            outputStream.flush()
            outputStream.close()
            inputStream.close()

            return@withContext file.absolutePath // <-- This is the local path to store in DB
        } catch (e: Exception) {
            e.printStackTrace()
            return@withContext null
        }
    }





}