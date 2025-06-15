package com.example.pest_detection_app.screen

import android.content.Context
import android.content.Intent
import android.net.Uri
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
        Log.e("FileProvider", "Failed to get URI for file", e)
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
                Text("Sync Now")
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
                .fillMaxWidth() // Make Row take all width
                .padding(vertical = 24.dp, horizontal = 16.dp) // Add horizontal padding
        ) {
            Text(
                text = stringResource(R.string.welcomefarmer) + " ${user?.last_name}",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp, // reduce size to fit better
                fontFamily = FontFamily.Serif,
                modifier = Modifier
                    .weight(1f) // Text takes all available space
                    .padding(end = 8.dp), // Space between text and icon
                maxLines = 2, // Avoid overflow
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
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        Text(
            text = stringResource(R.string.welcomefarmer),
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 32.sp, // reduce a bit
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
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

    LaunchedEffect(userid) {
        userid?.let { detectionSaveViewModel.getRecentDetections(it) }
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
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp ,

                modifier = Modifier.padding(bottom = 8.dp)

            )

            TextButton(
                onClick = { navController.navigate(Screen.History.route) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.seemore),
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyMedium
                )
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
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            // Detection Cards
            LazyRow(
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(detections) { detection ->
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
        horizontalAlignment = Alignment.CenterHorizontally // Center content below the card
    ) {
        Card(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp)
                .clickable { navController.navigate("detail_screen/${detection.detection.id}") }
            ,

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
                    contentScale = ContentScale.Crop // Ensure image fills the card
                )

                Text(
                    text = formattedDate,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp)) // Add some space between card and text

        Text(
            text = pestName,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold ,
            fontFamily = FontFamily.Serif,

            )

        Text(
            text = stringResource(R.string.confidence) +" $confidence%",
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScanButton(navController: NavController) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()  // ✅ Use OpenDocument for persistable access
    ) { uri: Uri? ->
        uri?.let {
            try {
                // ✅ Persist permission immediately
                context.contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                Log.d("Gallery", "✅ Persisted URI permission for: $it")
            } catch (e: SecurityException) {
                Log.e("Gallery", "❌ Failed to persist permission", e)
            }

            navController.navigate("results/${Uri.encode(it.toString())}")
        }
    }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraImageUri.value?.let {
                navController.navigate("results/${Uri.encode(it.toString())}")
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
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        if (showOptions) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = { Text(stringResource(R.string.chooseOption)) },
                text = { Text(stringResource(R.string.How_would_you_like_to_scan_for_pests)) },
                confirmButton = {
                    Column {
                        Button(
                            onClick = {
                                showOptions = false
                                // ✅ Launch OpenDocument with array
                                galleryLauncher.launch(arrayOf("image/*"))
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.stats),
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp ,

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
                        label = "ScanText",
                        modifier = Modifier.align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    ) { targetText ->
                        Text(
                            text = targetText,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
    }
}}
@Composable
fun AuthButtons(navController: NavController) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(
                onClick = { navController.navigate("login") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.sign_in))
            }
            Button(
                onClick = { navController.navigate("signup") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(stringResource(R.string.sign_up))
            }
        }
    }
}