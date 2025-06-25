package com.example.pest_detection_app.navigation

import OnboardingFirstScreen
import OnboardingSecondScreen
import OnboardingThirdScreen
import android.app.Activity
import android.app.Application
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.pest_detection_app.components.GlobalSyncBanner
import com.example.pest_detection_app.endpoint.user.userEndpoint
import com.example.pest_detection_app.preferences.Globals
import com.example.pest_detection_app.preferences.Preferences
import com.example.pest_detection_app.repository.user.AuthRepository
import com.example.pest_detection_app.repository.user.UserPreferences
import com.example.pest_detection_app.screen.*
import com.example.pest_detection_app.screen.navigation.BottomNavBar
import com.example.pest_detection_app.screen.navigation.Screen
import com.example.pest_detection_app.screen.user.LogInScreen
import com.example.pest_detection_app.screen.user.LogoutScreen
import com.example.pest_detection_app.screen.ResultsScreen
import com.example.pest_detection_app.screen.pest.PestDetailScreen
import com.example.pest_detection_app.screen.pest.PestListScreen
import com.example.pest_detection_app.screen.user.ChangePasswordScreen
import com.example.pest_detection_app.screen.user.SignUpScreen
import com.example.pest_detection_app.screen.user.UserProfileScreen
import com.google.accompanist.permissions.rememberPermissionState
import android.Manifest
import android.os.Build
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted

val endpoint = userEndpoint.createEndpoint()
val userRepo by lazy { AuthRepository(endpoint) }

val preferences by lazy { Preferences(context = MyApp.getContext()) }
val userPreferences by lazy { UserPreferences(preferences) }

