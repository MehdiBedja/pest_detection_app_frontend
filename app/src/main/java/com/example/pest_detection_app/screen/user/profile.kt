package com.example.pest_detection_app.screen.user

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.annotation.StringRes
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.ViewModels.user.UserViewModelRoom
import com.example.pest_detection_app.data.user.User
import com.example.pest_detection_app.screen.LoginSignupDialog
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.ui.theme.DarkBackground
import java.util.Locale

// Language preference utilities
fun Context.updateLocale(languageCode: String): Context {
    val locale = Locale(languageCode)
    Locale.setDefault(locale)
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    return createConfigurationContext(config)
}

object LanguagePref {
    private const val PREF_NAME = "language_pref"
    private const val LANGUAGE_KEY = "language_key"

    fun saveLanguage(context: Context, lang: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(LANGUAGE_KEY, lang)
            .apply()
    }

    fun getLanguage(context: Context): String {
        val lang = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, "en") ?: "en"
        return when (lang) {
            "en", "ar", "fr" -> lang
            else -> "en" // fallback to English
        }
    }

}

@Composable
fun UserProfileScreen(viewModel: LoginViewModel, navController: NavController , userViewModelRoom : UserViewModelRoom) {
    val userState by viewModel.user
    val loading by viewModel.loading
    val error by viewModel.error
    val savedToken by viewModel.token.collectAsState()



    val userid by viewModel.userId.collectAsState()
    val user by userViewModelRoom.user.observeAsState()


    // Trigger data fetching
    LaunchedEffect(Unit) {
        userid?.let { userViewModelRoom.fetchUserById(it) }
    }





    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(savedToken) {
        viewModel.getUser()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
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

                if (user != null) {
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

            }

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

                    else -> UserProfileContent(user, navController, viewModel)
                }
            }
        }
    }
}


@Composable
fun LoginSignupDialog1(navController: NavController, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text (stringResource(R.string.Hello_farmer) ) },
        confirmButton = {
            Button(onClick = {
                onDismiss()
                navController.navigate("login")
            }) {
                Text(stringResource(R.string.login))
            }
        },
        dismissButton = {
            Button(onClick = {
                onDismiss()
                navController.navigate("signup")
            }) {
                Text(stringResource(R.string.sign_up))
            }
        }
    )
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileContent(
    user: com.example.pest_detection_app.RoomDatabase.User?,
    navController: NavController,
    viewModel: LoginViewModel
) {
    val context = LocalContext.current

    var currentLanguage by remember {
        mutableStateOf(
            when (LanguagePref.getLanguage(context)) {
                "en" -> "English"
                "ar" -> "Arabic"
                "fr" -> "Français"
                else -> "English"
            }
        )
    }

    var isDarkMode by rememberSaveable { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    var showSettingsSheet by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(80.dp)
    ) {
        if (user != null) {
            // ✅ Logged-in user content

            // Profile Image
            Image(
                painter = rememberAsyncImagePainter(R.drawable.profilepicture),
                contentDescription = "Profile Image",
                modifier = Modifier
                    .size(130.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color(0xFFD4D0B4))
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

            // Edit Profile
            OutlinedButton(
                onClick = { /* Navigate to edit profile */ },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF2B3A2F)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF2B3A2F))
            ) {
                Text(text = stringResource(R.string.edit_profile))
            }

            Spacer(modifier = Modifier.height(30.dp))
        } else {
            // ✅ Not logged in content

            Text(
                text = "",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = Color(0xFF2B3A2F),
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            var showLoginDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showLoginDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2B3A2F))
            ) {
                Text(
                    text = stringResource(R.string.Sign_in_Sign_Up),
                    color = Color.White
                )
            }

            if (showLoginDialog) {
                LoginSignupDialog1(
                    navController = navController,
                    onDismiss = { showLoginDialog = false }
                )
            }


            Spacer(modifier = Modifier.height(30.dp))
        }

        // Settings Section (available for everyone)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            ProfileOption(Icons.Default.Settings, label = stringResource(R.string.settings)) {
                showSettingsSheet = true
            }

            if (user != null) {
                Divider(color = Color(0xFFE0DECC))
                ProfileOption(Icons.Default.Lock, label = stringResource(R.string.change_pass)) {
                    // Navigate to change password
                }
            }
        }

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = Color.White
            ) {
                EmbeddedSettings(
                    isDarkMode = isDarkMode,
                    onToggleDarkMode = { isDarkMode = !isDarkMode },
                    language = currentLanguage,
                    onLanguageChange = { newLanguage ->
                        currentLanguage = newLanguage
                        val langCode = when (newLanguage) {
                            "English" -> "en"
                            "Arabic" -> "ar"
                            "Français" -> "fr"
                            else -> "en"
                        }
                        LanguagePref.saveLanguage(context, langCode)
                        (context as? Activity)?.recreate()
                    }
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
    var showLanguageDialog by remember { mutableStateOf(false) }

    // Language selection dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(text = stringResource(R.string.choose_language)) },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.english),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("English")
                                showLanguageDialog = false
                            }
                            .padding(12.dp)
                    )
                    Divider()
                    Text(
                        text = stringResource(R.string.arabic),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("Arabic")
                                showLanguageDialog = false
                            }
                            .padding(12.dp)
                    )
                    Divider()
                    Text(
                        text = stringResource(R.string.french),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onLanguageChange("Français")
                                showLanguageDialog = false
                            }
                            .padding(12.dp)
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.account_settings),
            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
        )

        Spacer(modifier = Modifier.height(10.dp))

        SettingRow(R.string.language," $language", onClick = {
            showLanguageDialog = true
        })

        SettingRow(R.string.about_the_app,"", onClick = { /* Show dialog or bottom sheet */ })

        SettingRow(R.string.rate_us, "", onClick = { /* Open play store link */ })

        SettingToggleRow(
            label = "Dark Mode",
            checked = isDarkMode,
            onCheckedChange = { onToggleDarkMode() },
            gradient = Brush.horizontalGradient(gradientColors)
        )

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
fun SettingRow(@StringRes text :Int, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text =stringResource(id = text)+   label,
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