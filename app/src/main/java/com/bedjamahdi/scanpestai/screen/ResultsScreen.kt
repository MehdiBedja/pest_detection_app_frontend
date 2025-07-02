package com.bedjamahdi.scanpestai.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.view.Gravity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.bedjamahdi.scanpestai.R
import com.bedjamahdi.scanpestai.ViewModels.DetectionViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.bedjamahdi.scanpestai.ViewModels.detection_result.DetectionSaveViewModel
import com.bedjamahdi.scanpestai.ViewModels.user.LoginViewModel
import com.bedjamahdi.scanpestai.screen.navigation.Screen
import com.bedjamahdi.scanpestai.ui.theme.AppTypography
import com.bedjamahdi.scanpestai.ui.theme.CustomTextStyles
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import org.json.JSONArray
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale



val pestNameTranslationsAr = mapOf(
    "Grub" to "اليرقة البيضاء",
    "Mole Cricket" to "صرصور الحقل",
    "Wireworm" to "الدودة السلكية",
    "Corn Borer" to "ثاقبة الذرة",
    "Aphids" to "المنّ",
    "Beet Armyworm" to "دودة الجيش",
    "Flax Budworm" to "يرقة براعم الكتان",
    "Lytta Polita" to "ذبابة الزيتون",
    "Legume Blister beetle" to "خنفساء التقرح البقولية",
    "Blister Beetle" to "خنفساء التقرح",
    "Miridae" to "حشرات الميري",
    "Prodenia Litura" to "يرقة الحشد",
    "Cicadellidae" to "النطاطات"
)

val pestNameTranslationsFr = mapOf(
    "Grub" to "Vers blanc",
    "Mole Cricket" to "Grillon des champs",
    "Wireworm" to "Ver fil de fer",
    "Corn Borer" to "Foreur du maïs",
    "Aphids" to "Pucerons",
    "Beet Armyworm" to "Chenille de la betterave",
    "Flax Budworm" to "Chenille du lin",
    "Lytta Polita" to "Mouche de l'olive",
    "Legume Blister beetle" to "Cantharide des légumineuses",
    "Blister Beetle" to "Cantharide",
    "Miridae" to "Mirides",
    "Prodenia Litura" to "Chenille défoliatrice",
    "Cicadellidae" to "Cicadelles"
)







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

    val saveStatus by saveViewModel.saveStatus.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    var hasSavedOnce by rememberSaveable { mutableStateOf(false) }



    fun saveResults() {

        if (hasSavedOnce) return  // Don't save again

        if (isLoggedIn) {
            if (savedUserId != null && imageUri != null) {
                saveViewModel.saveDetection(
                    userId = savedUserId!!,
                    imageUri = imageUri,
                    boundingBoxes = boundingBoxes,
                    inferenceTime = inferenceTime
                )
            }
        } else {
            showLoginDialog = true
        }
    }

    LaunchedEffect(imageUri) {
        imageUri?.let {
            viewModel.processImageFromUri(Uri.parse(it))
        }
    }

    val resultsSaved =stringResource(R.string.results_saved)
    val resultsFailed =stringResource(R.string.results_save_failed)


    LaunchedEffect(saveStatus) {
        saveStatus?.let { success ->
            coroutineScope.launch {
                val message = if (success) {
                    hasSavedOnce = true
                    resultsSaved
                } else {
                    resultsFailed
                }
                val toast = Toast.makeText(context, message, Toast.LENGTH_SHORT)
                toast.setGravity(Gravity.CENTER_VERTICAL or Gravity.CENTER_HORIZONTAL, 0, 0)
                toast.show()

                saveViewModel.resetSaveStatus()
            }
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (isProcessing) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
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
                            Text(
                                stringResource(R.string.no_pests_detected),
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.error
                            )
                        } else {
                            Text(
                                stringResource(R.string.inference_time) + " ${inferenceTime} ms" ,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    itemsIndexed(boundingBoxes) { index, box ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            PestDetectionCard(
                                pestIndex = index + 1,
                                pestName = box.clsName,
                                confidenceScore = box.cnf,
                                context =context
                            )
                        }
                    }
                }
            }

            ActionButtons(isLoggedIn , onSave = { saveResults() } ,  isSaved = hasSavedOnce , navController)
        }

        // Back Button
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
                contentDescription = "Back"
            )
        }

    }

    if (showLoginDialog) {
        LoginSignupDialog(navController = navController, onDismiss = { showLoginDialog = false })
    }
}


@Composable
fun LoginSignupDialog(navController: NavController, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = {
            Text(
                "To save your results, you need to log in or sign up.",
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onDismiss()
                    navController.navigate("login")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.login))
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    onDismiss()
                    navController.navigate("signup")
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.sign_up))
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.onSurface
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
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
            } ?: Text(
                stringResource(R.string.failed_to_load_image),
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}



