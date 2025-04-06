package com.example.pest_detection_app.screen

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.model.BoundingBox
import com.example.pest_detection_app.ui.theme.AccentGreen
import com.example.pest_detection_app.ui.theme.CardBackground
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
        val context = MyApp.getContext()
        val contentResolver = context.contentResolver

        Log.d("DetailItemScreen", "📷 Received image URI: $imageUri")

        try {
            if (imageUri.toString()
                    .startsWith("content://com.android.providers.media.documents/")
            ) {
                val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                contentResolver.takePersistableUriPermission(imageUri, takeFlags)
                Log.d("DetailItemScreen", "✅ Persistable permission granted for URI: $imageUri")
            } else {
                Log.d(
                    "DetailItemScreen",
                    "📸 Camera image detected, no persistable permission needed: $imageUri"
                )
            }

            delay(500)
            viewModel.loadPastDetection(boundingBoxesModel ?: emptyList(), imageUri)
        } catch (e: SecurityException) {
            Log.e("DetailItemScreen", "❌ Failed to take persistable URI permission", e)
        }
    }

    Scaffold(
    ) { paddingValues ->
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
                        modifier = Modifier
                            .padding(16.dp),
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
                                        context = MyApp.getContext()
                                    )
                                }
                            }
                        }

                        // Note Section
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Note:",
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
                                    Text("➕ Add Note")
                                }
                            } else {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(
                                            0xFFF1F1F1
                                        )
                                    ),
                                    elevation = CardDefaults.cardElevation(6.dp)
                                ) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        if (isEditingNote) {
                                            OutlinedTextField(
                                                value = noteText,
                                                onValueChange = { noteText = it },
                                                modifier = Modifier.fillMaxWidth(),
                                                label = { Text("Write your note...") }
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
                                                    Text("Cancel")
                                                }

                                                Button(
                                                    onClick = {
                                                        getViewModel.setNoteForDetection(
                                                            detectionId,
                                                            noteText
                                                        )
                                                        isEditingNote = false
                                                        getViewModel.getDetectionById(detectionId) // 🔥 Refetch updated detection
                                                    }
                                                ) {
                                                    Text("✅ Save Note")
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
                                                    Text("✏️ Modify Note")
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
                                                    Text("🗑️ Delete Note", color = Color.White)
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
                                Text("Delete Detection", color = Color.White)
                            }
                        }
                    }
                }


            }


            // Floating Back Button (top start)
            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                containerColor = DarkBackground,
                contentColor = Color.White,
                shape = RoundedCornerShape(50)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }

            if (showDeleteDialog) {
                AlertDialog(
                    onDismissRequest = { showDeleteDialog = false },
                    title = { Text("Confirm Delete") },
                    text = { Text("Are you sure you want to delete this detection? This action cannot be undone.") },
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
                            Text("Delete", color = Color.White)
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    }
}