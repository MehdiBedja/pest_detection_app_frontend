package com.example.pest_detection_app.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.model.BoundingBox
import kotlinx.coroutines.delay


@Composable
fun DetailItemScreen(
    navController: NavController,
    viewModel: DetectionViewModel = viewModel(),
    getViewModel: DetectionSaveViewModel = viewModel(),
    detectionId: Int
) {
    Log.d("DetailItemScreen", "Fetching detection with ID: $detectionId")
    getViewModel.getDetectionById(detectionId)

    val detection by getViewModel.detection.collectAsState()
    val bitmap by viewModel.bitmap1.collectAsState()

    Log.d("DetailItemScreen", "Current detection state: $detection")

    val boundingBoxesModel = detection?.boundingBoxes?.map { box ->
        BoundingBox(
            x1 = box.x1, y1 = box.y1,
            x2 = box.x2, y2 = box.y2,
            cx = box.cx, cy = box.cy,
            w = box.w, h = box.h,
            cnf = box.cnf, cls = box.cls,
            clsName = box.clsName
        )
    }

    LaunchedEffect(detection) {
        if (detection == null) {
            Log.e("DetailItemScreen", "❌ Detection data is still NULL, retrying...")
            return@LaunchedEffect
        }

        val uriString = detection!!.detection.imageUri
        if (uriString.isNullOrEmpty()) {
            Log.e("DetailItemScreen", "❌ Error: imageUri is empty! Waiting for data...")
            return@LaunchedEffect
        }

        val imageUri = Uri.parse(uriString)
        val context = MyApp.getContext()
        val contentResolver = context.contentResolver

        Log.d("DetailItemScreen", "📷 Received image URI: $imageUri")

        try {
            if (imageUri.toString().startsWith("content://com.android.providers.media.documents/")) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(imageUri, takeFlags)
                Log.d("DetailItemScreen", "✅ Persistable permission granted for URI: $imageUri")
            } else {
                Log.d("DetailItemScreen", "📸 Camera image detected, no persistable permission needed: $imageUri")
            }

            // Delay slightly to ensure data is available
            delay(500)

            Log.d("DetailItemScreen", "🖼 Loading image in ViewModel...")
            viewModel.loadPastDetection(boundingBoxesModel ?: emptyList(), imageUri)
        } catch (e: SecurityException) {
            Log.e("DetailItemScreen", "❌ Failed to take persistable URI permission", e)
        }
    }


    Scaffold(
        topBar = { AppHeader("Detection Results") { navController.popBackStack() } }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        if (bitmap == null) {
                            Log.d("DetailItemScreen", "Image is still loading, showing progress bar...")
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        } else {
                            Log.d("DetailItemScreen", "Displaying detected image: $bitmap")
                            DetectedImage(bitmap)
                        }

                    }

                    detection?.let {
                        itemsIndexed(it.boundingBoxes) { index, box ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PestInfoCard(
                                    pestIndex = index + 1,
                                    pestName = box.clsName,
                                    confidenceScore = box.cnf
                                )
                                PesticideRecommendationCard(
                                    pestName = box.clsName,
                                    context = MyApp.getContext()
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
