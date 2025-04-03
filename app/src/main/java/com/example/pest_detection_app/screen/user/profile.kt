package com.example.pest_detection_app.screen.user

import android.annotation.SuppressLint
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.pest_detection_app.R
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.data.user.User

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun UserProfileScreen(viewModel: LoginViewModel, navController: NavController) {
    val userState by viewModel.user
    val loading by viewModel.loading
    val error by viewModel.error
    val savedToken by viewModel.token.collectAsState()

    LaunchedEffect(savedToken) {
        viewModel.getUser()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile", fontSize = 20.sp, color = Color.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFEBEFEC) // Deep teal color
                )
            )
        },
        content = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8F5E9)) // Light green background

                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    loading -> CircularProgressIndicator(color = Color.White)
                    error != null -> Text("Error: $error", color = Color.Red)
                    userState != null -> UserProfileContent(userState!!, navController, viewModel)
                }
            }
        }
    )
}

@Composable
fun UserProfileContent(user: User, navController: NavController, viewModel: LoginViewModel) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        // Profile Image
        Image(
            painter = rememberAsyncImagePainter(R.drawable.user),
            contentDescription = "Profile Image",
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .border(4.dp, Color.White, CircleShape)
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Username
        Text(
            text = user.username,
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.White),
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Email
        Text(
            text = user.email,
            style = MaterialTheme.typography.bodyLarge.copy(color = Color(0xFFD1E8E2))
        )

        Spacer(modifier = Modifier.height(16.dp))

        // User Details in a Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                UserDetailItem(label = "First Name", value = user.first_name ?: "N/A")
                UserDetailItem(label = "Last Name", value = user.last_name ?: "N/A")
                UserDetailItem(label = "Phone Number", value = user.phone_number ?: "N/A")
                UserDetailItem(label = "Date of Birth", value = user.date_of_birth ?: "N/A")
                UserDetailItem(label = "Joined On", value = user.date_joined ?: "N/A")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Logout Button
        Button(
            onClick = {
                viewModel.logout()
                navController.navigate("home_screen") { popUpTo("home_screen") { inclusive = true } }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Logout", fontSize = 18.sp, color = Color.White)
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun UserDetailItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$label:",
            fontSize = 16.sp,
            color = Color(0xFF004D40),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            modifier = Modifier.weight(1f)
        )
    }
}
