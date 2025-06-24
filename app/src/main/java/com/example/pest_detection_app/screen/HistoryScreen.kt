package com.example.pest_detection_app.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.data.user.DetectionWithBoundingBoxes
import com.example.pest_detection_app.ui.theme.AppTypography
import com.example.pest_detection_app.ui.theme.CustomTextStyles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun getTranslatedPestName(pestName: String): String {
    val currentLanguage = LocalContext.current.resources.configuration.locales[0].language

    return when (currentLanguage) {
        "ar" -> pestNameTranslationsAr[pestName] ?: pestNameTranslationsFr[pestName] ?: pestName
        "fr" -> pestNameTranslationsFr[pestName] ?: pestNameTranslationsAr[pestName] ?: pestName
        else -> pestName // English fallback
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetectionHistoryScreen(
    navController: NavController,
    userViewModel: LoginViewModel,
    viewModel: DetectionSaveViewModel = viewModel(),
) {
    val savedUserId by userViewModel.userId.collectAsState()
    val detectionList by viewModel.detections.collectAsState()
    var selectedPest by remember { mutableStateOf("None") }
    var isDescending by remember { mutableStateOf(true) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    // Load initial detections
    LaunchedEffect(savedUserId) {
        viewModel.getSortedDetections(savedUserId, isDescending)
    }

    Scaffold() { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Top App Bar with custom typography
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.detections_history),
                        style = CustomTextStyles.sectionTitle,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )

            // Action buttons row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pest Selection Dropdown
                var expanded by remember { mutableStateOf(false) }
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = { expanded = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (selectedPest == "None") stringResource(R.string.select_pest) else selectedPest,
                            style = CustomTextStyles.buttonText,
                            color = MaterialTheme.colorScheme.onPrimary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        listOf("None", "Grub", "Mole Cricket", "Wireworm", "Corn Borer", "Aphids",
                            "Beet Armyworm", "Flax Budworm", "Lytta Polita", "Legume Blister beetle",
                            "Blister Beetle", "Miridae", "Prodenia Litura", "Cicadellidae"
                        ).forEach { pest ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = if (pest =="None") stringResource(R.string.select_pest) else getTranslatedPestName(pest),
                                        style = AppTypography.bodyMedium
                                    )
                                },
                                onClick = {
                                    selectedPest = pest
                                    expanded = false
                                    viewModel.getDetectionsByPestName(
                                        if (selectedPest == "None") "" else selectedPest,
                                        isDescending
                                    )
                                }
                            )
                        }
                    }
                }

                // Sorting Button
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = {
                            isDescending = !isDescending
                            viewModel.getDetectionsByPestName(
                                if (selectedPest == "None") "" else selectedPest,
                                isDescending
                            )
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (isDescending) "${stringResource(R.string.date)} ▼" else "${stringResource(R.string.date)} ▲",
                            style = CustomTextStyles.buttonText,
                            color = MaterialTheme.colorScheme.onSecondary,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Delete Button
                Box(
                    modifier = Modifier.weight(1f)
                ) {
                    Button(
                        onClick = { showDeleteDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                    ) {
                        Text(
                            text = if (selectedPest == "None") stringResource(R.string.delete_all) else stringResource(R.string.delete_all),
                            style = CustomTextStyles.buttonText,
                            color = MaterialTheme.colorScheme.onError,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Confirmation Dialog for Deleting
            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    message = if (selectedPest == "None")
                        stringResource(R.string.confirm_delete_all)
                    else
                        stringResource(R.string.confirm_delete_filtered) + "${getTranslatedPestName(selectedPest)}?",

                    onConfirm = {
                        savedUserId?.let {
                            if (selectedPest == "None") {
                                viewModel.deleteAllDetections(it, isDescending)
                            } else {
                                viewModel.deleteDetectionsByPestName(it, selectedPest)
                                selectedPest = "None"
                            }
                        }
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            // Detection List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(detectionList) { detection ->
                    DetectionItem(
                        navController, detection, viewModel, savedUserId, isDescending, {
                            selectedPest = "None"
                        })
                }
            }
        }
    }
}

@Composable
fun DetectionItem(
    navController: NavController,
    detection: DetectionWithBoundingBoxes,
    viewModel: DetectionSaveViewModel,
    userId: Int?,
    isDescending: Boolean,
    resetPestSelection: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("detail_screen/${detection.detection.id}") },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Detection Image
                Image(
                    painter = rememberAsyncImagePainter(detection.detection.imageUri),
                    contentDescription = "Detected Pest",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.date) + " ${formatDate(detection.detection.detectionDate)}",
                        style = CustomTextStyles.dateText,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (detection.boundingBoxes.isNotEmpty()) {
                        detection.boundingBoxes.forEachIndexed { index, box ->
                            Column {
                                Text(
                                    text = stringResource(R.string.detected_pest) + " ${getTranslatedPestName(box.clsName)}",
                                    style = CustomTextStyles.pestName,
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Text(
                                    text = stringResource(R.string.confidence) + " ${box.cnf?.times(100)?.toInt() ?: 0}%",
                                    style = CustomTextStyles.confidence,
                                    fontWeight = FontWeight.Light,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                if (index < detection.boundingBoxes.size - 1) {
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outline,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.no_detection),
                            style = CustomTextStyles.cardTitle,
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Delete Button
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(
                        painterResource(id = R.drawable.ic_delete),
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Confirmation Dialog for Deleting One Detection
            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    message = stringResource(R.string.confirm_delete_one),
                    onConfirm = {
                        viewModel.deleteDetection(detection.detection.id, userId ?: 0, isDescending)
                        resetPestSelection()
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }
        }
    }
}

@Composable
fun ConfirmDeleteDialog(message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                text = message,
                style = AppTypography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Text(
                    text = stringResource(R.string.delete),
                    style = CustomTextStyles.buttonText,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = CustomTextStyles.buttonText,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    )
}

@Composable
fun SortButton(text: String, isDescending: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondary
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = if (isDescending) "$text ▼" else "$text ▲",
            style = CustomTextStyles.buttonText,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

// Function to format the timestamp into a readable date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}