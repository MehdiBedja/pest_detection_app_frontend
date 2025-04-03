/* import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.reservation_app_frontend.data.parking.Parking
import com.example.reservation_app_frontend.data.parking.ParkingPlace
import com.example.reservation_app_frontend.repository.parking.ParkingRepository
import com.example.reservation_app_frontend.viewModel.parking.getParkingsViewModel
import com.example.reservation_app_frontend.viewModel.reservation.getMyReservationsViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class getAllReservationModel(private val parkingRepository: ParkingRepository) : ViewModel() {
    // StateFlows for different types of data
    private val _parkings = MutableStateFlow<List<Parking>>(emptyList())
    val parkings: StateFlow<List<Parking>> = _parkings

    private val _parkingPlaces = MutableStateFlow<List<ParkingPlace>>(emptyList())
    val parkingPlaces: StateFlow<List<ParkingPlace>> = _parkingPlaces

    // Error handling
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    // Loading indicator
    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading

    fun fetchParkings() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.value = true
                Log.d("getAllReservationModel", "Fetching parkings...")
                val response = parkingRepository.getParkings()
                if (response.isSuccessful) {
                    val data = response.body()
                    _parkings.value = data ?: emptyList()
                    Log.d("getAllReservationModel", "Fetched parkings successfully. Length: ${_parkings.value.size}")
                    Log.d("getAllReservationModel", "Parkings list: ${_parkings.value}")
                } else {
                    _error.value = "Failed to fetch parkings: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch parkings: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }

    // Fetch parking places based on the parking ID
    fun fetchParkingPlaces(parkingId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _loading.value = true
                Log.d("getAllReservationModel", "Fetching parking places...")
                val response = parkingRepository.getAllParkingPlaces(parkingId)
                if (response.isSuccessful) {
                    val data = response.body()
                    if (data != null) {
                        _parkingPlaces.value = data
                        Log.d("getAllReservationModel", "Fetched parking places successfully. Length: ${data.size}")
                        Log.d("getAllReservationModel", "Parking Places list: $data")
                    } else {
                        _error.value = "Empty response received."
                    }
                } else {
                    _error.value = "Failed to fetch Parking Places: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Failed to fetch Parking Places: ${e.message}"
            } finally {
                _loading.value = false
            }
        }
    }


    // ViewModel Factory
    class Factory(private val parkingRepository: ParkingRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return getAllReservationModel(parkingRepository) as T
        }
    }
}

 */