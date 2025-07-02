package com.bedjamahdi.scanpestai.ViewModels.detection_result

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedjamahdi.scanpestai.MyApp
import com.bedjamahdi.scanpestai.RoomDatabase.BoundingBox
import com.bedjamahdi.scanpestai.RoomDatabase.BoundingBoxDao
import com.bedjamahdi.scanpestai.RoomDatabase.DetectionResult
import com.bedjamahdi.scanpestai.RoomDatabase.DetectionResultDao
import com.bedjamahdi.scanpestai.data.user.DetectionWithBoundingBoxes
import com.bedjamahdi.scanpestai.endpoint.detections.detectionEndpoint
import com.bedjamahdi.scanpestai.endpoint.user.userEndpoint
import com.bedjamahdi.scanpestai.network.url
import com.bedjamahdi.scanpestai.preferences.Globals
import com.bedjamahdi.scanpestai.repository.detection.DetectionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.UUID
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.Locale

class DetectionSaveViewModel(
    application: Application, // Pass Application as a constructor parameter
    private val detectionResultDao: DetectionResultDao,
    private val boundingBoxDao: BoundingBoxDao
) : AndroidViewModel(application) {


    val endpoint = detectionEndpoint.createEndpoint()

    val detRepository by lazy { DetectionRepository(endpoint) }  // ✅ Inject the repo

    private val _saveStatus = MutableStateFlow<Boolean?>(null)
    val saveStatus: StateFlow<Boolean?> = _saveStatus

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing

    private val _syncCompletedEvent = MutableStateFlow<SyncResult?>(null)
    val syncCompletedEvent: StateFlow<SyncResult?> = _syncCompletedEvent




    sealed class SyncResult {
        object Success : SyncResult()
        data class Failure(val errorMessage: String) : SyncResult()
    }

    fun saveDetection(
        userId: Int,
        imageUri: String,
        boundingBoxes: List<com.bedjamahdi.scanpestai.model.BoundingBox>,
        inferenceTime: Long
    ) {
        val localGeneratedServerId = UUID.randomUUID().toString()
        val currentTime = System.currentTimeMillis()
        val contentResolver = getApplication<Application>().contentResolver
        val uri = Uri.parse(imageUri)

        viewModelScope.launch {
            try {
                // ✅ Try to persist URI permission only if it's a content URI (usually from gallery)
                if (uri.scheme == "content") {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    try {
                        contentResolver.takePersistableUriPermission(uri, takeFlags)
                       // Log.d("DetectionSaveViewModel", "✅ Persistable permission granted for URI: $uri")
                    } catch (e: SecurityException) {
                       // Log.w("DetectionSaveViewModel", "⚠️ Persistable permission NOT granted (maybe already persisted or not granted temporarily): ${e.message}")
                    }
                } else {
                   // Log.d("DetectionSaveViewModel", "📸 Not a gallery content URI, skipping persistable permission: $uri")
                }

                // ✅ Save detection metadata to Room
                val detectionResult = DetectionResult(
                    userId = userId,
                    imageUri = imageUri,
                    timestamp = inferenceTime,
                    isSynced = false,
                    detectionDate = currentTime,
                    serverId = localGeneratedServerId,
                    updatedAt = System.currentTimeMillis()
                )

                val detectionId = detectionResultDao.insertDetectionResult(detectionResult)

                // ✅ Save bounding boxes
                val roomBoundingBoxes = boundingBoxes.map {
                    BoundingBox(
                        detectionId = detectionId.toInt(),
                        x1 = it.x1, y1 = it.y1, x2 = it.x2, y2 = it.y2,
                        cx = it.cx, cy = it.cy, w = it.w, h = it.h,
                        cnf = it.cnf, cls = it.cls, clsName = it.clsName
                    )
                }

                boundingBoxDao.insertBoundingBoxes(roomBoundingBoxes)

                _saveStatus.value = true
               // Log.d("DetectionSaveViewModel", "✅ Detection and bounding boxes saved successfully")

            } catch (e: Exception) {
              //  Log.e("DetectionSaveViewModel", "❌ Error saving detection or bounding boxes", e)
                _saveStatus.value = false
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
            detectionResultDao.setNoteForDetection(detectionId, note , System.currentTimeMillis())
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


    fun syncAll(userId: Int, authToken: String) {
        viewModelScope.launch {
            try {
                _isSyncing.value = true
                _syncCompletedEvent.value = null // Reset previous result


                // Step 1: Sync local server IDs with cloud
                val syncResult = syncLocalServerIdsWithCloud(userId)
                if (!syncResult) throw SyncException("failed_sync_cloud")

                // Step 2: Handle soft deletes
                val deleteResult = softDeleteLocalDetections(authToken, userId)
                if (!deleteResult) throw SyncException("failed_soft_deletes")

                // Step 3: Sync soft deleted items
                val syncDeletedResult = syncSoftDeletedDetections()
                if (!syncDeletedResult) throw SyncException("failed_sync_deleted")

                // Step 4: Sync notes
                val notesResult = syncNotes(authToken, userId)
                if (!notesResult) throw SyncException("failed_sync_notes")



                // In syncAll method, right before emitting success:
              //  Log.d("SyncViewModel", "🚀 About to emit SyncResult.Success")
                _syncCompletedEvent.emit(SyncResult.Success)
              //  Log.d("SyncViewModel", "✅ SyncResult.Success emitted")
            } catch (e: SyncException) {
                val localizedError = getLocalizedErrorMessage(e.errorKey)
                _syncCompletedEvent.emit(SyncResult.Failure(localizedError))
             //   Log.e("SyncAll", "Sync failed: ${e.errorKey}", e)
            } catch (e: Exception) {
                val localizedError = getLocalizedErrorMessage("unknown_error")
                _syncCompletedEvent.emit(SyncResult.Failure(localizedError))
             //   Log.e("SyncAll", "Sync failed", e)
            } finally {
                _isSyncing.value = false
            }
        }
    }

    // Custom exception class for sync errors
    class SyncException(val errorKey: String) : Exception(errorKey)

    // Helper function in ViewModel to get localized error messages
    private fun getLocalizedErrorMessage(errorKey: String): String {
        // You can get the current language from your app's locale
        val currentLanguage = getCurrentLanguage() // Implement this method based on your app's language management

        return when (currentLanguage) {
            "fr" -> when (errorKey) {
                "failed_sync_cloud" -> "Échec de la synchronisation avec le cloud"
                "failed_soft_deletes" -> "Échec de la gestion des suppressions"
                "failed_sync_deleted" -> "Échec de la synchronisation des éléments supprimés"
                "failed_sync_notes" -> "Échec de la synchronisation des notes"
                "unknown_error" -> "Erreur inconnue"
                else -> "Erreur inconnue"
            }
            "ar" -> when (errorKey) {
                "failed_sync_cloud" -> "فشل في المزامنة مع السحابة"
                "failed_soft_deletes" -> "فشل في معالجة الحذف"
                "failed_sync_deleted" -> "فشل في مزامنة العناصر المحذوفة"
                "failed_sync_notes" -> "فشل في مزامنة الملاحظات"
                "unknown_error" -> "خطأ غير معروف"
                else -> "خطأ غير معروف"
            }
            else -> when (errorKey) { // Default to English
                "failed_sync_cloud" -> "Failed to sync with cloud"
                "failed_soft_deletes" -> "Failed to handle soft deletes"
                "failed_sync_deleted" -> "Failed to sync deleted items"
                "failed_sync_notes" -> "Failed to sync notes"
                "unknown_error" -> "Unknown error occurred"
                else -> "Unknown error occurred"
            }
        }
    }






    fun getCurrentLanguage(): String {
        return Locale.getDefault().language // e.g. "en", "fr", "ar"
    }






    suspend fun syncLocalServerIdsWithCloud(userId: Int): Boolean {
        return try {
            val authToken = Globals.savedToken ?: throw Exception("No auth token available")
            val localServerIds = detectionResultDao.getServerIdsForUser(userId)
            val response = detRepository.sendIds(localServerIds, authToken)

            if (response.isSuccessful) {
                val body = response.body()
                if (body != null) {
                 //   Log.d("SYNC", "Server response received: $body")
                    
                    body.detectionsToSend.forEach { detectionToSend ->
                        val detection = detectionToSend.detection
                        val boxes = detectionToSend.boundingBoxes
                        
                        val localImagePath = downloadAndSaveImage(
                            context = MyApp.getContext(),
                            imageUrl = if (detection.imageUri.startsWith("http")) {
                                detection.imageUri
                            } else {
                                url.trimEnd('/') + "/" + detection.imageUri.trimStart('/')
                            },
                            imageFileName = "${UUID.randomUUID()}.png"
                        ) ?: throw Exception("Failed to download image")

                        val localDetectionId = detectionResultDao.insertDetectionResult(
                            DetectionResult(
                                userId = detection.userId,
                                imageUri = localImagePath,
                                timestamp = detection.timestamp,
                                isSynced = detection.isSynced,
                                detectionDate = detection.detectionDate,
                                serverId = detection.serverId,
                                note = detection.note,
                                updatedAt = System.currentTimeMillis()
                            )
                        )

                        val roomBoxes = boxes.map { box ->
                            BoundingBox(
                                detectionId = localDetectionId.toInt(),
                                x1 = box.x1, y1 = box.y1, x2 = box.x2, y2 = box.y2,
                                cx = box.cx, cy = box.cy, w = box.w, h = box.h,
                                cnf = box.cnf, cls = box.cls, clsName = box.clsName
                            )
                        }
                        boundingBoxDao.insertBoundingBoxes(roomBoxes)
                    }

                    body.detectionsNeededFromPhone?.let { neededServerIds ->
                        processDetectionsNeededFromPhone(neededServerIds, authToken, userId)
                    }

                  //  Log.d("SYNC", "✅ Sync success! ${body.detectionsToSend.size} detections saved.")
                    true
                } else {
                  //  Log.e("SYNC", "❌ Empty response body")
                    false
                }
            } else {
                //   Log.e("SYNC", "❌ Server returned error: ${response.code()}")
                false
            }
        } catch (e: Exception) {
            //    Log.e("SYNC", "❌ Exception during sync", e)
            false
        }
    }

    suspend fun softDeleteLocalDetections(authToken: String, userId: Int): Boolean {
        return try {
            val softDeletedIds = detectionResultDao.getSoftDeletedServerIds(userId)
            if (softDeletedIds.isEmpty()) {
                //    Log.d("SoftDelete", "No soft deleted IDs found locally.")
                return true
            }

            val response = detRepository.softDeleteLocalDetections(softDeletedIds, authToken)
            if (response.isSuccessful) {
                detectionResultDao.deleteDetectionsByServerIds(softDeletedIds)
                //    Log.d("SoftDelete", "Successfully deleted detections locally and on server.")
                true
            } else {
                //    Log.e("SoftDelete", "Error deleting on server: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            //    Log.e("SoftDelete", "Exception during soft delete", e)
            false
        }
    }

    suspend fun syncSoftDeletedDetections(): Boolean {
        return try {
            val token = Globals.savedToken ?: throw Exception("No auth token")
            val response = detRepository.getSoftDeletedDetectionsFromServer(token)
            
            if (response.isSuccessful) {
                val deletedIds = response.body()?.deletedIds ?: emptyList()
                if (deletedIds.isNotEmpty()) {
                    detectionResultDao.deleteDetectionsByServerIds(deletedIds)
                    //     Log.d("SyncSoftDelete", "Successfully deleted local detections: $deletedIds")
                }
                true
            } else {
                //    Log.e("SyncSoftDelete", "Server error: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            //    Log.e("SyncSoftDelete", "Exception during sync", e)
            false
        }
    }

    suspend fun syncNotes(authToken: String, userId: Int): Boolean {
        return try {
            val localDetections = detectionResultDao.getDetectionNotesForSync(userId)
            val response = detRepository.updateNotesOnServer(authToken, localDetections)
            
            if (response.isSuccessful) {
                response.body()?.detections?.forEach { serverDetection ->
                    val serverId = serverDetection.serverId
                    val note = serverDetection.note
                    val updatedAt = serverDetection.updatedAt
                    
                    if (note != null && updatedAt != null) {
                        detectionResultDao.updateNoteAndUpdatedAtByServerId(
                            serverId = serverId,
                            note = note,
                            updatedAt = updatedAt
                        )
                    }
                }
                //    Log.d("SyncNotes", "Successfully synced notes with server.")
                true
            } else {
                //     Log.e("SyncNotes", "Server error: ${response.errorBody()?.string()}")
                false
            }
        } catch (e: Exception) {
            //    Log.e("SyncNotes", "Error syncing notes", e)
            false
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



    private fun processDetectionsNeededFromPhone(
        neededServerIds: List<String>,
        authToken: String ,
        userId: Int
    ) {

        viewModelScope.launch {
            try {
                // 1️⃣ Get detections + boxes from DB
                val detectionsWithBoxes = detectionResultDao.getDetectionsWithBoundingBoxesByServerIds(
                    neededServerIds,
                    userId // fallback if no user ID
                )

                // 2️⃣ Send them to the server
                formatAndSendDetectionsToServer(
                   detectionsWithBoxes,
                    authToken,
                    detRepository
                )
            } catch (e: Exception) {
                //     Log.e("SYNC", "❌ Failed to process needed detections", e)
            }
        }
    }







    private fun formatDetectionsForServer(
        detections: List<DetectionWithBoundingBoxes>
    ): Pair<String, Map<String, File>> {
        val detectionsJsonArray = mutableListOf<JSONObject>()
        val imageKeyToFileMap = mutableMapOf<String, File>()
        val context = MyApp.getContext()

        detections.forEachIndexed { index, detectionWithBoxes ->
            val detection = detectionWithBoxes.detection
            val boxes = detectionWithBoxes.boundingBoxes

            val boundingBoxesJson = JSONArray().apply {
                boxes.forEach { box ->
                    put(
                        JSONObject().apply {
                            put("x1", box.x1)
                            put("y1", box.y1)
                            put("x2", box.x2)
                            put("y2", box.y2)
                            put("cx", box.cx)
                            put("cy", box.cy)
                            put("w", box.w)
                            put("h", box.h)
                            put("cnf", box.cnf)
                            put("cls", box.cls)
                            put("cls_name", box.clsName)
                        }
                    )
                }
            }

            val imageKey = "image_$index"
            try {
                val uri = Uri.parse(detection.imageUri)
                
                // Take persistable permission if needed
                if (uri.scheme == "content") {
                    try {
                        val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                                Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                        context.contentResolver.takePersistableUriPermission(uri, takeFlags)
                        //    Log.d("SYNC", "✅ Persistable permission granted for URI: $uri")
                    } catch (e: SecurityException) {
                        //    Log.e("SYNC", "❌ Failed to take persistable permission: ${e.message}")
                        // Continue anyway as we might still be able to open the stream
                    }
                }

                val inputStream = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val tempFile = File.createTempFile("upload_image_$index", ".jpg", context.cacheDir)
                    tempFile.outputStream().use { output ->
                        inputStream.copyTo(output)
                    }
                    imageKeyToFileMap[imageKey] = tempFile
                    inputStream.close()
                } else {
                    //    Log.w("SYNC", "⚠️ Could not resolve image URI to file: ${detection.imageUri}")
                    return@forEachIndexed
                }
            } catch (e: Exception) {
                //     Log.e("SYNC", "❌ Error reading image file: ${detection.imageUri}", e)
                return@forEachIndexed
            }

            val detectionJson = JSONObject().apply {
                put("serverid", detection.serverId)
                put("timestamp", detection.timestamp)
                put("detection_date", detection.detectionDate)
                put("note", detection.note)
                put("image", imageKey)
                put("bounding_boxes", boundingBoxesJson)
                put("updated_at1", detection.updatedAt)
            }

            detectionsJsonArray.add(detectionJson)
        }

        val detectionsJsonString = detectionsJsonArray.toString()
        return Pair(detectionsJsonString, imageKeyToFileMap)
    }



    private fun formatAndSendDetectionsToServer(
        detections: List<DetectionWithBoundingBoxes>,
        authToken: String,
        repository: DetectionRepository
    ) {
        viewModelScope.launch {
            try {
                val (detectionsJson, imageFileMap) = formatDetectionsForServer(detections)

                // Convert images to MultipartBody
                val imageParts = imageFileMap.map { (key, file) ->
                    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                    MultipartBody.Part.createFormData(key, file.name, requestFile)
                }

                val response = repository.sendDetectionsToServer(authToken, detectionsJson, imageParts)

                if (response.isSuccessful) {
                    //     Log.d("SYNC", "Detections successfully sent to server.")
                } else {
                    //    Log.e("SYNC", "Failed to sync: ${response.code()} - ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                //     Log.e("SYNC", "Error during sync: ${e.localizedMessage}")
            }
        }
    }





    fun clearSyncResult() {
        _syncCompletedEvent.value = null
    }




    fun forceRefreshDetections(userId: Int?, selectedPest: String? = null, isDescending: Boolean = true) {
        viewModelScope.launch {
        //    Log.d("DetectionSaveViewModel", "🔄 Starting force refresh for userId: $userId")

            // Get fresh data from database
            val freshDetections = detectionResultDao.getDetectionsByUser(userId)
        //    Log.d("DetectionSaveViewModel", "📊 Fresh from DB: ${freshDetections.size} detections")

            allDetections = freshDetections

            // Apply filters
            applyFilters(
                pestName = if (selectedPest == "None") null else selectedPest,
                isDescending = isDescending
            )

        //    Log.d("DetectionSaveViewModel", "✅ After filtering: ${_detections.value.size} detections shown")
        }
    }




}