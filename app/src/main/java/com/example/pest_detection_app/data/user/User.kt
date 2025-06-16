package com.example.pest_detection_app.data.user


import com.google.gson.annotations.SerializedName


// auth
data class SignUpRequest(
    @SerializedName("user_type") val userType: String,
    @SerializedName("user") val user: User_signUp
)

//for signup
data class User_signUp(
    val email: String,
    val username: String,
    @SerializedName("last_name") val lastName: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    val password: String
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class LoginResponse(
    val token: String,
    val user: User
)

data class User(
    val id: Int,
    val username: String,
    val email: String,
    val last_name: String?,
    val first_name: String?,
    val phone_number: String?,
    val date_of_birth: String?,
    val date_joined: String?,
    val profile_picture: String?
)


data class CustomUser(
    val id: Int,
    val username: String,
    val email: String,
    val profileImage: String?
)



data class GoogleSignUpRequest(
    @SerializedName("id_token") val idToken: String,
    @SerializedName("user_type") val userType: String
)



data class GoogleSignUpResponse(
    val token: String,
    val user: User,
    val message: String
)



// Add these data classes to your data models file

data class GoogleSignInRequest(
    val idToken: String
)

data class GoogleSignInResponse(
    val token: String,
    val user: User,
    val message: String
)




data class ChangePasswordRequest(
    val old_password: String? = null, // null for Google users
    val new_password: String
)


data class SetPasswordRequest(
    val new_password: String
)


data class PasswordResponse(
    val message: String,
    val success: Boolean
)



data class IsGoogleUserResponse(
    val is_google_user: Boolean,
    val has_usable_password: Boolean
)
