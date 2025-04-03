package com.example.pest_detection_app.screen.navigation

import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.R


@Composable
fun BottomNavBar(navController: NavController, isLoggedIn: Boolean) {
    val bottomNavItems = mutableListOf(
        BottomNavItem("Home", Screen.Home.route, Icons.Filled.Home),
        BottomNavItem("Forum", Screen.Forum.route, Icons.Filled.Send),
        BottomNavItem("Settings", Screen.Settings.route, Icons.Filled.Settings),
    )

    if (isLoggedIn) {
        bottomNavItems.add(BottomNavItem("History", Screen.History.route, R.drawable.history))
        bottomNavItems.add(BottomNavItem("Profile", Screen.UserProfileScreen.route, Icons.Filled.Person))
    }

    NavigationBar(containerColor = Color(0xF05C6467), tonalElevation = 8.dp) {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                icon = {
                    if (item.icon is Int) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.title,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else if (item.icon is ImageVector) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                label = { Text(item.title, color = Color.White) },
                selected = false,
                onClick = { navController.navigate(item.route) }
            )
        }
    }
}