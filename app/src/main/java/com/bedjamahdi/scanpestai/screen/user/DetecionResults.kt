package com.bedjamahdi.scanpestai.screen.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavController
import com.bedjamahdi.scanpestai.R

@Composable
fun DetectionResultsScreen(navController: NavController) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)
        .background(Color(0xFFE8F5E9)) // Light green background
    ) {
        // Header with Back Navigation
        AppHeader(pageTitle = "Detection Results") {
            navController.popBackStack() // Go back to the previous screen
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Detected Image
        DetectedImage()

        Spacer(modifier = Modifier.height(16.dp))

        // Pest Information
        PestInfoCard(
            pestName = "Aphid",
            description = "Aphids attack crops like beans, potatoes, and tomatoes.",
            confidenceScore = "92%"
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Recommended Pesticide
        PesticideRecommendationCard(
            pesticideName = "Neem Oil",
            usageInstructions = "Apply using a fine mist spray in the early morning or late evening."
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* Save Results */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5722))) {
                Text("Save Results")
            }
            Button(onClick = { /* Share Results */ }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))) {
                Text("Share")
            }
        }
    }
}


/** ✅ Header Component */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(pageTitle: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = pageTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            Image(
                painter = painterResource(id = R.drawable.logo6), // Replace with your actual logo
                contentDescription = "App Logo",
                modifier = Modifier.size(40.dp).padding(end = 8.dp)
            )
        }
    )
}


/** ✅ Detected Image Component */
@Composable
fun DetectedImage() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(Color.LightGray, shape = RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Image Placeholder", color = Color.DarkGray)
    }
}

/** ✅ Pest Info Component */
@Composable
fun PestInfoCard(pestName: String, description: String, confidenceScore: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Detected Pest: $pestName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(description, fontSize = 14.sp)
            Text("Confidence Score: $confidenceScore", fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** ✅ Pesticide Recommendation Component */
@Composable
fun PesticideRecommendationCard(pesticideName: String, usageInstructions: String) {
    Card(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Recommended Pesticide: $pesticideName", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(usageInstructions, fontSize = 14.sp)
        }
    }
}

