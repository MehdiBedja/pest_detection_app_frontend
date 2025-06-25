package com.example.pest_detection_app.screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.R
import com.example.pest_detection_app.screen.user.DarkModePref
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val context = LocalContext.current
    val isDarkMode = DarkModePref.getDarkMode(context)

    val onboardingManager = OnboardingManager(context)

    // Simplified animation states
    var contentVisible by remember { mutableStateOf(false) }

    // Minimal fade-in animation
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(600)
    )

    // Navigation effect
    LaunchedEffect(Unit) {
        contentVisible = true
        delay(1000) // Reduced delay

        if (onboardingManager.hasSeenOnboarding()) {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.OnboardingFirst.route) {
                popUpTo(Screen.Splash.route) { inclusive = true }
            }
        }
    }

    // Theme colors
    val backgroundColor = MaterialTheme.colorScheme.background
    val primaryColor = MaterialTheme.colorScheme.primary
    val onBackgroundColor = MaterialTheme.colorScheme.onBackground

    // Simplified gradient background
    val gradientColors = if (isDarkMode) {
        listOf(
            Color(0xFF1A1A1A),
            Color(0xFF2A2A2A),
            Color(0xFF1A1A1A)
        )
    } else {
        listOf(
            Color(0xFFF6F4E8),
            Color(0xFFE8E4D8),
            Color(0xFFF6F4E8)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors,
                    startY = 0f,
                    endY = Float.POSITIVE_INFINITY
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.alpha(contentAlpha)
        ) {
            // Bigger logo with minimal styling
            Box(
                modifier = Modifier.size(200.dp), // Increased from 120.dp to 200.dp
                contentAlignment = Alignment.Center
            ) {
                // Subtle logo glow effect
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .clip(CircleShape)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.2f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Logo image - much bigger
                Image(
                    painter = painterResource(
                        id = if (isDarkMode) R.drawable.darkmodeapplogo else R.drawable.logo6
                    ),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(180.dp) // Increased from 100.dp to 180.dp
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            // App title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.app_name), // "ScanPest AI"
                    fontSize = 32.sp, // Slightly bigger text too
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(R.string.app_tagline), // "Intelligent Pest Detection"
                    fontSize = 18.sp, // Slightly bigger tagline
                    fontWeight = FontWeight.Medium,
                    color = onBackgroundColor.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(60.dp))

        }

        // Version text at bottom
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(
                text = stringResource(R.string.version_text), // "Version 1.0"
                fontSize = 12.sp,
                color = onBackgroundColor.copy(alpha = 0.5f),
                modifier = Modifier.alpha(contentAlpha)
            )
        }
    }
}