package com.example.pest_detection_app.screen.navigation

import com.example.pest_detection_app.R

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object Forum : Screen("detection_screen")
    object Results : Screen("results/{imageUri}")
    object History : Screen("history_screen")
    object PesticideInfo : Screen("pesticide_info_screen")
    object Settings : Screen("settings_screen")


    object UserProfileScreen:Screen ("My Profile")

    object Logout:Screen ("Logout")


    object Login:Screen("Login")
    object SignUp:Screen("SignUp")



    object Profile:Screen("profile")
    object Details:Screen("details/{userId}") {
        fun createRoute(userId:Int) = "details/$userId"
    }
    object DetailItemScreen : Screen("detailedItem_screen")

    object Stat : Screen("Stat")

    object ChangePassword : Screen("Change_Password")


    object PestList : Screen("pest_list")
    object PestDetail : Screen("pest_detail/{pestName}") {
        fun createRoute(pestName: String) = "pest_detail/$pestName"}

}
