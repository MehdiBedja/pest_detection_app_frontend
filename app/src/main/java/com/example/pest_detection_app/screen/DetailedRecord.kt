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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.model.BoundingBox
import com.example.pest_detection_app.ui.theme.AppTypography
import com.example.pest_detection_app.ui.theme.CustomTextStyles
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
    val context = LocalContext.current

    getViewModel.getDetectionById(detectionId)
    val detection by getViewModel.detection.collectAsState()
    val bitmap by viewModel.bitmap1.collectAsState()
    val savedUserId by userViewModel.userId.collectAsState()

    val boundingBoxesModel = detection?.boundingBoxes?.map {
        BoundingBox(it.x1, it.y1, it.x2, it.y2, it.cx, it.cy, it.w, it.h, it.cnf, it.cls, it.clsName)
    }

    LaunchedEffect(detection) {
        detection?.detection?.imageUri?.takeIf { it.isNotEmpty() }?.let { uriString ->
            val imageUri = Uri.parse(uriString)

            if (imageUri.toString().startsWith("content://com.android.providers.media")) {
                try {
                    val takeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                    context.contentResolver.takePersistableUriPermission(imageUri, takeFlags)
                } catch (e: SecurityException) {
                    //        Log.e("DetailItemScreen", "Failed to take permission: ${e.message}")
                }
            }

            delay(500)
            viewModel.loadPastDetection(boundingBoxesModel ?: emptyList(), imageUri)
        }
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(color = MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { DetectionImageSection(bitmap) }

                detection?.let {
                    itemsIndexed(it.boundingBoxes) { index, box ->
                        PestDetectionCard(
                            pestIndex = index + 1,
                            pestName = box.clsName,
                            confidenceScore = box.cnf,
                            context =context
                        )
                    }
                }

                item {
                    DetectionNoteSection(
                        detectionId = detectionId,
                        getViewModel = getViewModel
                    )
                }

                item {
                    DetectionDeleteSection(onDeleteClick = { showDeleteDialog = true })
                }
            }

            FloatingActionButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
                    title = {
                        Text(
                            text = stringResource(R.string.confirm_delete_title),
                            style = CustomTextStyles.dangerZoneTitle
                        )
                    },
                    text = {
                        Text(
                            text = stringResource(R.string.confirm_delete_text),
                            style = AppTypography.bodyMedium
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                savedUserId?.let {
                                    getViewModel.deleteDetection(detectionId, it, true)
                                }
                                showDeleteDialog = false
                                navController.popBackStack()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.delete),
                                style = CustomTextStyles.buttonText
                            )
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showDeleteDialog = false }) {
                            Text(
                                text = stringResource(R.string.cancel),
                                style = CustomTextStyles.buttonText
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface
                )
            }
        }
    }
}

@Composable
fun DetectionImageSection(bitmap: android.graphics.Bitmap?) {
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

@Composable
fun DetectionNoteSection(
    detectionId: Int,
    getViewModel: DetectionSaveViewModel
) {
    val detection by getViewModel.detection.collectAsState()
    var noteText by remember { mutableStateOf(detection?.detection?.note ?: "") }
    var isEditingNote by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = stringResource(R.string.note_label),
                style = CustomTextStyles.noteHeader,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (noteText.isEmpty() && !isEditingNote) {
                Button(
                    onClick = { isEditingNote = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.add_note),
                        style = CustomTextStyles.buttonText
                    )
                }
            } else {
                if (isEditingNote) {
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        label = {
                            Text(
                                text = stringResource(R.string.write_note_hint),
                                style = AppTypography.labelMedium
                            )
                        },
                        textStyle = CustomTextStyles.noteContent
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = {
                            isEditingNote = false
                            noteText = detection?.detection?.note ?: ""
                        }) {
                            Text(
                                text = stringResource(R.string.cancel),
                                style = CustomTextStyles.buttonText
                            )
                        }
                        Button(onClick = {
                            getViewModel.setNoteForDetection(detectionId, noteText)
                            isEditingNote = false
                        }) {
                            Text(
                                text = stringResource(R.string.save_note),
                                style = CustomTextStyles.buttonText
                            )
                        }
                    }
                } else {
                    Text(
                        text = noteText,
                        style = CustomTextStyles.noteContent,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        OutlinedButton(onClick = { isEditingNote = true }) {
                            Text(
                                text = stringResource(R.string.modify_note),
                                style = CustomTextStyles.buttonText
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                getViewModel.setNoteForDetection(detectionId, "")
                                noteText = ""
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(
                                text = stringResource(R.string.delete_note),
                                style = CustomTextStyles.buttonText
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetectionDeleteSection(onDeleteClick: () -> Unit) {
    Spacer(modifier = Modifier.height(16.dp))
    Button(
        onClick = onDeleteClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = stringResource(R.string.delete_detection),
            style = CustomTextStyles.buttonText
        )
    }
}