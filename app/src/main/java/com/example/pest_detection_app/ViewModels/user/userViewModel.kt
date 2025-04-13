package com.example.pest_detection_app.ViewModels.user

import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.rememberNavController
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.RoomDatabase.DatabaseManager
import com.example.pest_detection_app.RoomDatabase.PestDetectionDatabase
import com.example.pest_detection_app.RoomDatabase.UserDao
import com.example.pest_detection_app.data.user.User
import com.example.pest_detection_app.preferences.Globals
import com.example.pest_detection_app.repository.user.AuthRepository
import com.example.pest_detection_app.repository.user.UserPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext






class AccountViewModel(private val authRepository: AuthRepository ) : ViewModel() {
    var loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)
    val createdSuccess=mutableStateOf(false)



    val user = mutableStateOf<com.example.pest_detection_app.RoomDatabase.User?>(null)




    fun signUpUser(
        email: String,
        username: String,
        lastName: String,
        firstName: String,
        phoneNumber: String,
        password: String,
        context: Context  // Context is needed to initialize the database
    ) {
        loading.value = true
        error.value = null

        viewModelScope.launch {
            try {
                val response = authRepository.signUpUser(
                    email = email,
                    username = username,
                    lastName = lastName,
                    firstName = null.toString(),
                    phoneNumber = null.toString(),
                    password = password
                )

                if (response.isSuccessful) {
                    val user = response.body() // Get full JSON response

                    if (user != null) {
                        // Initialize the Room database and UserDao

                        val userDao = DatabaseManager.userDao(context)

                        // Convert API response to Room Entity
                        val userEntity = com.example.pest_detection_app.RoomDatabase.User(
                            id = user.id,
                            username = user.username,
                            email = user.email,
                            last_name = user.last_name,
                            first_name = null,
                            phone_number = null,
                            date_of_birth = user.date_of_birth,
                            date_joined = user.date_joined,
                            profile_picture = user.profile_picture
                        )

                        // Save user to Room Database in background thread
                        withContext(Dispatchers.IO) {
                            userDao.insertUser(userEntity)
                        }

                        // Sign-up success
                        createdSuccess.value = true
                        Log.d("SignUp", "User successfully signed up and stored in Room DB: $user")

                    } else {
                        error.value = "Failed to get user data from server"
                        Log.e("SignUp", "Failed to get user data from server")
                    }
                } else {
                    error.value = "Failed to sign up: ${response.message()}"
                    Log.e("SignUp", "Failed to sign up: ${response.message()}")
                }
            } catch (e: Exception) {
                error.value = "Failed to sign up: ${e.message}"
                Log.e("SignUp", "Failed to sign up: ${e.message}")
            } finally {
                loading.value = false
            }
        }
    }



//    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            return AccountViewModel(authRepository) as T
//        }
//    }

    companion object {
        @Volatile
        private var instance: AccountViewModel? = null

        fun getInstance(authRepository: AuthRepository): AccountViewModel {
            return instance ?: synchronized(this) {
                instance ?: AccountViewModel(authRepository).also { instance = it }
            }
        }
    }
}




