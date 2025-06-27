package com.example.pest_detection_app.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pest_detection_app.R
import com.example.pest_detection_app.preferences.Preferences
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.ui.theme.AppTypography
import com.example.pest_detection_app.ui.theme.CustomTextStyles
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun FileAccessPermissionScreen(
    navController: NavController,
    storagePermissionState: PermissionState,
    onPermissionHandled: () -> Unit, // Callback to handle post-permission logic
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferences = remember { Preferences(context) }

    var showDenyDialog by remember { mutableStateOf(false) }
    var shouldRequestPermission by remember { mutableStateOf(false) }

    // Handle permission request result
    LaunchedEffect(shouldRequestPermission, storagePermissionState.status.isGranted) {
        if (shouldRequestPermission) {
            if (storagePermissionState.status.isGranted) {
                // Permission granted, proceed to home
                onPermissionHandled()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.FileAccessPermission.route) { inclusive = true }
                }
            } else {
                // Permission denied, still proceed to home but sync won't work
                onPermissionHandled()
                navController.navigate(Screen.Home.route) {
                    popUpTo(Screen.FileAccessPermission.route) { inclusive = true }
                }
            }
            shouldRequestPermission = false
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Image Background - Full width reaching top of screen
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                        .clip(RoundedCornerShape(bottomStart = 40.dp, bottomEnd = 40.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.syncscreenpic), // You'll need to add this image
                        contentDescription = null,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Spacer to create space for card overlap
                Spacer(modifier = Modifier.height(80.dp))
            }

            // Scrollable Card overlapping the image - Made shorter
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 300.dp)
                    .padding(bottom = 140.dp)
                    .heightIn(max = 350.dp), // Limit maximum height to make it shorter
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                // Make the card content scrollable with visible scrollbar indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(
                            rememberScrollState(),
                            reverseScrolling = false
                        )
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Title
                    Text(
                        text = stringResource(R.string.file_access_title),
                        style = CustomTextStyles.sectionHeader.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subtitle
                    Text(
                        text = stringResource(R.string.file_access_subtitle),
                        style = AppTypography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Benefits list
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.Start
                    ) {
                        FileAccessBenefit(
                            title = stringResource(R.string.benefit_sync_title),
                            description = stringResource(R.string.benefit_sync_description)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        FileAccessBenefit(
                            title = stringResource(R.string.benefit_storage_title),
                            description = stringResource(R.string.benefit_storage_description)
                        )




                    }
                }
            }

            // Action Buttons - Fixed at bottom (Now only 2 buttons!)
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Continue Button (Primary)
                Button(
                    onClick = {
                        // Mark as shown and launch permission request
                        preferences.putBoolean("sync_permission_prompt_shown", true)
                        storagePermissionState.launchPermissionRequest()
                        shouldRequestPermission = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.continue_button),
                        style = CustomTextStyles.buttonText
                    )
                }

                // Skip Button (shows dialog with options)
                OutlinedButton(
                    onClick = {
                        showDenyDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.secondary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.secondary
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.skip_button), // Add this string resource: "Skip"
                        style = CustomTextStyles.buttonText
                    )
                }
            }
        }
    }

    // Custom Styled Dialog for Skip Options
    if (showDenyDialog) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Dialog Title
                    Text(
                        text = stringResource(R.string.skip_options_title),
                        style = CustomTextStyles.sectionHeader.copy(
                            color = MaterialTheme.colorScheme.primary
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dialog Message
                    Text(
                        text = stringResource(R.string.skip_options_message),
                        style = AppTypography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    // Dialog Buttons - Properly styled and aligned
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Skip This Time Button
                        Button(
                            onClick = {
                                // Just mark as shown but don't set "don't show again"
                                preferences.putBoolean("sync_permission_prompt_shown", true)
                                showDenyDialog = false
                                onPermissionHandled()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.FileAccessPermission.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.skip_this_time),
                                style = CustomTextStyles.buttonText
                            )
                        }

                        // Don't Show Again Button
                        OutlinedButton(
                            onClick = {
                                // Set both flags - don't show again and mark as shown
                                preferences.putBoolean("sync_permission_dont_show_again", true)
                                preferences.putBoolean("sync_permission_prompt_shown", true)
                                showDenyDialog = false
                                onPermissionHandled()
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(Screen.FileAccessPermission.route) { inclusive = true }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.error
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.dont_show_again),
                                style = CustomTextStyles.buttonText
                            )
                        }

                        // Cancel Button
                        OutlinedButton(
                            onClick = { showDenyDialog = false },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.secondary
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.secondary
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.cancel),
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
private fun FileAccessBenefit(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = CustomTextStyles.sectionTitle.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = AppTypography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}