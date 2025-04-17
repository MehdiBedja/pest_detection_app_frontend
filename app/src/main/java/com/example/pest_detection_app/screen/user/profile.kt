package com.example.pest_detection_app.screen.user

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.data.user.User
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.ui.theme.DarkBackground

@Composable
fun UserProfileScreen(viewModel: LoginViewModel, navController: NavController) {
    val userState by viewModel.user
    val loading by viewModel.loading
    val error by viewModel.error
    val savedToken by viewModel.token.collectAsState()

    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    var language by rememberSaveable { mutableStateOf("English") }


    LaunchedEffect(savedToken) {
        viewModel.getUser()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground) // Beige background
    ) {
        Column {
            // Top bar with Back and Logout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.navigate(Screen.Home.route) }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color(0xFF2B3A2F)
                    )
                }

                IconButton(
                    onClick = {
                        viewModel.logout()
                        navController.navigate("home_screen") {
                            popUpTo("home_screen") { inclusive = true }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.logout),
                        contentDescription = "Logout",
                        tint = Color(0xFF2B3A2F)
                    )
                }
            }

            // Spacer to separate top bar from content
            Spacer(modifier = Modifier.height(8.dp))

            // Main Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> CircularProgressIndicator(
                        color = Color.DarkGray,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    error != null -> Text(
                        "Error: $error",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    userState != null -> UserProfileContent(userState!!, navController, viewModel)
                }
            }
        }

    }



    }


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileContent(user: User, navController: NavController, viewModel: LoginViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(80.dp)
    ) {


        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        val coroutineScope = rememberCoroutineScope()
        var showSettingsSheet by remember { mutableStateOf(false) }

        // Profile Image in rounded square
        Image(
            painter = rememberAsyncImagePainter(R.drawable.profilepicture),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(130.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFD4D0B4)) // placeholder background if needed
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${user.last_name}",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color(0xFF2B3A2F),
                fontWeight = FontWeight.Bold
            )
        )

        Text(
            text = "@${user.username}",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF61705C))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Edit Profile Button
        OutlinedButton(
            onClick = { /* Edit profile nav */ },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color(0xFF2B3A2F)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2B3A2F))
        ) {
            Text("Edit Profile")
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Settings Section
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            ProfileOption(Icons.Default.Settings, label = "Settings") {
                showSettingsSheet = true
            }
            Divider(color = Color(0xFFE0DECC))
            ProfileOption(Icons.Default.Lock, label = "Change password") { /* nav */ }

        }

        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = Color.White
            ) {
                EmbeddedSettings(
                    isDarkMode = false,
                    onToggleDarkMode = { /* Add logic */ },
                    language = "English",
                    onLanguageChange = { /* Add logic */ }
                )
            }
        }
    }
}

@Composable
fun ProfileOption(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = Color(0xFF2B3A2F),
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF2B3A2F),
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Go",
            tint = Color(0xFF2B3A2F)
        )
    }
}



@Composable
fun EmbeddedSettings(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    language: String,
    onLanguageChange: (String) -> Unit,
) {
    val gradientColors = listOf(Color(0xFFB66DD1), Color(0xFFE77675))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingRow("Language: $language", onClick = {
            val newLang = if (language == "English") "Arabic" else "English"
            onLanguageChange(newLang)
        })

        SettingRow("About the App", onClick = { /* Show dialog or bottom sheet */ })

        SettingRow("Rate us", onClick = { /* Open play store link */ })

        SettingToggleRow(
            label = "Dark Mode",
            checked = isDarkMode,
            onCheckedChange = { onToggleDarkMode() },
            gradient = Brush.horizontalGradient(gradientColors)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Support Email
        Text(
            text = "Support: support@farmshield.com",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}


@Composable
fun SettingRow(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Go")
    }
}

@Composable
fun SettingToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    gradient: Brush
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Transparent,
                uncheckedThumbColor = Color.LightGray,
                uncheckedTrackColor = Color.Gray
            ),
            thumbContent = {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .background(brush = gradient, shape = CircleShape)
                )
            }
        )
    }
}
