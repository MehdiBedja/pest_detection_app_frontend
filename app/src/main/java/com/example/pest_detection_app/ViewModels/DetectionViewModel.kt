package com.example.pest_detection_app.ViewModels

import android.app.Application
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.pest_detection_app.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream

class DetectionViewModel(application: Application) : AndroidViewModel(application), Detector.DetectorListener {

    private val _bitmap = MutableStateFlow<Bitmap?>(null)
    val bitmap: StateFlow<Bitmap?> = _bitmap.asStateFlow()

    private val _bitmap1 = MutableStateFlow<Bitmap?>(null)
    val bitmap1: StateFlow<Bitmap?> = _bitmap1.asStateFlow()

    private val _inferenceTime = MutableStateFlow(0L)
    val inferenceTime: StateFlow<Long> = _inferenceTime.asStateFlow()

    private val _boundingBoxes = MutableStateFlow<List<BoundingBox>>(emptyList())
    val boundingBoxes: StateFlow<List<BoundingBox>> = _boundingBoxes.asStateFlow()

    private val detector: Detector = Detector(application, Constants.MODEL_PATH, Constants.LABELS_PATH, this)


    private val _isProcessing = MutableStateFlow(false)  // Tracks if detection is running
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private var detectionInProgress = false  // Internal flag to track if detection started


    init {
        detector.setup()
    }

    /**
     * Processes an image selected from the gallery or taken by the camera.
     */
    fun processImageFromUri(imageUri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            _isProcessing.value = true  // Processing started
            detectionInProgress = true  // Mark detection as running

            val image = loadBitmapFromUri(imageUri)
            if (image != null) {
                _bitmap.value = image
                detector.detect(image)  // This will call either onDetect() or onEmptyDetect()
            } else {
                _isProcessing.value = false  // Stop if image fails to load
                detectionInProgress = false
            }
        }
    }


    suspend fun loadPastDetection (boundingBoxes: List<BoundingBox>,
                                   imageUri: Uri) {

        val image = loadBitmapFromUriDetItem(imageUri)
        if (image !=null) {
            _bitmap1.value = image
        }
        _bitmap1.value?.let { originalBitmap ->
            val processedBitmap =
                ImageProcessor.drawBoundingBoxesOnBitmap(originalBitmap, boundingBoxes)
            withContext(Dispatchers.Main) {
                _bitmap1.value = processedBitmap
            }
        }
    }


    /**
     * Callback when object detection is complete.
     */
    override fun onDetect(boundingBoxes: List<BoundingBox>, inferenceTime: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _boundingBoxes.value = boundingBoxes
            _inferenceTime.value = inferenceTime
            //  Log.d("DetectionViewModel", "Inference time updated: $inferenceTime ms")


            _bitmap.value?.let { originalBitmap ->
                val processedBitmap = ImageProcessor.drawBoundingBoxesOnBitmap(originalBitmap, boundingBoxes)
                withContext(Dispatchers.Main) {
                    _bitmap.value = processedBitmap
                    _isProcessing.value = false  // Detection finished
                    detectionInProgress = false
                }
            }
        }
    }


    /**
     * Callback when no objects are detected.
     */
    override fun onEmptyDetect() {
        // Log.e("DetectionViewModel", "No objects detected.")

        viewModelScope.launch {
            _boundingBoxes.value = emptyList()

            // Ensure we only stop processing if detection was actually started
            if (detectionInProgress) {
                _isProcessing.value = false
                detectionInProgress = false
            }
        }
    }

    /**
     * Loads a bitmap from the given URI.
     * This is used for initial image loading from camera/gallery
     */
    private fun loadBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val contentResolver = getApplication<Application>().contentResolver

            // Take persistable URI permission if needed (for gallery images)
            if (uri.scheme == "content") {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                try {
                    contentResolver.takePersistableUriPermission(uri, takeFlags)
                } catch (e: SecurityException) {
                    // Ignore if permission was already granted or can't be taken
                    //         Log.w("DetectionViewModel", "Persistable permission not granted: ${e.message}")
                }
            }

            contentResolver.openInputStream(uri)?.use { inputStream ->
                BitmapFactory.decodeStream(inputStream)
            }
        } catch (e: Exception) {
            //     Log.e("DetectionViewModel", "Error loading image from URI: $uri", e)
            null
        }
    }


    private fun loadBitmapFromUriDetItem(uri: Uri): Bitmap? {
        return try {
            val context = getApplication<Application>()
            val contentResolver = context.contentResolver

            val inputStream = when {
                // For all content URIs (gallery, camera, downloads, etc.)
                uri.scheme == "content" -> {
                    contentResolver.openInputStream(uri)
                }

                // For file URIs
                uri.scheme == "file" -> {
                    FileInputStream(File(uri.path!!))
                }

                // For raw paths
                else -> {
                    FileInputStream(File(uri.toString()))
                }
            }

            inputStream?.use {
                BitmapFactory.decodeStream(it)
            }

        } catch (e: SecurityException) {
            //     Log.e("DetectionViewModel", "❌ Permission error: ${e.message}", e)
            null
        } catch (e: Exception) {
            //     Log.e("DetectionViewModel", "❌ Error loading image: ${e.message}", e)
            null
        }
    }


}