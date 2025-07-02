package com.bedjamahdi.scanpestai.repository.user

import com.bedjamahdi.scanpestai.data.user.ChangePasswordRequest
import com.bedjamahdi.scanpestai.data.user.GoogleSignInRequest
import com.bedjamahdi.scanpestai.data.user.GoogleSignInResponse
import com.bedjamahdi.scanpestai.data.user.GoogleSignUpRequest
import com.bedjamahdi.scanpestai.data.user.GoogleSignUpResponse
import com.bedjamahdi.scanpestai.data.user.IsGoogleUserResponse
import com.bedjamahdi.scanpestai.data.user.LoginRequest
import com.bedjamahdi.scanpestai.data.user.LoginResponse
import com.bedjamahdi.scanpestai.data.user.PasswordResponse
import com.bedjamahdi.scanpestai.data.user.SetPasswordRequest
import com.bedjamahdi.scanpestai.data.user.SignUpRequest
import com.bedjamahdi.scanpestai.data.user.User
import com.bedjamahdi.scanpestai.data.user.User_signUp
import com.bedjamahdi.scanpestai.endpoint.user.userEndpoint
import okhttp3.ResponseBody
import retrofit2.Response


class AuthRepository(private val endpoint: userEndpoint) {

    suspend fun signUpUser(
        email: String,
        username: String,
        lastName: String,
        firstName: String,
        phoneNumber: String,
        password: String
    ): Response<User> {
        val signUpRequest = SignUpRequest(
            userType = "client",
            user = User_signUp(
                email = email,
                username = username,
                lastName = lastName,
                firstName = firstName,
                phoneNumber = phoneNumber,
                password = password
            )
        )
        return endpoint.signUp(signUpRequest)
    }

    suspend fun loginUser(username: String, password: String): Response<LoginResponse> {
        val loginRequest = LoginRequest(
            username = username,
            password = password
        )
        return endpoint.login(loginRequest)
    }

    suspend fun getUser(token: String): Response<User> {
        return endpoint.getUser("Token $token") // Only use the token now
    }

    suspend fun googleSignUp(idToken: String, userType: String = "client"): Response<GoogleSignUpResponse> {
        val googleSignUpRequest = GoogleSignUpRequest(
            idToken = idToken,
            userType = userType
        )
        return endpoint.googleSignUp(googleSignUpRequest)
    }

    // Add this function to your AuthRepository class
    suspend fun googleSignIn(idToken: String): Response<GoogleSignInResponse> {
        val googleSignInRequest = GoogleSignInRequest(
            idToken = idToken
        )
        return endpoint.googleSignIn(googleSignInRequest)
    }



    // Add these functions to your existing AuthRepository class

    suspend fun changePassword(
        oldPassword: String,
        newPassword: String,
        token: String
    ): Response<PasswordResponse> {
        val changePasswordRequest = ChangePasswordRequest(
            old_password = oldPassword,
            new_password = newPassword
        )
        return endpoint.changePassword(changePasswordRequest, "Token $token")
    }

    suspend fun setPassword(
        newPassword: String,
        token: String
    ): Response<PasswordResponse> {
        val setPasswordRequest = SetPasswordRequest(
            new_password = newPassword
        )
        return endpoint.setPassword(setPasswordRequest, "Token $token")
    }


    suspend fun checkIsGoogleUser(token: String): Response<IsGoogleUserResponse> {
        return endpoint.checkIsGoogleUser("Token $token")

    }

    }
