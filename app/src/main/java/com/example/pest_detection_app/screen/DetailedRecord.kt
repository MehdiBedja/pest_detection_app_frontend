package com.example.pest_detection_app.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.model.BoundingBox
import com.example.pest_detection_app.ui.theme.AccentGreen
import com.example.pest_detection_app.ui.theme.DarkBackground
import kotlinx.coroutines.delay

@Composable
fun DetailItemScreen(
    navController: NavController,
    viewModel: DetectionViewModel = viewModel(),
    getViewModel: DetectionSaveViewModel = viewModel(),
    detectionId: Int,
    userViewModel: LoginViewModel
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var isEditingNote by remember { mutableStateOf(false) }

    val context = LocalContext.current

    Log.d("DetailItemScreen", "Fetching detection with ID: $detectionId")
    getViewModel.getDetectionById(detectionId)

    val detection by getViewModel.detection.collectAsState()
    val bitmap by viewModel.bitmap1.collectAsState()
    val savedUserId by userViewModel.userId.collectAsState()

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

        noteText = detection?.detection?.note ?: ""

        val uriString = detection!!.detection.imageUri
        if (uriString.isNullOrEmpty()) {
            Log.e("DetailItemScreen", "❌ Error: imageUri is empty! Waiting for data...")
            return@LaunchedEffect
        }

        val imageUri = Uri.parse(uriString)
        Log.d("DetailItemScreen", "📷 Received image URI: $imageUri")

        try {
            // For gallery images, try to take permission
            if (imageUri.toString().startsWith("content://com.android.providers.media")) {
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(imageUri, takeFlags)
                    Log.d("DetailItemScreen", "✅ Permission granted for gallery image: $imageUri")
                } catch (e: SecurityException) {
                    Log.e("DetailItemScreen", "❌ Failed to take permission, but continuing: ${e.message}")
                }
            }

            delay(500)
            viewModel.loadPastDetection(boundingBoxesModel ?: emptyList(), imageUri)
        } catch (e: Exception) {
            Log.e("DetailItemScreen", "❌ Error loading detection: ${e.message}", e)
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = DarkBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            if (bitmap == null) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            } else {
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
                                        context = context
                                    )
                                }
                            }
                        }

                        // Note Section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                stringResource(R.string.note_label),
                                style = MaterialTheme.typography.titleLarge,
                                color = AccentGreen
                            )

                            if (noteText.isEmpty() && !isEditingNote) {
                                Button(
                                    onClick = { isEditingNote = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(stringResource(R.string.add_note))
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF1F1F1)
                                    ),
                                    elevation = CardDefaults.cardElevation(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        if (isEditingNote) {
                                            OutlinedTextField(
                                                value = noteText,
                                                onValueChange = { noteText = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = { Text(stringResource(R.string.write_note_hint)) }
                                            )
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 8.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Button(
                                                    onClick = {
                                                        isEditingNote = false
                                                        noteText = detection?.detection?.note ?: ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.Gray
                                                    )
                                                ) {
                                                    Text(stringResource(R.string.cancel))
                                                }

                                                Button(
                                                    onClick = {
                                                        getViewModel.setNoteForDetection(
                                                            detectionId,
                                                            noteText
                                                        )
                                                        isEditingNote = false
                                                        getViewModel.getDetectionById(detectionId)
                                                    }
                                                ) {
                                                    Text(stringResource(R.string.save_note))
                                                }
                                            }
                                        } else {
                                            Text(
                                                text = noteText,
                                                style = MaterialTheme.typography.bodyLarge,
                                                modifier = Modifier.padding(bottom = 8.dp)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Button(onClick = { isEditingNote = true }) {
                                                    Text(stringResource(R.string.modify_note))
                                                }
                                                Button(
                                                    onClick = {
                                                        getViewModel.setNoteForDetection(
                                                            detectionId,
                                                            ""
                                                        )
                                                        noteText = ""
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = Color.Red
                                                    )
                                                ) {
                                                    Text(stringResource(R.string.delete_note), color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Delete Button
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { showDeleteDialog = true },
                                colors = ButtonDefaults.buttonColors(Color.Red),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                Text(stringResource(R.string.delete_detection), color = Color.White)
                            }
                        }
                    }
                }
            }

            // Back Button
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                containerColor = Color.Green,
                contentColor = Color.Black,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = stringResource(R.string.back)
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text(stringResource(R.string.confirm_delete_title)) },
                    text = { Text(stringResource(R.string.confirm_delete_text)) },
                    confirmButton = {
                        Button(
                            onClick = {
                                savedUserId?.let {
                                    getViewModel.deleteDetection(detectionId, it, true)
                                }
                                showDeleteDialog = false
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(Color.Red)
                        ) {
                            Text(stringResource(R.string.delete), color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = false }) {
                            Text(stringResource(R.string.cancel))
                        }
                    }
                )
            }
        }
    }
}
