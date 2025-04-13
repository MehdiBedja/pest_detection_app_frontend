package com.example.pest_detection_app.screen

import android.content.Context
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
import com.example.pest_detection_app.navigation.userView
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.ui.theme.AccentGreen
import com.example.pest_detection_app.ui.theme.CardBackground
import com.example.pest_detection_app.ui.theme.DarkBackground
import com.example.pest_detection_app.ui.theme.GrayText
import com.example.pest_detection_app.ui.theme.LightText
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
                            syncSuccess = true
                        } catch (e: Exception) {
                            syncSuccess = false
                        } finally {
                            isSyncing = false
                        }
                    }
                },
                enabled = !isSyncing
            ) {
                Text("Sync Now")
            }

            if (isSyncing) {
                Spacer(modifier = Modifier.height(12.dp))
                CircularProgressIndicator()
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
            .background(DarkBackground)
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState()) // Make the column vertically scrollable
            .background(
            color = DarkBackground
            )
        ,


    )  {
        UserGreeting(navController , userViewModel , userViewModelRoom)

        SyncNowButton(detectionSaveViewModel , userViewModel)

        if(!isLoggedIn ) {
            AuthButtons(navController)
        }

            ScanButton(navController)

        if(isLoggedIn ) {
            RecentDetectionsSection(
                navController, userViewModel,
                detectionViewModel, detectionSaveViewModel
            )
        }
        ForumSection()
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
                text = "Welcome, Farmer ${user?.first_name} ${user?.last_name}",
                color = LightText,
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
                    .background(color = LightText.copy(alpha = 0.2f))
                    .clickable { navController.navigate(Screen.UserProfileScreen.route) }
            ) {
                Text(
                    text = "${user?.first_name?.firstOrNull()?.uppercase() ?: ""}${user?.last_name?.firstOrNull()?.uppercase() ?: ""}",
                    color = LightText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    } else {
        Text(
            text = "Welcome, Farmer",
            color = LightText,
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
                text = "Recent Detections",
                color = LightText,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp ,

                modifier = Modifier.padding(bottom = 8.dp)

            )

            TextButton(
                onClick = { navController.navigate(Screen.History.route) },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = GrayText
                )
            ) {
                Text(
                    text = "See More",
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
                    text = "No detections made for now.",
                    color = GrayText,
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
    val pestName = firstBox?.clsName ?: "Unknown Pest"
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
            colors = CardDefaults.cardColors(containerColor = CardBackground) ,

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
                    color = Color.White.copy(alpha = 0.7f),
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
            color = Color.White,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold ,
            fontFamily = FontFamily.Serif,

            )

        Text(
            text = "Confidence: $confidence%",
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 16.sp
        )
    }
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ScanButton(navController: NavController) {
    val context = LocalContext.current
    var showOptions by remember { mutableStateOf(false) }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { navController.navigate("results/${Uri.encode(it.toString())}") }
    }

    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri.value?.let { navController.navigate("results/${Uri.encode(it.toString())}") }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
    ) {
        Button(
            onClick = { showOptions = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent), // make button transparent
            shape = RoundedCornerShape(28.dp),
            contentPadding = PaddingValues(0.dp), // remove inner padding
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp) // adjust height as needed
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Background image
                Image(
                    painter = painterResource(id = R.drawable.scanforpests_image),
                    contentDescription = "Pest Detection Illustration",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(28.dp))
                )

                // Text on top
                AnimatedContent(
                    targetState = "Scan for Pests",
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) with fadeOut(animationSpec = tween(400))
                    },
                    label = "ScanText",
                    modifier = Modifier
                        .align(Alignment.Center)
                ) { targetText ->
                    Text(
                        text = targetText,
                        color = Color(0xFF1A1A1A), // dark text
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Options dialog
        if (showOptions) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = { Text("Choose Option") },
                text = { Text("How would you like to scan for pests?") },
                confirmButton = {
                    Column {
                        Button(
                            onClick = {
                                showOptions = false
                                galleryLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.buttonColors(AccentGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Upload from Gallery")
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
                                    Toast.makeText(context, "Failed to create image file", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(AccentGreen),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Take Photo")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = { showOptions = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Cancel")
                        }
                    }
                },
                dismissButton = {} // Don't render a dismiss button separately
            )
        }



    }
}


@Composable
fun ForumSection() {
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
                text = "Forum",
                color = LightText,
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Serif,
                letterSpacing = 0.5.sp ,

                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "See More",
                color = GrayText,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Farmer",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = "3 h ago",
                        color = GrayText,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Has anyone tried this pesticide?",
                    color = Color.Black,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
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
                    containerColor = CardBackground,
                    contentColor = Color.Black
                )
            ) {
                Text("Sign In")
            }
            Button(
                onClick = { navController.navigate("signup") },
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardBackground,
                    contentColor = Color.Black
                )
            ) {
                Text("Sign Up")
            }
        }
    }
}


