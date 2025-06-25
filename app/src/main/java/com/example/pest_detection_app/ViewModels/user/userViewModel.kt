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
import com.example.pest_detection_app.data.user.GoogleSignUpRequest
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
                            profile_picture = user.profile_picture ,
                            has_password = true
                        )

                        // Save user to Room Database in background thread
                        withContext(Dispatchers.IO) {
                            userDao.insertUser(userEntity)
                        }

                        // Sign-up success
                        createdSuccess.value = true
                        //   Log.d("SignUp", "User successfully signed up and stored in Room DB: $user")

                    } else {
                        error.value = "Failed to get user data from server"
                        //     Log.e("SignUp", "Failed to get user data from server")
                    }
                } else {
                    error.value = "Failed to sign up: ${response.message()}"
                    //    Log.e("SignUp", "Failed to sign up: ${response.message()}")
                }
            } catch (e: Exception) {
                error.value = "Failed to sign up: ${e.message}"
                //     Log.e("SignUp", "Failed to sign up: ${e.message}")
            } finally {
                loading.value = false
            }
        }
    }










    // Add this to your ViewModel's googleSignUp function for better debugging:

    fun googleSignUp(idToken: String, context: Context) {
        loading.value = true
        error.value = null

        //   Log.d("GoogleSignUpViewModel", "Starting Google sign-up process")
        //   Log.d("GoogleSignUpViewModel", "ID Token being sent: ${idToken.take(50)}...") // Only show first 50 chars for security

        viewModelScope.launch {
            try {
                val request = GoogleSignUpRequest(idToken, "client")
                //   Log.d("GoogleSignUpViewModel", "Request object created: id_token length=${idToken.length}, user_type=client")

                val response = authRepository.googleSignUp(idToken, "client")
                //  Log.d("GoogleSignUpViewModel", "Response received: code=${response.code()}, isSuccessful=${response.isSuccessful}")

                if (response.isSuccessful) {
                    val googleResponse = response.body()
                    //     Log.d("GoogleSignUpViewModel", "Response body: $googleResponse")

                    if (googleResponse != null) {
                        val user = googleResponse.user

                        // Initialize the Room database and UserDao
                        val userDao = DatabaseManager.userDao(context)

                        // Convert API response to Room Entity
                        val userEntity = com.example.pest_detection_app.RoomDatabase.User(
                            id = user.id,
                            username = user.username,
                            email = user.email,
                            last_name = user.last_name,
                            first_name = user.first_name,
                            phone_number = user.phone_number,
                            date_of_birth = user.date_of_birth,
                            date_joined = user.date_joined,
                            profile_picture = user.profile_picture ,
                            has_password = false
                        )

                        // Save user to Room Database in background thread
                        withContext(Dispatchers.IO) {
                            userDao.insertUser(userEntity)
                        }

                        // Sign-up success
                        createdSuccess.value = true
                        //      Log.d("GoogleSignUp", "User successfully signed up with Google and stored in Room DB: $user")
                    } else {
                        error.value = "Failed to get user data from server"
                        //      Log.e("GoogleSignUp", "Response body was null")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    //     Log.e("GoogleSignUpViewModel", "Error response: code=${response.code()}, body=$errorBody")

                    error.value = when {
                        response.code() == 400 && errorBody?.contains("already exists") == true ->
                            "Account with this email already exists. Please try logging in instead."
                        response.code() == 400 && !errorBody.isNullOrBlank() -> {
                            try {
                                val jsonObject = org.json.JSONObject(errorBody)
                                jsonObject.getString("error")
                            } catch (e: Exception) {
                                "Invalid Google authentication. Details: $errorBody"
                            }
                        }
                        response.code() == 400 -> "Invalid Google authentication. Please try again."
                        else -> "Failed to sign up with Google: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                error.value = "Failed to sign up with Google: ${e.message}"
                //     Log.e("GoogleSignUpViewModel", "Exception during Google sign-up", e)
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

    private val _isGoogle = MutableStateFlow<Boolean?>(null)
    val isGoogle = _isGoogle.asStateFlow()


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
        _isGoogle.value = null
        logout.value = true
        login.value = false
        user.value = null  // Clear the user data
        Globals.isGoogle = null


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




                        loadIsGoogle()



                        //     Log.d("Login", "Login successful. Token: $tokenValue")

                        login.value = true
                        logout.value = false

                        val userDao = DatabaseManager.userDao(MyApp.getContext())
                        val existingUser =
                            withContext(Dispatchers.IO) { userDao.getUserById(userId) }

                        if (existingUser == null) {
                            //      Log.d("Login", "User not found in Room, fetching from backend...")

                            val userResponse = authRepository.getUser(tokenValue)
                            if (userResponse.isSuccessful) {
                                val userData = userResponse.body()
                                if (userData != null) {
                                    val userEntity =
                                        com.example.pest_detection_app.RoomDatabase.User(
                                            id = userData.id,
                                            username = userData.username,
                                            email = userData.email,
                                            last_name = userData.last_name,
                                            first_name = userData.first_name,
                                            phone_number = userData.phone_number,
                                            date_of_birth = userData.date_of_birth,
                                            date_joined = userData.date_joined,
                                            profile_picture = userData.profile_picture ,
                                            has_password = true
                                        )

                                    withContext(Dispatchers.IO) {
                                        userDao.insertUser(userEntity)
                                    }

                                    //        Log.d("Login", "User successfully stored in Room database")
                                }
                            } else {
                                //    Log.e("Login", "Failed to fetch user from backend: ${userResponse.message()}")
                            }
                        } else {
                            //    Log.d("Login", "User already exists in Room, no need to fetch.")
                        }
                    }
                } else {
                    error.value = if (response.code() == 401) {
                        "Incorrect username or password"
                    } else {
                        "Login failed: ${response.message()}"
                    }
                    //   Log.e("Login", "Failed to login: ${response.message()}")
                    _token.value = null
                    Globals.savedToken = null
                }
            } catch (e: Exception) {
                error.value = "Failed to login: ${e.message}"
                //  Log.e("Login", "Failed to login: ${e.message}")
                _token.value = null
                Globals.savedToken = null
            } finally {
                loading.value = false
            }
        }
    }

    // Add this function to your LoginViewModel class
    fun loginWithGoogle(idToken: String) {
        loading.value = true
        error.value = null

        //    Log.d("GoogleSignInViewModel", "Starting Google sign-in process")
        //    Log.d("GoogleSignInViewModel", "ID Token being sent: ${idToken.take(50)}...") // Only show first 50 chars for security

        viewModelScope.launch {
            try {
                val response = authRepository.googleSignIn(idToken)
                //      Log.d("GoogleSignInViewModel", "Response received: code=${response.code()}, isSuccessful=${response.isSuccessful}")

                if (response.isSuccessful) {
                    val googleResponse = response.body()
                    //      Log.d("GoogleSignInViewModel", "Response body: $googleResponse")

                    if (googleResponse != null) {
                        val tokenValue = googleResponse.token
                        val user = googleResponse.user

                        if (tokenValue != null && user != null) {
                            // Save credentials to preferences
                            //      Log.d("Login", "Login successful. Token: $tokenValue")

                            userPreferences.updateValues(true, user.id, tokenValue )
                            Globals.savedToken = tokenValue
                            loadIsGoogle()
                            // Update global state
                            Globals.isGoogle = _isGoogle.value
                            _token.value = tokenValue
                            _userId.value = user.id


                            //       Log.d("GoogleSignIn", "Sign-in successful. Token: $tokenValue")

                            login.value = true
                            logout.value = false

                            // Check if user exists in Room database
                            val userDao = DatabaseManager.userDao(MyApp.getContext())
                            val existingUser =
                                withContext(Dispatchers.IO) { userDao.getUserById(user.id) }

                            if (existingUser == null) {
                                //      Log.d("GoogleSignIn", "User not found in Room, storing user data...")

                                // Convert API response to Room Entity
                                val userEntity = com.example.pest_detection_app.RoomDatabase.User(
                                    id = user.id,
                                    username = user.username,
                                    email = user.email,
                                    last_name = user.last_name,
                                    first_name = user.first_name,
                                    phone_number = user.phone_number,
                                    date_of_birth = user.date_of_birth,
                                    date_joined = user.date_joined,
                                    profile_picture = user.profile_picture ,
                                    has_password = false
                                )

                                // Save user to Room Database
                                withContext(Dispatchers.IO) {
                                    userDao.insertUser(userEntity)
                                }

                                //      Log.d("GoogleSignIn", "User successfully stored in Room database")
                            } else {
                                //        Log.d("GoogleSignIn", "User already exists in Room, no need to store.")
                            }
                        } else {
                            error.value = "Failed to get authentication data from server"
                            //     Log.e("GoogleSignIn", "Token or user data was null")
                        }
                    } else {
                        error.value = "Failed to get response data from server"
                        //    Log.e("GoogleSignIn", "Response body was null")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    //     Log.e("GoogleSignInViewModel", "Error response: code=${response.code()}, body=$errorBody")

                    error.value = when {
                        response.code() == 404 -> "Account not found. Please sign up first."
                        response.code() == 400 && !errorBody.isNullOrBlank() -> {
                            try {
                                val jsonObject = org.json.JSONObject(errorBody)
                                jsonObject.getString("error")
                            } catch (e: Exception) {
                                "Invalid Google authentication. Details: $errorBody"
                            }
                        }

                        response.code() == 400 -> "Invalid Google authentication. Please try again."
                        else -> "Failed to sign in with Google: ${response.message()}"
                    }
                }
            } catch (e: Exception) {
                error.value = "Failed to sign in with Google: ${e.message}"
                //        Log.e("GoogleSignInViewModel", "Exception during Google sign-in", e)
            } finally {
                loading.value = false
            }
        }
    }



    // Add these to your LoginViewModel class

    // Password change states
    val passwordLoading = mutableStateOf(false)
    val passwordError = mutableStateOf<String?>(null)
    val passwordSuccess = mutableStateOf(false)

    fun changePassword(
        userId: Int,
        oldPassword: String?,
        newPassword: String
    ) {
        passwordLoading.value = true
        passwordError.value = null
        passwordSuccess.value = false

        viewModelScope.launch {
            try {
                val token = Globals.savedToken ?: run {
                    passwordError.value = "Authentication token not found"
                    passwordLoading.value = false
                    return@launch
                }

                val response = if (_isGoogle.value == false) {
                    // Google user setting password
                    authRepository.setPassword(newPassword, token)
                } else {
                    // Normal user changing password
                    if (oldPassword.isNullOrBlank()) {
                        passwordError.value = "Old password is required"
                        passwordLoading.value = false
                        return@launch
                    }
                    authRepository.changePassword(oldPassword, newPassword, token)
                }

                if (response.isSuccessful) {
                    val passwordResponse = response.body()
                    if (passwordResponse?.success == true) {
                        passwordSuccess.value = true
                        //     Log.d("PasswordChange", "Password ${if (_isGoogle.value == false) "set" else "changed"} successfully")
                        DatabaseManager.userDao(MyApp.getContext()).updateHasPassword(userId , true)
                    } else {
                        passwordError.value = passwordResponse?.message ?: "Failed to ${if (_isGoogle.value == false) "set" else "change"} password"
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    passwordError.value = when (response.code()) {
                        400 -> "Invalid password format or old password incorrect"
                        401 -> "Unauthorized. Please login again"
                        else -> "Failed to ${if (_isGoogle.value == false) "set" else "change"} password: ${response.message()}"
                    }
                    //      Log.e("PasswordChange", "Error: ${response.code()}, Body: $errorBody")
                }
            } catch (e: Exception) {
                passwordError.value = "Failed to change password: ${e.message}"
                //     Log.e("PasswordChange", "Exception during password change", e)
            } finally {
                passwordLoading.value = false
            }
        }
    }


    fun clearPasswordStates() {
        passwordError.value = null
        passwordSuccess.value = false
        passwordLoading.value = false
    }


    suspend fun loadIsGoogle() {
        try {
            val token = Globals.savedToken
            if (token != null) {
                //       Log.d("LoadIsGoogle", "Starting loadIsGoogle with token: ${token.take(20)}...")
                val response = authRepository.checkIsGoogleUser(token)
                if (response.isSuccessful) {
                    val isGoogleResponse = response.body()
                    _isGoogle.value = isGoogleResponse?.has_usable_password
                    Globals.isGoogle = isGoogleResponse?.has_usable_password
                    //         Log.d("LoadIsGoogle", "Successfully loaded isGoogle: ${isGoogleResponse?.is_google_user}")
                    //         Log.d("LoadIsGoogle", "has_usable_password: ${isGoogleResponse?.has_usable_password}")
                } else {
                    //       Log.e("LoadIsGoogle", "Failed to load isGoogle: ${response.message()}")
                    _isGoogle.value = null
                    Globals.isGoogle = null
                }
            } else {
                //       Log.w("LoadIsGoogle", "Token is null, cannot load isGoogle")
                _isGoogle.value = null
                Globals.isGoogle = null
            }
        } catch (e: Exception) {
            //      Log.e("LoadIsGoogle", "Exception loading isGoogle: ${e.message}")
            _isGoogle.value = null
            Globals.isGoogle = null
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