@Composable
fun PestDetectionCard(
    pestIndex: Int,
    pestName: String,
    confidenceScore: Float,
    context: Context
) {
    val currentLanguage = LocalContext.current.resources.configuration.locales[0].language

    val displayedPestName = when (currentLanguage) {
        "ar" -> pestNameTranslationsAr[pestName] ?: pestNameTranslationsFr[pestName] ?: pestName
        "fr" -> pestNameTranslationsFr[pestName] ?: pestNameTranslationsAr[pestName] ?: pestName
        else -> pestName
    }

    // Load pesticide recommendation data
    val jsonString = remember(currentLanguage) { loadLocalizedJson(context) }
    val jsonArray = remember(jsonString) { JSONArray(jsonString) }

    val pestInfo = remember(pestName) {
        (0 until jsonArray.length())
            .map { jsonArray.getJSONObject(it) }
            .find { it.optString("pest") == pestName }
    }

    val cropCategory = pestInfo?.optString("category") ?:"unknown_category"
    val pesticideRecommendation = pestInfo?.optString("recommendation")
        ?: "no_recommendation_available"

    Card(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .shadow(6.dp, RoundedCornerShape(24.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pest Info Section
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stringResource(R.string.detected_pest)} #$pestIndex",
                        style = CustomTextStyles.treatmentTitle,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.confidence),
                            style = AppTypography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Text(
                            text = "${"%.2f".format(confidenceScore * 100)}%",
                            style = CustomTextStyles.confidenceBadge,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Text(
                    text = displayedPestName,
                    style = CustomTextStyles.pestName,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Crop Category Section
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.crop_category),
                    style = CustomTextStyles.treatmentTitle,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = cropCategory,
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Divider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                thickness = 1.dp,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Pesticide Recommendation Section
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(R.string.pesticide_recommendation),
                    style = CustomTextStyles.treatmentTitle,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = pesticideRecommendation,
                    style = AppTypography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

fun loadLocalizedJson(context: Context): String {
    val language = Locale.getDefault().language
    val fileName = when (language) {
        "ar" -> "pests_ar.json"
        "fr" -> "pests_fr.json"
        else -> "pestInfo.json"  // English default
    }
    return context.assets.open(fileName).bufferedReader().use { it.readText() }
}



@Composable
fun ActionButtons(
    isLoggedIn: Boolean,
    onSave: () -> Unit,
    isSaved: Boolean,
    navController: NavController
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        if (isSaved && isLoggedIn) {
            Button(
                onClick = { navController.navigate(Screen.History.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
            ) {
                Text(stringResource(R.string.see_detections_history))
            }
        } else {
            Button(
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                enabled = !isSaved
            ) {
                Text(stringResource(R.string.save_results))
            }
        }

        ScanButton1(navController)

    }
}



@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanButton1(navController: NavController) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // Add states to track pending actions
    var pendingCameraLaunch by remember { mutableStateOf(false) }
    var pendingGalleryLaunch by remember { mutableStateOf(false) }

    // Permission states using Accompanist
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)


    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                //     Log.d("ScanButton1", "✅ Persistable URI permission granted: $it")
            } catch (e: SecurityException) {
                //     Log.e("ScanButton1", "❌ Failed to persist URI permission", e)
            }
            navController.navigate("results/${Uri.encode(it.toString())}")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri.value?.let { navController.navigate("results/${Uri.encode(it.toString())}") }
        }
    }



    // Helper functions
    fun launchGallery() {
        galleryLauncher.launch(arrayOf("image/*"))
    }

    fun launchCamera() {
        val uri = createImageUri(context)
        if (uri != null) {
            cameraImageUri.value = uri
            cameraLauncher.launch(uri)
        } else {
            Toast.makeText(context, "Failed to create image file", Toast.LENGTH_SHORT).show()
        }
    }

    // Auto-launch camera when permission is granted and pending
    LaunchedEffect(cameraPermissionState.status.isGranted, pendingCameraLaunch) {
        if (cameraPermissionState.status.isGranted && pendingCameraLaunch) {
            pendingCameraLaunch = false
            launchCamera()
        }
    }





    fun handleCameraClick() {
        when {
            cameraPermissionState.status.isGranted -> {
                launchCamera()
            }
            cameraPermissionState.status.shouldShowRationale -> {
                // Set pending state and request permission
                pendingCameraLaunch = true
                cameraPermissionState.launchPermissionRequest()
            }
            else -> {
                // Set pending state and request permission
                pendingCameraLaunch = true
                cameraPermissionState.launchPermissionRequest()
            }
        }
    }


    Box(modifier = Modifier) {
        Button(
            onClick = { showOptions = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
        ) {
            Text(text = stringResource(R.string.try_again))
        }

        if (showOptions) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = {
                    Text(
                        stringResource(R.string.choose_option),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                text = {
                    Text(
                        stringResource(R.string.scan_question),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                },
                confirmButton = {
                    Column {
                        Button(
                            onClick = {
                                showOptions = false
                                launchGallery()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.upload_from_gallery))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            onClick = {
                                showOptions = false
                                handleCameraClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.takeiimage))
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showOptions = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                dismissButton = {},
                containerColor = MaterialTheme.colorScheme.surface,
                textContentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}


private fun createImageUri(context: Context): Uri? {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "IMG_$timestamp.jpg"
    )
    return try {
        FileProvider.getUriForFile(
            context,
            "com.bedjamahdi.scanpestai.fileprovider",
            imageFile
        )
    } catch (e: IllegalArgumentException) {
        //    Log.e("FileProvider", "Failed to get URI for file", e)
        null
    }
}