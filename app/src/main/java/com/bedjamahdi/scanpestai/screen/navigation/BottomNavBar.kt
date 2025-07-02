package com.bedjamahdi.scanpestai.screen.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.bedjamahdi.scanpestai.screen.navigation.Screen
import com.bedjamahdi.scanpestai.R

@Composable
fun BottomNavBar(navController: NavController, isLoggedIn: Boolean) {
    val bottomNavItems = mutableListOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home),
    )

    if (isLoggedIn) {
        bottomNavItems.add(BottomNavItem("Stat", Screen.Stat.route, R.drawable.stats))
        bottomNavItems.add(BottomNavItem("History", Screen.History.route, R.drawable.history))
    }

    bottomNavItems.add(
        BottomNavItem("Profile", Screen.UserProfileScreen.route, Icons.Default.Person)
    )

    val currentRoute = navController.currentDestination?.route

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // BACKGROUND LAYER: Blurred background for glassy effect
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .height(64.dp)
                    .fillMaxWidth(0.8f)
                    .blur(30.dp) // Blur only the background
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(32.dp)
                    )
                    .graphicsLayer {
                        shadowElevation = 20f // Shadow only on background
                    }
            )

            // FOREGROUND LAYER: Completely transparent Row with explicit transparent background
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .height(64.dp)
                    .fillMaxWidth(0.8f)
                    .background(Color.Transparent) // Explicitly set transparent background
                    .graphicsLayer {
                        shadowElevation = 0f // Remove shadow from foreground
                        alpha = 1f
                    },
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    IconButton(
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent
                            )
                    ) {
                        if (item.icon is Int) {
                            Icon(
                                painter = painterResource(id = item.icon),
                                contentDescription = item.title,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        } else if (item.icon is ImageVector) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.title,
                                tint = if (isSelected) Color.Black else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}