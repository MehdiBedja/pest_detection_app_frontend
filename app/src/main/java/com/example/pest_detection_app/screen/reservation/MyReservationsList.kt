/* package com.example.reservation_app_frontend.screen.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.example.reservation_app_frontend.roomDatabase.ReservationEntity
import com.example.reservation_app_frontend.screen.navigation.Destination
import com.example.reservation_app_frontend.viewModel.reservation.getMyReservationsViewModel

@Composable
fun ShowReservationList(reservationViewModel: getMyReservationsViewModel, navController: NavHostController) {
    AddProgress(reservationViewModel)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .background(Color(0xFFF5F5F5))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
                .background(Color(0xFF6200EE))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "My Reservations",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(reservationViewModel.reservationsentity) { reservation ->
                ReservationItem(reservation = reservation, navController, reservationViewModel)
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ReservationItem(reservation: ReservationEntity, navController: NavController, reservationViewModel: getMyReservationsViewModel) {
    val backgroundColor = when (reservation.payment_status?.lowercase()) {
        "Validated" -> Color(0xFFADE772)
        "pending" -> Color(0xFFFFEB3B)
        else -> Color.LightGray
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable {
                reservationViewModel.getReservationById(reservation.id)
                reservationViewModel.getReservationOffline(reservation.id)
                navController.navigate(Destination.oneReservation.createRoute1(reservation.id)) {
                }
            }
            .padding(16.dp)
    ) {
        Column {
            Text(
                text = "Reservation code: ${reservation.reservation_code}",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Location: ${reservation.parking_place ?: "N/A"}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Start: ${reservation.entry_datetime}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Payment: ${reservation.payment_status}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}




@Composable
fun getShadedColorForReservation(reservation: ReservationEntity): Color {
    val baseColor = when (reservation.payment_status?.lowercase()) {
        "paid" -> Color(0xFFADE772)
        "pending" -> Color(0xFF457BCC)
        "cancelled" -> Color(0xFFDA1061)
        else -> Color.Gray
    }
    val lighterColor = baseColor.copy(alpha = 0.9f)
    val darkerColor = baseColor.copy(alpha = 0.7f)
    return if (reservation.payment_status?.lowercase() == "pending") {
        lighterColor
    } else {
        darkerColor
    }
}

@Composable
fun AddProgress(reservationViewModel: getMyReservationsViewModel) {
    val isLoading = reservationViewModel.loading.value

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

 */