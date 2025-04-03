package com.example.pest_detection_app.screen

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.pest_detection_app.R
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
fun HomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE8F5E9)), // Light green background
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Hero Image
        Image(
            painter = painterResource(id = R.drawable.homescreen) ,
            contentDescription = "Farm Hero",
            modifier = Modifier.size(150.dp),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.height(16.dp))

        // App Title
        Text(
            "Protect Your Crops with AI!",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(20.dp))

        AuthButtons(navController)
        DetectionButtons(navController)
        FeatureCard()
    }
}

@Composable
fun AuthButtons(navController: NavController) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { navController.navigate("login") }, colors = ButtonDefaults.buttonColors(Color(0xFF43A047))) {
            Text("\uD83D\uDD0D Sign In")
        }
        Button(onClick = { navController.navigate("signup") }, colors = ButtonDefaults.buttonColors(Color(0xFF43A047))) {
            Text("➕ Sign Up")
        }
    }
}

@Composable
fun DetectionButtons(navController: NavController) {
    val context = LocalContext.current
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { navController.navigate("results/${Uri.encode(it.toString())}") }
    }
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            cameraImageUri.value?.let { navController.navigate("results/${Uri.encode(it.toString())}") }
        }
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Button(onClick = { galleryLauncher.launch("image/*") }, colors = ButtonDefaults.buttonColors(Color(0xFF43A047))) {
            Text("\uD83D\uDCC2 Upload Image")
        }
        Button(onClick = {
            val uri = createImageUri(context)
            if (uri != null) {
                cameraImageUri.value = uri
                cameraLauncher.launch(uri)
            } else {
                Toast.makeText(context, "Failed to create image file", Toast.LENGTH_SHORT).show()
            }
        }, colors = ButtonDefaults.buttonColors(Color(0xFF43A047))) {
            Text("\uD83D\uDCF8 Take Photo")
        }
    }
}

@Composable
fun FeatureCard() {
    Card(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(0.9f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("App Features", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("✓ AI-powered pest detection")
            Text("✓ Instant recommendations")
        }
    }
}