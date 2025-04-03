/* package com.example.pest_detection_app.screen.navigation

import AddReservationScreen
import android.content.Context
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.pest_detection_app.endpoint.reservation.ReservationEndpoint
import com.example.pest_detection_app.network.Globals
import com.example.reservation_app_frontend.repository.parking.ParkingRepository
import com.example.reservation_app_frontend.repository.reservation.ReservationRepository

import com.example.pest_detection_app.screen.reservation.OneReservation
import com.example.pest_detection_app.screen.reservation.ShowReservationList
import com.example.pest_detection_app.screen.user.LogInScreen
import com.example.pest_detection_app.screen.user.LogoutButton
import com.example.pest_detection_app.screen.user.SignUpScreen
import com.example.pest_detection_app.screen.user.UserProfileScreen
import com.example.pest_detection_app.ViewModels.reservation.AddReservationViewModel
import com.example.pest_detection_app.ViewModels.reservation.getMyReservationsViewModel
import com.example.pest_detection_app.ViewModels.user.LoginViewModel
import getAllReservationModel

@ExperimentalFoundationApi
@Composable
fun MainScreen(
    navController: NavHostController
    , context: Context,
    startDestination: String,
    userViewModel: LoginViewModel ,
    //   appDatabase: AppDatabase
)
{






    val isConnectedState = remember { mutableStateOf(userViewModel.isConnected()) }
    val endpoint2 = ReservationEndpoint.createEndpoint()
    val reservationRepository by lazy { ReservationRepository(endpoint2
        //    , appDatabase
    ) }
    val addReservationViewModel = AddReservationViewModel.Factory(reservationRepository).create(AddReservationViewModel::class.java)
    val reservationviewModel = getMyReservationsViewModel.Factory(reservationRepository).create(getMyReservationsViewModel::class.java)

    reservationviewModel.fetchReservations()
    reservationviewModel.getReservationsOffline()


    val items = listOf(
        Screen.ShowParkingList,
        Screen.ShowReservationList,
        Screen.UserProfileScreen,
        Screen.AddReservationScreen
    )

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry = navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.value?.destination

              //  if (isConnectedState.value) {

                items.forEach { des ->
                    val selected =
                        currentDestination?.hierarchy?.any { it.route == des.route } == true
                    NavigationBarItem(
                        icon = {
                            when (des) {
                                Screen.ShowReservationList -> Icon(
                                    Icons.Filled.List,
                                    contentDescription = null
                                ) // Replace with appropriate icon
                                Screen.UserProfileScreen -> Icon(
                                    Icons.Filled.Person,
                                    contentDescription = null
                                ) // Replace with appropriate icon
                                Screen.AddReservationScreen -> Icon(
                                    Icons.Filled.Create,
                                    contentDescription = null
                                ) // Replace with appropriate icon
                                else -> {}
                            }
                        },
                        selected = selected,
                        onClick = {
                            if (des == Screen.ShowReservationList) {
                                reservationviewModel.fetchReservations()
                                reservationviewModel.getReservationsOffline()
                            }
                            if (des == Screen.UserProfileScreen) {
                                Globals.savedUsername?.let { userViewModel.getUser(it) }
                            }

                            navController.navigate(des.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }

                                launchSingleTop = true
                                restoreState = true
                            }


                        }
                    )
                }
            }
        //    }
        }
    ) { innerPadding ->
        NavHost(navController, startDestination = startDestination, Modifier.padding(innerPadding)) {


            composable(Screen.LogoutButton.route) {
                LogoutButton(navController = navController , userViewModel)

            }


            composable(Screen.SignUp.route) {
                SignUpScreen(navController = navController)
            }

            composable(Screen.LogIn.route) {
                LogInScreen(navController = navController, userViewModel)
            }





            composable(Screen.ShowReservationList.route) { ShowReservationList(reservationviewModel , navController ) }
            composable(Screen.oneReservation.route) {navBack ->
                val id = navBack?.arguments?.getString("reservationId")?.toInt()
                OneReservation(id
                    //  , appDatabase
                )
            }

            composable(Screen.UserProfileScreen.route) { UserProfileScreen(userViewModel,navController) }

            composable(Screen.AddReservationScreen.route) { AddReservationScreen(reservationViewModelAll ,addReservationViewModel, context,reservationviewModel , navController ) }

        }
    }
} */