val userView by lazy { LoginViewModel(userRepo, userPreferences) }
val application = MyApp.getContext()

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NavGraph(navController: NavHostController) {
    val isLoggedIn by userView.isLoggedIn.collectAsState()
    val context = LocalContext.current.applicationContext as Application
    val preferences = remember { Preferences(context) }

    // Permission state for storage access
    val storagePermissionState = rememberPermissionState(
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    )

    // Track if permission screen has been shown to prevent endless loop
    var hasShownPermissionScreen by remember { mutableStateOf(false) }

    // Track if user has explicitly handled the permission (granted, denied, or chose don't show again)
    var permissionHandled by remember { mutableStateOf(false) }

    // Reset permission tracking when login state changes
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            hasShownPermissionScreen = false
            permissionHandled = false
        }
    }

    // Check if we should show the permission screen
    val shouldShowPermissionScreen = remember {
        derivedStateOf {
            isLoggedIn &&
                    !storagePermissionState.status.isGranted &&
                    !preferences.getBoolean("sync_permission_dont_show_again", false) &&
                    !hasShownPermissionScreen &&
                    !permissionHandled
        }
    }

    val detectionSaveViewModel: DetectionSaveViewModel = viewModel(
        factory = DetectionSaveViewModelFactory(
            context,
            DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
            DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
        )
    )

    val currentLanguage = LocalContext.current.resources.configuration.locales[0].language
    var showSyncBanner by remember { mutableStateOf(true) }

    // Helper function to get localized messages
    fun getLocalizedMessage(key: String, language: String): String {
        return when (language) {
            "fr" -> when (key) {
                "sync_success" -> "Synchronisation terminée avec succès"
                "sync_failed" -> "Échec de la synchronisation"
                "failed_sync_cloud" -> "Échec de la synchronisation avec le cloud"
                "failed_soft_deletes" -> "Échec de la gestion des suppressions"
                "failed_sync_deleted" -> "Échec de la synchronisation des éléments supprimés"
                "failed_sync_notes" -> "Échec de la synchronisation des notes"
                "unknown_error" -> "Erreur inconnue"
                else -> "Erreur inconnue"
            }
            "ar" -> when (key) {
                "sync_success" -> "تمت المزامنة بنجاح"
                "sync_failed" -> "فشلت المزامنة"
                "failed_sync_cloud" -> "فشل في المزامنة مع السحابة"
                "failed_soft_deletes" -> "فشل في معالجة الحذف"
                "failed_sync_deleted" -> "فشل في مزامنة العناصر المحذوفة"
                "failed_sync_notes" -> "فشل في مزامنة الملاحظات"
                "unknown_error" -> "خطأ غير معروف"
                else -> "خطأ غير معروف"
            }
            else -> when (key) {
                "sync_success" -> "Sync completed successfully"
                "sync_failed" -> "Sync failed"
                "failed_sync_cloud" -> "Failed to sync with cloud"
                "failed_soft_deletes" -> "Failed to handle soft deletes"
                "failed_sync_deleted" -> "Failed to sync deleted items"
                "failed_sync_notes" -> "Failed to sync notes"
                "unknown_error" -> "Unknown error occurred"
                else -> "Unknown error occurred"
            }
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(Unit) {
        detectionSaveViewModel.syncCompletedEvent.collect { result ->
            when (result) {
                is DetectionSaveViewModel.SyncResult.Success -> {
                    snackbarHostState.showSnackbar(
                        getLocalizedMessage("sync_success", currentLanguage)
                    )
                }
                is DetectionSaveViewModel.SyncResult.Failure -> {
                    val errorMessage = "${
                        getLocalizedMessage(
                            "sync_failed",
                            currentLanguage
                        )
                    }: ${result.errorMessage}"
                    snackbarHostState.showSnackbar(errorMessage)
                }
            }
        }
    }

    // Trigger automatic sync when user is logged in AND permission is granted
    LaunchedEffect(isLoggedIn, storagePermissionState.status.isGranted) {
        if (isLoggedIn && storagePermissionState.status.isGranted) {
            val userId = userView.userId.value
            val token = Globals.savedToken
            if (userId != null && token != null) {
                detectionSaveViewModel.syncAll(userId, token)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column {
            // Global Sync Banner - only show if logged in AND has storage permission
            if (isLoggedIn && storagePermissionState.status.isGranted) {
                GlobalSyncBanner(
                    detectionSaveViewModel = detectionSaveViewModel,
                    onDismiss = { showSyncBanner = false }
                )
            }

            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.weight(1f)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreen(navController)
                }

                composable(Screen.FileAccessPermission.route) {
                    FileAccessPermissionScreen(
                        navController = navController,
                        storagePermissionState = storagePermissionState,
                        onPermissionHandled = {
                            // Mark as handled regardless of the result
                            permissionHandled = true
                            hasShownPermissionScreen = true
                            // Navigate back to home after handling permission
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.FileAccessPermission.route) { inclusive = true }
                            }
                        }
                    )
                }

                composable(Screen.Home.route) {
                    // Check if we need to show permission screen
                    if (shouldShowPermissionScreen.value) {
                        // Mark that we're about to show the permission screen
                        LaunchedEffect(Unit) {
                            hasShownPermissionScreen = true
                            navController.navigate(Screen.FileAccessPermission.route) {
                                // Don't remove Home from back stack, just navigate to permission screen
                            }
                        }
                        // Show a loading or empty state while navigating
                        Box(modifier = Modifier.fillMaxSize())
                    } else {
                        // Show actual Home screen content
                        val userViewModelRoom: UserViewModelRoom = viewModel(
                            factory = UserViewModelFactory(
                                context,
                                DatabaseManager.getDatabase(MyApp.getContext()).userDao(),
                            )
                        )

                        val detectionViewModel: DetectionViewModel = viewModel()
                        HomeScreen(navController, userView, detectionSaveViewModel, detectionViewModel, userViewModelRoom)
                    }
                }

                composable(Screen.Forum.route) { ForumScreen(navController) }
                composable(Screen.Settings.route) { SettingsScreen(navController) }
                composable(Screen.Login.route) { LogInScreen(navController, userView) }
                composable(Screen.SignUp.route) { SignUpScreen(navController) }
                composable(Screen.UserProfileScreen.route) {
                    val userViewModelRoom: UserViewModelRoom = viewModel(
                        factory = UserViewModelFactory(
                            context,
                            DatabaseManager.getDatabase(MyApp.getContext()).userDao(),
                        )
                    )
                    UserProfileScreen(userView, navController, userViewModelRoom)
                }
                composable(Screen.Logout.route) { LogoutScreen(navController, userView) }

                composable(
                    route = "results/{imageUri}",
                    arguments = listOf(navArgument("imageUri") { type = NavType.StringType })
                ) { backStackEntry ->
                    val imageUri = backStackEntry.arguments?.getString("imageUri")
                    val context = LocalContext.current.applicationContext as Application

                    val detectionSaveViewModel: DetectionSaveViewModel = viewModel(
                        factory = DetectionSaveViewModelFactory(
                            context,
                            DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                            DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
                        )
                    )

                    val detectionViewModel: DetectionViewModel = viewModel()

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

                    val detectionSaveViewModel: DetectionSaveViewModel = viewModel(
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

                    val detectionSaveViewModel: DetectionSaveViewModel = viewModel(
                        factory = DetectionSaveViewModelFactory(
                            context,
                            DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                            DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
                        )
                    )

                    val detectionViewModel: DetectionViewModel = viewModel()

                    DetailItemScreen(navController, detectionViewModel, detectionSaveViewModel, detectionId, userView)
                }

                composable(Screen.Stat.route) {
                    val context = LocalContext.current.applicationContext as Application

                    val detectionSaveViewModel: DetectionSaveViewModel = viewModel(
                        factory = DetectionSaveViewModelFactory(
                            context,
                            DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                            DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
                        )
                    )

                    StatsDashboardScreen(
                        navController = navController,
                        userViewModel = userView,
                        detectionSaveViewModel = detectionSaveViewModel
                    )
                }

                composable(Screen.ChangePassword.route) {
                    ChangePasswordScreen(
                        navController = navController,
                        viewModel = userView
                    )
                }

                composable(Screen.PestList.route) {
                    PestListScreen(
                        navController = navController
                    )
                }

                composable(
                    route = Screen.PestDetail.route,
                    arguments = listOf(
                        navArgument("pestName") {
                            type = NavType.StringType
                        }
                    )
                ) { backStackEntry ->
                    val pestName = backStackEntry.arguments?.getString("pestName") ?: ""
                    PestDetailScreen(navController, pestName)
                }

                composable(Screen.OnboardingFirst.route) {
                    OnboardingFirstScreen(navController)
                }

                composable(Screen.OnboardingSecond.route) {
                    OnboardingSecondScreen(navController)
                }

                composable(Screen.OnboardingThird.route) {
                    OnboardingThirdScreen(navController)
                }
            }
        }

        // Floating Bottom Navigation Bar
        val currentRoute = currentRoute(navController)
        if (currentRoute in listOf(
                Screen.Home.route,
                Screen.Stat.route,
                Screen.Settings.route,
                Screen.History.route,
                Screen.UserProfileScreen.route,
                Screen.SignUp.route,
                Screen.Login.route
            )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
            ) {
                BottomNavBar(navController, isLoggedIn)
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}