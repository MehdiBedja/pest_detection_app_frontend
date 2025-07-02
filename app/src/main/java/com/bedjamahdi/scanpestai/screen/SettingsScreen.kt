package com.bedjamahdi.scanpestai.screen

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.util.Locale


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
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(LANGUAGE_KEY, "en") ?: "en"
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current


    var showLanguageDialog by remember { mutableStateOf(false) }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text("Choose Language") },
            text = {
                Column {
                    Text("English", modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            LanguagePref.saveLanguage(context, "en")
                            showLanguageDialog = false
                            (context as? Activity)?.recreate()
                        }
                        .padding(8.dp)
                    )
                    Text("Arabic", modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            LanguagePref.saveLanguage(context, "ar")
                            showLanguageDialog = false
                            (context as? Activity)?.recreate()
                        }
                        .padding(8.dp)
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



    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFFE8F5E9)) // Light green background

        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {


                item { SectionTitle("Preferences") }

                item {
                    SettingsItem("🌐 Language", "English / Arabic") {
                        showLanguageDialog = true
                    }
                }
                item { SettingsItem("🎨 Theme", "Light / Dark Mode") { } }


                item { SettingsItem("🔔 Notifications", "Manage alerts & updates") { } }

                item { SettingsItem("❓ About", "App version & details") { } }

                item { SectionTitle("Account") }
                item {
                    SettingsItem("🚪 Log Out", "Sign out from your account") {
                        Toast.makeText(context, "Logged out!", Toast.LENGTH_SHORT).show()
                        navController.navigate("Login") { popUpTo("home_screen") { inclusive = true } }
                    }
                }
                item {
                    SettingsItem("💥 Delete Account", "Permanently delete your account") {
                        // Add confirmation dialog here
                        Toast.makeText(context, "Account deleted!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}


@Composable
fun SectionTitle(title: String) {
    Text(text = title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }, // ✅ Add this!
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEAC8))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = subtitle, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

