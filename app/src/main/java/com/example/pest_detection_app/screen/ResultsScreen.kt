package com.example.pest_detection_app.screen

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.rememberTransformableState
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.ui.theme.AccentGreen
import com.example.pest_detection_app.ui.theme.CardBackground
import com.example.pest_detection_app.ui.theme.DarkBackground
import com.example.pest_detection_app.ui.theme.GrayText
import org.json.JSONArray
import java.io.InputStream

@Composable
fun ResultsScreen(
    navController: NavController,
    imageUri: String?,
    viewModel: DetectionViewModel = viewModel(),
    saveViewModel: DetectionSaveViewModel = viewModel(),
    context: Context,
    userview: LoginViewModel = viewModel()
) {
    val bitmap by viewModel.bitmap.collectAsState()
    val inferenceTime by viewModel.inferenceTime.collectAsState()
    val boundingBoxes by viewModel.boundingBoxes.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val savedUserId by userview.userId.collectAsState()
    val isLoggedIn by userview.isLoggedIn.collectAsState()

    var showLoginDialog by remember { mutableStateOf(false) }

    // Function to handle saving
    fun saveResults() {
        if (isLoggedIn) { // If user is logged in, save the results
            if (savedUserId != null && imageUri != null) {
                saveViewModel.saveDetection(
                    userId = savedUserId!!,
                    imageUri = imageUri,
                    boundingBoxes = boundingBoxes,
                    inferenceTime = inferenceTime
                )
            }
        } else {
            showLoginDialog = true // Show login/signup prompt
        }
    }

    LaunchedEffect(imageUri) {
        imageUri?.let {
            viewModel.processImageFromUri(Uri.parse(it))
        }
    }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
                .background(DarkBackground)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (isProcessing) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item { DetectedImage(bitmap) }

                        item {
                            if (boundingBoxes.isEmpty()) {
                                Text("No pests detected.", fontSize = 18.sp, color = Color.Red)
                            } else {
                                Text(
                                    "Inference Time: ${inferenceTime} ms",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        itemsIndexed(boundingBoxes) { index, box ->
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                PestInfoCard(
                                    pestIndex = index + 1,
                                    pestName = box.clsName,
                                    confidenceScore = box.cnf
                                )
                                PesticideRecommendationCard(pestName = box.clsName, context = context)
                            }
                        }
                    }
                }
                ActionButtons(onSave = { saveResults() })
            }

            // Floating Back Button (add this part)
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
        }


    // Show login/signup dialog if not logged in
    if (showLoginDialog) {
        LoginSignupDialog(navController = navController, onDismiss = { showLoginDialog = false })
    }
}


@Composable
fun LoginSignupDialog(navController: NavController, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("To save your results, you need to log in or sign up.") },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                navController.navigate("login")
            }) {
                Text("Login")
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
                navController.navigate("signup")
            }) {
                Text("Sign Up")
            }
        }
    )
}



@Composable
fun DetectedImage(bitmap: Bitmap?) {
    var scale by remember { mutableStateOf(1f) }
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }

    val state = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 5f)  // Limit zoom between 1x and 5x
        offsetX += panChange.x
        offsetY += panChange.y
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)  // Zoom limit
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY
                ),
            contentAlignment = Alignment.Center
        ) {
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Detected Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } ?: Text("Failed to load image", color = Color.Red)
        }
    }
}




@Composable
fun PestInfoCard(pestIndex: Int, pestName: String, confidenceScore: Float) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Detected Pest : $pestIndex",
                fontSize = 20.sp,
                color = AccentGreen,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,

                )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = pestName,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground ,

                )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Confidence: ${"%.2f".format(confidenceScore * 100)}%",
                fontSize = 14.sp,
                color = GrayText
            )
        }
    }
}


@Composable
fun PesticideRecommendationCard(pestName: String, context: Context) {
    val jsonString = loadJsonFromAssets(context, "pestInfo.json")
    val jsonArray = JSONArray(jsonString)
    val pestInfo = (0 until jsonArray.length())
        .map { jsonArray.getJSONObject(it) }
        .find { it.optString("pest") == pestName }

    val cropCategory = pestInfo?.optString("category") ?: "Unknown"
    val pesticideRecommendation = pestInfo?.optString("recommendation") ?: "No recommendation available"

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Crop Category:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = AccentGreen ,
                fontFamily = FontFamily.Serif,

                )
            Text(
                text = cropCategory,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pesticide Recommendation:",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                color = AccentGreen ,
                fontFamily = FontFamily.Serif,

                )
            Text(
                text = pesticideRecommendation,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = DarkBackground
            )
        }
    }
}


fun loadJsonFromAssets(context: Context, fileName: String): String {
    val inputStream: InputStream = context.assets.open(fileName)
    return inputStream.bufferedReader().use { it.readText() }
}

@Composable
fun ActionButtons(onSave: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(
                onClick = { onSave() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Results")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Button(
                onClick = { /* Share Results */ },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                modifier = Modifier.weight(1f)
            ) {
                Text("Share")
            }
        }
    }
}
