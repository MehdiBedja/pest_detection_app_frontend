package com.example.pest_detection_app.screen

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.ViewModels.user.UserViewModelRoom
import com.example.pest_detection_app.data.user.DetectionWithBoundingBoxes
import com.example.pest_detection_app.preferences.Globals
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.ui.theme.AppTypography
import com.example.pest_detection_app.ui.theme.CustomTextStyles
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private fun createImageUri(context: Context): Uri? {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val imageFile = File(
        context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
        "IMG_$timestamp.jpg"
    )
    return try {
        FileProvider.getUriForFile(
            context,
            "com.example.pest_detection_app.fileprovider",
            imageFile
        )
    } catch (e: IllegalArgumentException) {
        //    Log.e("FileProvider", "Failed to get URI for file", e)
        null
    }
}

@Composable
fun SyncNowButton(detectionSaveViewModel: DetectionSaveViewModel , userViewModel: LoginViewModel) {

    val userid by userViewModel.userId.collectAsState()
    val scope = rememberCoroutineScope()
    var isSyncing by remember { mutableStateOf(false) }
    var syncSuccess by remember { mutableStateOf<Boolean?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }

    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = {
                    isSyncing = true
                    scope.launch {
                        try {
                            detectionSaveViewModel.syncLocalServerIdsWithCloud(userid!!)
                            Globals.savedToken?.let {
                                detectionSaveViewModel.softDeleteLocalDetections(
                                    it, userid!!)

                                detectionSaveViewModel.syncSoftDeletedDetections()
                                detectionSaveViewModel.syncNotes(it , userid!!)
                            }
                            syncSuccess = true
                        } catch (e: Exception) {
                            syncSuccess = false
                        } finally {
                            isSyncing = false
                        }
                    }
                },
                enabled = !isSyncing,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(
                    text = "Sync Now",
                    style = CustomTextStyles.buttonText
                )
            }

            if (isSyncing) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary
                )
            }

            // Show result
            LaunchedEffect(syncSuccess) {
                syncSuccess?.let {
                    val msg = if (it) "✅ Sync Successful!" else "❌ Sync Failed!"
                    snackbarHostState.showSnackbar(msg)
                    syncSuccess = null
                }
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}


@Composable
fun HomeScreen(navController: NavController, userViewModel: LoginViewModel ,
               detectionSaveViewModel: DetectionSaveViewModel , detectionViewModel: DetectionViewModel ,
               userViewModelRoom: UserViewModelRoom) {

    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()







    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        UserGreeting(navController, userViewModel, userViewModelRoom)

        if(!isLoggedIn) {
            AuthButtons(navController)
        }

        ScanButton(navController)

        if(isLoggedIn) {
            RecentDetectionsSection(
                navController, userViewModel,
                detectionViewModel, detectionSaveViewModel
            )
            StatSection(navController)
        }

    }
}

@Composable
fun UserGreeting(navController: NavController , userViewModel: LoginViewModel, userViewModelRoom: UserViewModelRoom) {

    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val userid by userViewModel.userId.collectAsState()
    val user by userViewModelRoom.user.observeAsState()

    // Trigger data fetching
    LaunchedEffect(Unit) {
        userid?.let { userViewModelRoom.fetchUserById(it) }
    }

    if (isLoggedIn && user != null) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp, horizontal = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.welcomefarmer) + " ${user?.last_name}",
                color = MaterialTheme.colorScheme.onBackground,
                style = CustomTextStyles.welcomeText,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            // Circle with initials
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
                    .clickable { navController.navigate(Screen.UserProfileScreen.route) }
            ) {
                Text(
                    text = "${user?.username?.firstOrNull()?.uppercase() ?: ""}${user?.last_name?.firstOrNull()?.uppercase() ?: ""}",
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    style = CustomTextStyles.buttonText
                )
            }
        }
    } else {
        Text(
            text = stringResource(R.string.welcomefarmer),
            color = MaterialTheme.colorScheme.onBackground,
            style = AppTypography.displayMedium,
            modifier = Modifier
                .padding(vertical = 24.dp, horizontal = 16.dp)
        )
    }
}



@Composable
fun RecentDetectionsSection(
    navController: NavController,
    userViewModel: LoginViewModel,
    detectionViewModel: DetectionViewModel,
    detectionSaveViewModel: DetectionSaveViewModel
) {
    val userid by userViewModel.userId.collectAsState()
    val detections by detectionSaveViewModel.detections1.collectAsState()

    val syncCompletedEvent by detectionSaveViewModel.syncCompletedEvent.collectAsState(initial = null)

    LaunchedEffect(userid) {
        userid?.let { detectionSaveViewModel.getRecentDetections(it) }
    }

    // 🔥 THIS IS THE KEY FIX - Now using the shared ViewModel instance
    LaunchedEffect(syncCompletedEvent) {
        syncCompletedEvent?.let { syncResult ->
            when (syncResult) {
                is DetectionSaveViewModel.SyncResult.Success -> {
                    userid?.let { detectionSaveViewModel.getRecentDetections(it) }
                }
                is DetectionSaveViewModel.SyncResult.Failure -> {
                    userid?.let { detectionSaveViewModel.getRecentDetections(it) }
                }
            }
            detectionSaveViewModel.clearSyncResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.recentdetections),
                color = MaterialTheme.colorScheme.onBackground,
                style = CustomTextStyles.sectionTitle,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (detections.size > 3) {
                TextButton(
                    onClick = { navController.navigate(Screen.History.route) },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.seemore),
                        style = AppTypography.labelLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (detections.isNullOrEmpty()) {
            // No detections case
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.nodetectionsmadeyet),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = AppTypography.bodyLarge
                )
            }
        } else {
            // Detection Cards - Limited to first 3 detections
            LazyRow(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(detections.take(3)) { detection ->
                    DetectionCard(navController, detection)
                }
            }
        }
    }
}

