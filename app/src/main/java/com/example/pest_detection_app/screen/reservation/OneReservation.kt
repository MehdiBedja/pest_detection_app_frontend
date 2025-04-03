/* package com.example.reservation_app_frontend.screen.reservation
import android.graphics.Color as AndroidColor

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.reservation_app_frontend.endpoint.reservation.ReservationEndpoint
import com.example.reservation_app_frontend.repository.reservation.ReservationRepository
import com.example.reservation_app_frontend.screen.navigation.Destination
import com.example.reservation_app_frontend.viewModel.reservation.getMyReservationsViewModel
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.WriterException
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter


@Composable
fun OneReservation(reservationId: Int?) {
    val endpoint = ReservationEndpoint.createEndpoint()
    val reservationRepository by lazy { ReservationRepository(endpoint) }

    val reservationViewModel = getMyReservationsViewModel.Factory(reservationRepository).create(getMyReservationsViewModel::class.java)

    val reservation by remember { reservationViewModel.reservationoff }
    val isLoading by remember { reservationViewModel.loading }

    var showLoading by remember { mutableStateOf(false) }
    var showQrCode by remember { mutableStateOf(false) }
    var qrCodeBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(Unit) {
        reservationViewModel.getReservationOffline(reservationId) // Start loading
    }

    LaunchedEffect(showLoading) {
        if (showLoading) {
            reservationViewModel.updatePaymentStatus(reservationId, "Validated")
            showLoading = false
        }
    }

    if (isLoading) {
        AddProgress1(reservationViewModel = reservationViewModel, reservationId = reservationId)
    } else {
        reservation?.let {
            Log.d("OneReservation", "Reservation not null: $it")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Parking place : ${it.parking_place}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Entry DateTime: ${it.entry_datetime}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Exit DateTime: ${it.exit_datetime}",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = "Reservation Code: ${it.reservation_code}",
                        style = MaterialTheme.typography.labelMedium
                    )

                    // Payment Status Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { /* Handle click action */ },
                        contentAlignment = Alignment.Center
                    ) {
                        val paymentStatusColor = when (it.payment_status) {
                            "Validated" -> MaterialTheme.colorScheme.primary
                            "pending" -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.error
                        }
                        Column(
                            modifier = Modifier
                                .padding(8.dp)
                                .background(paymentStatusColor)
                                .clickable { /* Handle click action */ }
                        ) {
                            Text(
                                text = "Payment Status: ${it.payment_status}",
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(8.dp),
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = {
                                    showLoading = true
                                },
                                modifier = Modifier.padding(start = 8.dp),
                            ) {
                                Text(text = "Validate Payment", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // QR Code Section
                    Button(
                        onClick = {
                            showQrCode = true
                            qrCodeBitmap = it.reservation_code?.let { it1 -> generateQrCode(it1) }
                        },
                        modifier = Modifier.padding(start = 8.dp),
                    ) {
                        Text(text = "Show QR Code", style = MaterialTheme.typography.labelMedium)
                    }

                    if (showQrCode && qrCodeBitmap != null) {
                        Image(
                            bitmap = qrCodeBitmap!!.asImageBitmap(),
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(200.dp)
                                .padding(top = 16.dp)
                        )
                    }
                }
            }
        } ?: run {
            Log.e("OneReservation", "Reservation is null")
            // Handle null reservation scenario, maybe show an error message or navigate back
        }
    }
}

@Composable
fun AddProgress1(reservationViewModel: getMyReservationsViewModel, reservationId: Int?) {
    LaunchedEffect(Unit) {
        reservationViewModel.getReservationById(reservationId) // Start loading
    }

    val isLoading = reservationViewModel.loading1.value

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

fun generateQrCode(text: String): Bitmap? {
    val width = 512
    val height = 512
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val writer = MultiFormatWriter()
    try {
        val matrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (matrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
            }
        }
    } catch (e: WriterException) {
        e.printStackTrace()
    }
    return bitmap
}

 */