class LoginViewModel(
    private val authRepository: AuthRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    var loading = mutableStateOf(false)
    val error = mutableStateOf<String?>(null)

    private val _token = MutableStateFlow<String?>(Globals.savedToken)
    val token = _token.asStateFlow()

    private val _userId = MutableStateFlow<Int?>(Globals.savedUsername)
    val userId = _userId.asStateFlow()

    val login = mutableStateOf(false)
    val logout = mutableStateOf(false)

    val user = mutableStateOf<User?>(null)
    var loading1 = mutableStateOf(false)
    val error1 = mutableStateOf<String?>(null)

    val isLoggedIn = token.map { it != null }.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        false
    )


    fun logout() {
        userPreferences.clearCrediantials()
        Globals.savedToken = null  // Update global token
        _token.value = null
        _userId.value = -1 // Update state token
        logout.value = true
        login.value = false
        user.value = null  // Clear the user data

    }

    fun getUser() {
        loading1.value = true
        error1.value = null

        viewModelScope.launch {
            try {
                val authToken = Globals.savedToken ?: ""  // Get the token from Globals
                val response = authRepository.getUser(authToken)
                if (response.isSuccessful) {
                    user.value = response.body()
                    println("it did ")

                } else {
                    error1.value = "Failed to fetch user: ${response.message()}"
                    println("it did not")

                }
            } catch (e: Exception) {
                error1.value = "Failed to fetch user: ${e.message}"
            } finally {
                loading1.value = false
            }
        }
    }

    fun getToken(): String? {
        return userPreferences.getToken().also {
            Globals.savedToken = it  // Ensure Globals is updated
            _token.value = it
        }
    }


    fun getUserId(): Int {
        return userPreferences.getUserId().also {
            Globals.savedUsername = it  // Ensure Globals is updated
            _userId.value = it
        }
    }

    fun loginUser(email: String, password: String) {
        loading.value = true
        error.value = null

        viewModelScope.launch {
            try {
                val response = authRepository.loginUser(email, password)
                if (response.isSuccessful) {
                    val jsonResponse = response.body()
                    val tokenValue = jsonResponse?.token
                    val userId = jsonResponse?.user?.id

                    if (tokenValue != null && userId != null) {
                        userPreferences.updateValues(true, userId, tokenValue)

                        Globals.savedToken = tokenValue
                        _token.value = tokenValue
                        _userId.value = userId

                        Log.d("Login", "Login successful. Token: $tokenValue")

                        login.value = true
                        logout.value = false

                        val userDao = DatabaseManager.userDao(MyApp.getContext())
                        val existingUser = withContext(Dispatchers.IO) { userDao.getUserById(userId) }

                        if (existingUser == null) {
                            Log.d("Login", "User not found in Room, fetching from backend...")

                            val userResponse = authRepository.getUser(tokenValue)
                            if (userResponse.isSuccessful) {
                                val userData = userResponse.body()
                                if (userData != null) {
                                    val userEntity = com.example.pest_detection_app.RoomDatabase.User(
                                        id = userData.id,
                                        username = userData.username,
                                        email = userData.email,
                                        last_name = userData.last_name,
                                        first_name = userData.first_name,
                                        phone_number = userData.phone_number,
                                        date_of_birth = userData.date_of_birth,
                                        date_joined = userData.date_joined,
                                        profile_picture = userData.profile_picture
                                    )

                                    withContext(Dispatchers.IO) {
                                        userDao.insertUser(userEntity)
                                    }

                                    Log.d("Login", "User successfully stored in Room database")
                                }
                            } else {
                                Log.e("Login", "Failed to fetch user from backend: ${userResponse.message()}")
                            }
                        } else {
                            Log.d("Login", "User already exists in Room, no need to fetch.")
                        }
                    }
                } else {
                    error.value = if (response.code() == 401) {
                        "Incorrect username or password"
                    } else {
                        "Login failed: ${response.message()}"
                    }
                    Log.e("Login", "Failed to login: ${response.message()}")
                    _token.value = null
                    Globals.savedToken = null
                }
            } catch (e: Exception) {
                error.value = "Failed to login: ${e.message}"
                Log.e("Login", "Failed to login: ${e.message}")
                _token.value = null
                Globals.savedToken = null
            } finally {
                loading.value = false
            }
        }
    }





//    class Factory(private val authRepository: AuthRepository) : ViewModelProvider.Factory {
//        override fun <T : ViewModel> create(modelClass: Class<T>): T {
//            return LoginViewModel(authRepository) as T
//        }
//    }


    companion object {
        @Volatile
        private var instance: LoginViewModel? = null

        fun getInstance(authRepository: AuthRepository, userPreferences: UserPreferences): LoginViewModel {
            return instance ?: synchronized(this) {
                instance ?: LoginViewModel(authRepository, userPreferences).also { instance = it }
            }
        }
    }
}
