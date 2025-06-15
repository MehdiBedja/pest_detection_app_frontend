package com.example.pest_detection_app.screen.user

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.with
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.ViewModels.user.UserViewModelRoom
import com.example.pest_detection_app.screen.navigation.Screen
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
            .background(MaterialTheme.colorScheme.background)
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
                        tint = MaterialTheme.colorScheme.onBackground
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
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    loading -> CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    error != null -> Text(
                        "Error: $error",
                        color = MaterialTheme.colorScheme.error,
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
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "${user.last_name}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = "@${user.username}",
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Profile
            OutlinedButton(
                onClick = { /* Navigate to edit profile */ },
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = stringResource(R.string.edit_profile) , color = MaterialTheme.colorScheme.onSurface)
            }

            Spacer(modifier = Modifier.height(30.dp))
        } else {
            // ✅ Not logged in content

            Text(
                text = "",
                style = MaterialTheme.typography.headlineSmall.copy(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            var showLoginDialog by remember { mutableStateOf(false) }

            Button(
                onClick = { showLoginDialog = true },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = stringResource(R.string.Sign_in_Sign_Up),
                    color = MaterialTheme.colorScheme.onPrimary
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
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            ProfileOption(Icons.Default.Settings, label = stringResource(R.string.settings)) {
                showSettingsSheet = true
            }

            if (user != null) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
                ProfileOption(Icons.Default.Lock, label = stringResource(R.string.change_pass)) {
                    // Navigate to change password
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 🔥 NEW: Telegram Community Section
        TelegramCommunitySection(
            onJoinTelegram = {
                // Handle Telegram group join
                val telegramUrl = "https://t.me/+qRyX7uHuv5o5MDg0" // Replace with your actual Telegram group link
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(telegramUrl))
                context.startActivity(intent)
            }
        )

        // Settings Bottom Sheet (existing code)
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surface
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

        // Settings Bottom Sheet
        if (showSettingsSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsSheet = false },
                sheetState = sheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                containerColor = MaterialTheme.colorScheme.surface
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
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = label,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )

        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurface
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
    val gradientColors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
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
                    HorizontalDivider()
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
                    HorizontalDivider()
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
            .background(MaterialTheme.colorScheme.surface, shape = RoundedCornerShape(24.dp))
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.account_settings),
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
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
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = "Go",
            tint = MaterialTheme.colorScheme.onSurface
        )
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
            style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = Color.Transparent,
                uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
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



@Composable
fun TelegramCommunitySection(onJoinTelegram: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onJoinTelegram() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onPrimary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Telegram Icon (using a generic icon since Telegram icon might not be available)
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF2AABEE), // Telegram blue
                                Color(0xFF229ED9)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector =  Icons.Default.Send, // Add telegram icon to drawable
                    contentDescription = "Telegram",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.join_community),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = stringResource(R.string.connect_with_farmers),
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                )
            }

            // Arrow Icon
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = "Join",
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
