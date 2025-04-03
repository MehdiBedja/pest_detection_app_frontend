/* package com.example.reservation_app_frontend.viewModel.reservation

import android.annotation.SuppressLint
import android.content.ContentValues.TAG
import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reservation_app_frontend.data.reservation.Reservation
import com.example.reservation_app_frontend.data.reservation.ReservationDTO2
import com.example.reservation_app_frontend.network.Globals
import com.example.reservation_app_frontend.repository.reservation.ReservationRepository
import com.example.reservation_app_frontend.roomDatabase.DatabaseManager
import com.example.reservation_app_frontend.roomDatabase.ReservationEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class getMyReservationsViewModel(private val reservationRepository: ReservationRepository) : ViewModel() {
    val reservations = mutableStateListOf<Reservation>()
    var loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    val reservation = mutableStateOf<ReservationDTO2?>(null)
    var loading1 = mutableStateOf(false)
    val error1 = mutableStateOf<String?>(null)


    var reservationoff = mutableStateOf<ReservationEntity?>(null)
    var loading1off = mutableStateOf(false)
    val error1off = mutableStateOf<String?>(null)





    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing
    
    fun fetchReservations() {
        loading.value = true // Set loading to true before fetching data
        error.value = null // Clear any previous error messages

        val username = Globals.savedUsername ?: return // Handle null case if necessary

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = reservationRepository.getMyReservations(username)
                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d(TAG, "Data received: $data")

                    reservations.clear()
                    data?.let {
                        reservations.addAll(it)
                    }
                } else {
                    error.value = "Failed to fetch reservations: ${response.message()}"
                    Log.e(TAG, "Failed to fetch reservations: ${response.message()}")
                }
            } catch (e: Exception) {
                error.value = "Failed to fetch reservations: ${e.message}"
                Log.e(TAG, "Failed to fetch reservations: ${e.message}")
            } finally {
                loading.value = false
            }
        }
    }
    val reservationsentity = mutableStateListOf<ReservationEntity>()

    fun getReservationsOffline() {
        val userId = Globals.savedUsername ?: return // Handle null case if necessary

        loading.value = true // Set loading to true before fetching data
        error.value = null // Clear any previous error messages

        viewModelScope.launch {
            try {
                // Directly fetch data within the coroutine, no nested launch needed
                val resultList = withContext(Dispatchers.IO) {
                    DatabaseManager.reservationDao.getReservationsByUserId(userId.toInt())
                }

                Log.d(TAG, "Data received: $resultList")
                reservationsentity.clear()
                reservationsentity.addAll(resultList) // Update your mutable state list
            } catch (e: Exception) {
                error.value = "Failed to fetch reservations: ${e.message}"
                Log.e(TAG, "Failed to fetch reservations: ${e.message}")
            } finally {
                loading.value = false // Update loading state
            }
        }
    }

      @SuppressLint("SuspiciousIndentation")
      suspend fun updatePaymentStatus(reservationId: Int?, status: String?) {
          reservationId?.let { id ->
              status?.let { newStatus ->
                  DatabaseManager.reservationDao.updatePaymentStatus1(id, newStatus)
                  // Refresh the reservationoff state after updating the payment status
                  withContext(Dispatchers.IO) {
                      reservationoff.value = DatabaseManager.reservationDao.getReservationOffline(id)
                  }
              }
          }
      }




    // Function to select payment status
    suspend fun getPaymentStatus(reservationId: Int): String? {
        // Retrieve the payment status using the DAO query
        return DatabaseManager.reservationDao.getPaymentStatus(reservationId)
    }


    fun getReservationOffline(reservationId: Int?) {

        loading1off.value = true // Set loading to true before fetching data
        error1off.value = null // Clear any previous error messages

        viewModelScope.launch {
            try {
                // Directly fetch data within the coroutine, no nested launch needed
                val resultList = withContext(Dispatchers.IO) {
                    DatabaseManager.reservationDao.getReservationOffline(reservationId)
                }

                Log.d(TAG, "Data received: $resultList")
                reservationoff.value = resultList // Update your mutable state list
            } catch (e: Exception) {
                error1off.value = "Failed to fetch reservations: ${e.message}"
                Log.e(TAG, "Failed to fetch reservations: ${e.message}")
            } finally {
                loading1off.value = false // Update loading state
            }
        }
    }










    fun getReservationById(reservationId: Int?) {
        loading1.value = true // Set loading to true before fetching data
        error1.value = null // Clear any previous error messages

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = reservationRepository.getReservation(reservationId)
                Log.d(TAG, "Response code: ${response.code()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    reservation.value = data // Assuming 'reservation' is a mutableStateOf<Reservation?> in your ViewModel

                    Log.d(TAG, "Data received: $data")
                } else {
                    error1.value = "Failed to fetch reservation: ${response.message()}"
                    Log.e(TAG, "Failed to fetch reservation: ${response.message()}")
                }
            } catch (e: Exception) {
                error1.value = "Failed to fetch reservation: ${e.message}"
                Log.e(TAG, "Failed to fetch reservation: ${e.message}")
            } finally {
                loading1.value = false
            }
        }
    }

    class Factory(private val reservationRepository: ReservationRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return getMyReservationsViewModel(reservationRepository) as T
        }
    }
}

 */