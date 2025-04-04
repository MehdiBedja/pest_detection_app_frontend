package com.example.pest_detection_app.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


/** ✅ Header Component */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader1(pageTitle: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = pageTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            Image(
                painter = painterResource(id = R.drawable.logo), // Replace with your actual logo
                contentDescription = "App Logo",
                modifier = Modifier.size(40.dp).padding(end = 8.dp)
            )
        }
    )
}

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

    Scaffold(
        topBar = { AppHeader1("Detection Records") {} }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFE8F5E9))
        ) {
            // 🔹 Top Actions Row (Dropdown + Sort + Delete)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 🔹 Pest Selection Dropdown
                var expanded by remember { mutableStateOf(false) }
                Box {
                    Button(onClick = { expanded = true }) {
                        Text(if (selectedPest == "None") "Select Pest" else selectedPest)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("None", "Grub", "Mole Cricket", "Wireworm", "Corn Borer", "Aphids",
                            "Beet Armyworm", "Flax Budworm", "Lytta Polita", "Legume Blister beetle",
                            "Blister Beetle", "Miridae", "Prodenia Litura", "Cicadellidae"
                        ).forEach { pest ->
                            DropdownMenuItem(
                                text = { Text(pest) },
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

                // 🔹 Sorting Button
                SortButton("Date", isDescending) {
                    isDescending = !isDescending
                    viewModel.getDetectionsByPestName(
                        if (selectedPest == "None") "" else selectedPest,
                        isDescending
                    )
                }

                // 🔹 Delete Button (Deletes all or only filtered detections)
                Button(
                    onClick = { showDeleteDialog = true },
                    colors = ButtonDefaults.buttonColors(Color.Red),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(if (selectedPest == "None") "Delete All" else "Delete All Shown")
                }
            }

            // 🔹 Confirmation Dialog for Deleting
            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    message = if (selectedPest == "None")
                        "Are you sure you want to delete all detections?"
                    else
                        "Are you sure you want to delete all detections of $selectedPest?",

                    onConfirm = {
                        savedUserId?.let {
                            if (selectedPest == "None") {
                                viewModel.deleteAllDetections(it, isDescending)  // ✅ Correctly calling deleteAllDetections
                            } else {
                                viewModel.deleteDetectionsByPestName(it, selectedPest)  // ✅ Correctly calling deleteDetectionsByPestName
                                selectedPest = "None"  // 🔥 Reset selection after deleting all of that pest type!

                            }
                        }
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            // 🔹 Detection List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                items(detectionList) { detection ->
                    DetectionItem(navController, detection, viewModel, savedUserId, isDescending)
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
    isDescending: Boolean
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { navController.navigate("detail_screen/${detection.detection.id}") },
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEAC8)),
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
                        text = "Inference time: ${detection.detection.timestamp}",
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    Text(
                        text = "Date: ${formatDate(detection.detection.detectionDate)}",
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )

                    if (detection.boundingBoxes.isNotEmpty()) {
                        detection.boundingBoxes.forEachIndexed { index, box ->
                            Column {
                                Text(
                                    text = "Detected Pest: ${box.clsName}",
                                    fontWeight = FontWeight.Medium,
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1
                                )

                                Text(
                                    text = "Confidence: ${box.cnf?.times(100)?.toInt() ?: 0}%",
                                    fontWeight = FontWeight.Light
                                )

                                if (index < detection.boundingBoxes.size - 1) {
                                    Divider(
                                        color = Color.Gray,
                                        thickness = 1.dp,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    } else {
                        Text(
                            text = "No detection found.",
                            fontWeight = FontWeight.Medium,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
                    }
                }

                // Delete Button
                IconButton(onClick = { showDeleteDialog = true }) {
                    Icon(painterResource(id = R.drawable.ic_delete), contentDescription = "Delete", tint = Color.Red)
                }
            }

            // Confirmation Dialog for Deleting One Detection
            if (showDeleteDialog) {
                ConfirmDeleteDialog(
                    message = "Are you sure you want to delete this detection?",
                    onConfirm = {
                        viewModel.deleteDetection(detection.detection.id, userId ?: 0, isDescending)
                        showDeleteDialog = false
                    },
                    onDismiss = { showDeleteDialog = false }
                )
            }

            // Sync Status Indicator
            val syncColor = if (detection.detection.isSynced) Color.Green else Color.Red
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(syncColor)
                    .align(Alignment.End)
            )
        }
    }
}

@Composable
fun ConfirmDeleteDialog(message: String, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Confirm Delete") },
        text = { Text(message) },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(Color.Red)
            ) {
                Text("Delete", color = Color.White)
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}


@Composable
fun SortButton(text: String, isDescending: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(Color(0xFFFF7043)),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(
            text = if (isDescending) "$text ▼" else "$text ▲",
            color = Color.White
        )
    }
}


// Function to format the timestamp into a readable date
fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}