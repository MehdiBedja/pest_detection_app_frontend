package com.bedjamahdi.scanpestai.screen.navigation

import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val title: String,
    val route: String,
    val icon: Any // Can be ImageVector or Painter
)