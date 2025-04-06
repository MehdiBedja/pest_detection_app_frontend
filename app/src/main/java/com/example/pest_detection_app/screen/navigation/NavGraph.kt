package com.example.pest_detection_app.navigation

import android.app.Application
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.RoomDatabase.DatabaseManager
import com.example.pest_detection_app.ViewModels.DetectionSaveViewModelFactory
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import com.example.pest_detection_app.ViewModels.UserViewModelFactory
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import com.example.pest_detection_app.ViewModels.user.UserViewModelRoom
import com.example.pest_detection_app.endpoint.user.userEndpoint
import com.example.pest_detection_app.preferences.Preferences
import com.example.pest_detection_app.repository.user.AuthRepository
import com.example.pest_detection_app.repository.user.UserPreferences
import com.example.pest_detection_app.screen.*
import com.example.pest_detection_app.screen.navigation.BottomNavBar
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.screen.user.LogInScreen
import com.example.pest_detection_app.screen.user.LogoutScreen
import com.example.pest_detection_app.screen.ResultsScreen
import com.example.pest_detection_app.screen.user.SignUpScreen
import com.example.pest_detection_app.screen.user.UserProfileScreen


val endpoint = userEndpoint.createEndpoint()
val userRepo by lazy { AuthRepository(endpoint) }

val preferences by lazy { Preferences(context = MyApp.getContext()) }
val userPreferences by lazy { UserPreferences(preferences) }

val userView by lazy { LoginViewModel(userRepo, userPreferences) }
val application = MyApp.getContext()

@Composable
fun NavGraph(navController: NavHostController) {
    val isLoggedIn by userView.isLoggedIn.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Splash.route) { SplashScreen(navController) }
            composable(Screen.Home.route) {

                val context = LocalContext.current.applicationContext as Application

                val detectionSaveViewModel: DetectionSaveViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = DetectionSaveViewModelFactory(
                        context,
                        DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                        DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
                    )
                )


                val userViewModelRoom: UserViewModelRoom = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = UserViewModelFactory(
                        context,
                        DatabaseManager.getDatabase(MyApp.getContext()).userDao(),

                    )
                )

                val detectionViewModel: DetectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()



                HomeScreen(navController, userView , detectionSaveViewModel ,detectionViewModel , userViewModelRoom) }
            composable(Screen.Forum.route) { ForumScreen(navController) }
            composable(Screen.Settings.route) { SettingsScreen(navController) }
            composable(Screen.Login.route) { LogInScreen(navController, userView) }
            composable(Screen.SignUp.route) { SignUpScreen(navController) }
            composable(Screen.UserProfileScreen.route) { UserProfileScreen(userView, navController) }
            composable(Screen.PesticideInfo.route) { PesticideInfoScreen(navController) }
            composable(Screen.Logout.route) { LogoutScreen(navController, userView) }

            composable(
                route = "results/{imageUri}",
                arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
            ) { backStackEntry ->
                val imageUri = backStackEntry.arguments?.getString("imageUri")
                val context = LocalContext.current.applicationContext as Application

                val detectionSaveViewModel: DetectionSaveViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = DetectionSaveViewModelFactory(
                        context,
                        DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                        DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
                    )
                )

                val detectionViewModel: DetectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                ResultsScreen(
                    navController = navController,
                    imageUri = imageUri,
                    detectionViewModel,
                    detectionSaveViewModel,
                    context = MyApp.getContext(),
                    userview = userView
                )
            }

            composable(Screen.History.route) {
                val context = LocalContext.current.applicationContext as Application

                val detectionSaveViewModel: DetectionSaveViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = DetectionSaveViewModelFactory(
                        context,
                        DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                        DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
                    )
                )

                DetectionHistoryScreen(navController, userView, detectionSaveViewModel)
            }

            composable(
                route = "detail_screen/{detectionId}",
                arguments = listOf(navArgument("detectionId") { type = NavType.IntType })
            ) { backStackEntry ->
                val detectionId = backStackEntry.arguments?.getInt("detectionId") ?: 0
                val context = LocalContext.current.applicationContext as Application

                val detectionSaveViewModel: DetectionSaveViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                    factory = DetectionSaveViewModelFactory(
                        context,
                        DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                        DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
                    )
                )

                val detectionViewModel: DetectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

                DetailItemScreen(navController, detectionViewModel, detectionSaveViewModel, detectionId, userView)
            }
        }

        // Floating Bottom Navigation Bar
        val currentRoute = currentRoute(navController)
        if (currentRoute in listOf(
                Screen.Home.route,
                Screen.Forum.route,
                Screen.Settings.route,
                Screen.History.route,
                Screen.UserProfileScreen.route
            )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp) // Adjust padding if needed
            ) {
                BottomNavBar(navController, isLoggedIn)
            }
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}