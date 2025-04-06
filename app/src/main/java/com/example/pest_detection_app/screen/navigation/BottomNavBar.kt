package com.example.pest_detection_app.screen.navigation

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.R


@Composable
fun BottomNavBar(navController: NavController, isLoggedIn: Boolean) {
    val bottomNavItems = mutableListOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home),
        BottomNavItem("Forum", Screen.Forum.route, Icons.Filled.Send),
    )

    if (isLoggedIn) {
        bottomNavItems.add(BottomNavItem("History", Screen.History.route, R.drawable.history))
    }

    bottomNavItems.add(
        BottomNavItem("Settings", Screen.Settings.route, Icons.Filled.Settings)
    )

    val currentRoute = navController.currentDestination?.route

    // Wrap in a completely transparent Surface
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.Transparent // This makes the entire surface area transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            // Create the floating navigation bar
            NavigationBar(
                modifier = Modifier
                    .clip(RoundedCornerShape(32.dp))
                    .height(64.dp)
                    .fillMaxWidth(0.8f)
                    .graphicsLayer {
                        alpha = 1f  // Stronger opacity for a more solid gloss effect
                        shadowElevation = 20f // Increased shadow for more depth
                    }
                    .blur(30.dp)  // Stronger blur effect for more gloss
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = 0.5f), // More visible border for gloss effect
                        shape = RoundedCornerShape(32.dp)
                    ),
                containerColor = Color.Black.copy(alpha = 0.6f), // Less transparency for a glossy look
                tonalElevation = 8.dp // Adds subtle elevation for depth
                , // The blur radius

            ) {
                bottomNavItems.forEach { item ->
                    val isSelected = currentRoute == item.route

                    NavigationBarItem(
                        icon = {
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
                        },
                        label = null, // Remove labels for a cleaner look like in the image
                        selected = isSelected,
                        onClick = {
                            navController.navigate(item.route) {
                                launchSingleTop = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.Black,
                            unselectedIconColor = Color.White,
                            indicatorColor = if (isSelected) Color.White.copy(alpha = 0.5f) else Color.Transparent
                        )
                    )
                }
            }
        }
    }
}