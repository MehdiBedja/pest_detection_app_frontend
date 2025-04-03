package com.example.pest_detection_app.screen

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current

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
                    SettingsItem("🚪 Profile Page", "Check and change your account personal's information") {
                        navController.navigate("profile")
                    }
                }

                item { SectionTitle("Preferences") }
                item { SettingsItem("🌐 Language", "English / Arabic") { } }
                item { SettingsItem("🎨 Theme", "Light / Dark Mode") { } }
                item { SettingsItem("🔔 Notifications", "Manage alerts & updates") { } }

                item { SectionTitle("Privacy & Data") }
                item { SettingsItem("🚫 Clear History", "Remove all search & detection history") { } }
                item { SettingsItem("📝 Terms & Privacy Policy", "Read our policies") { } }

                item { SectionTitle("Support & Feedback") }
                item { SettingsItem("💬 Contact Support", "Send feedback or report issues") { } }
                item { SettingsItem("⭐ Rate the App", "Give us a rating") { } }
                item { SettingsItem("🔄 Check for Updates", "Ensure you're on the latest version") { } }
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
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFECEAC8)) ,
        onClick = onClick
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