@Composable
fun DetectionCard(navController: NavController,  detection: DetectionWithBoundingBoxes) {
    val firstBox = detection.boundingBoxes.firstOrNull()
    val pestName = firstBox?.clsName ?: stringResource(R.string.unknownpest)
    val confidence = firstBox?.cnf?.times(100)?.toInt()?.toString() ?: "0"
    val formattedDate = formatDate(detection.detection.detectionDate)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .clickable { navController.navigate("detail_screen/${detection.detection.id}") },
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                Image(
                    painter = rememberAsyncImagePainter(detection.detection.imageUri),
                    contentDescription = pestName,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Text(
                    text = formattedDate,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    style = CustomTextStyles.dateText,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = getTranslatedPestName(pestName),
            color = MaterialTheme.colorScheme.onBackground,
            style = CustomTextStyles.pestName
        )

        Text(
            text = stringResource(R.string.confidence) +" $confidence%",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            style = CustomTextStyles.confidence
        )
    }
}


@OptIn(ExperimentalAnimationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanButton(navController: NavController) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }

    // Add states to track pending actions
    var pendingCameraLaunch by remember { mutableStateOf(false) }
    var pendingGalleryLaunch by remember { mutableStateOf(false) }

    // Permission states using Accompanist
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val storagePermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    // Activity result launchers
    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            try {
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                //       Log.d("Gallery", "✅ Persisted URI permission for: $it")
            } catch (e: SecurityException) {
                //       Log.e("Gallery", "❌ Failed to persist permission", e)
            }
            navController.navigate("results/${Uri.encode(it.toString())}")
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let {
                navController.navigate("results/${Uri.encode(it.toString())}")
            }
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
            Toast.makeText(
                context,
                context.getString(R.string.failed_tocreateimagefile),
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    // Auto-launch camera when permission is granted and pending
    LaunchedEffect(cameraPermissionState.status.isGranted, pendingCameraLaunch) {
        if (cameraPermissionState.status.isGranted && pendingCameraLaunch) {
            pendingCameraLaunch = false
            launchCamera()
        }
    }

    // Auto-launch gallery when permission is granted and pending
    LaunchedEffect(storagePermissionState.status.isGranted, pendingGalleryLaunch) {
        if (storagePermissionState.status.isGranted && pendingGalleryLaunch) {
            pendingGalleryLaunch = false
            launchGallery()
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

    fun handleGalleryClick() {
        when {
            storagePermissionState.status.isGranted -> {
                launchGallery()
            }
            storagePermissionState.status.shouldShowRationale -> {
                // Set pending state and request permission
                pendingGalleryLaunch = true
                storagePermissionState.launchPermissionRequest()
            }
            else -> {
                // Set pending state and request permission
                pendingGalleryLaunch = true
                storagePermissionState.launchPermissionRequest()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Button(
            onClick = { showOptions = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(28.dp),
            contentPadding = PaddingValues(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.scanforpests_image),
                    contentDescription = "Pest Detection Illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                )

                AnimatedContent(
                    targetState = stringResource(R.string.scanforpest),
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                    },
                    label = "ScanText",
                    modifier = Modifier.align(Alignment.Center)
                ) { targetText ->
                    Text(
                        text = targetText,
                        color = MaterialTheme.colorScheme.secondary,
                        style = CustomTextStyles.scanButtonText
                    )
                }
            }
        }

        // Main options dialog
        if (showOptions) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = {
                    Text(
                        text = stringResource(R.string.chooseOption),
                        style = AppTypography.headlineSmall
                    )
                },
                text = {
                    Text(
                        text = stringResource(R.string.How_would_you_like_to_scan_for_pests),
                        style = AppTypography.bodyMedium
                    )
                },
                confirmButton = {
                    Column {
                        Button(
                            onClick = {
                                showOptions = false
                                handleGalleryClick()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = stringResource(R.string.upload_from_gallery),
                                style = CustomTextStyles.buttonText
                            )
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
                            Text(
                                text = stringResource(R.string.takeiimage),
                                style = CustomTextStyles.buttonText
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showOptions = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
                                color = MaterialTheme.colorScheme.primary,
                                style = AppTypography.labelLarge
                            )
                        }
                    }
                },
                dismissButton = {}
            )
        }
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun StatSection(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.stats),
                color = MaterialTheme.colorScheme.onBackground,
                style = CustomTextStyles.sectionTitle,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Button(
                onClick = { navController.navigate(Screen.Stat.route) },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(28.dp),
                contentPadding = PaddingValues(0.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.statsimage2),
                        contentDescription = "Pest Stats Illustration",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(28.dp))
                    )

                    AnimatedContent(
                        targetState = stringResource(R.string.stats_dashboard),
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                        },
                        label = "stats",
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) { targetText ->
                        Text(
                            text = targetText,
                            color = MaterialTheme.colorScheme.secondary,
                            style = CustomTextStyles.scanButtonText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthButtons(navController: NavController) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedButton(
            onClick = { navController.navigate("login") },
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.height(44.dp)
        ) {
            Text(
                text = stringResource(R.string.sign_in),
                style = CustomTextStyles.buttonText
            )
        }

        OutlinedButton(
            onClick = { navController.navigate("signup") },
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.height(44.dp)
        ) {
            Text(
                text = stringResource(R.string.sign_up),
                style = CustomTextStyles.buttonText
            )
        }
    }
}
