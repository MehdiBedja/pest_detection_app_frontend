package com.bedjamahdi.scanpestai.endpoint.user

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
import com.bedjamahdi.scanpestai.network.url
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query


interface userEndpoint {

    //auth


    @POST("user_management/signup")
    suspend fun signUp(@Body request: SignUpRequest): Response<User>

    @POST("user_management/test")
    suspend fun googleSignUp(@Body request: GoogleSignUpRequest): Response<GoogleSignUpResponse>



    @POST("user_management/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    // Add this to your userEndpoint interface
    @POST("user_management/google_sign_in")
    suspend fun googleSignIn(@Body request: GoogleSignInRequest): Response<GoogleSignInResponse>

    @GET("user_management/get_user_info/")
    suspend fun getUser(
        @Query("id") id: Int?,
        @Header("Authorization") authHeader: String
    ): Response<User>

    @GET("user_management/get_user_info/")
    suspend fun getUser(
        @Header("Authorization") authHeader: String
    ): Response<User> // Removed `id` query parameter



    companion object {
        var endpoint: userEndpoint? = null
        fun createEndpoint(): userEndpoint {
            if(endpoint ==null) {
                endpoint = Retrofit.Builder().baseUrl(url).
                addConverterFactory(GsonConverterFactory.create()).build().
                create(userEndpoint::class.java)
            }
            return endpoint!!

        }

    }




    @POST("user_management/change_password")
    suspend fun changePassword(
        @Body request: ChangePasswordRequest,
        @Header("Authorization") authHeader: String
    ): Response<PasswordResponse>

    @POST("user_management/set_password")
    suspend fun setPassword(
        @Body request: SetPasswordRequest,
        @Header("Authorization") authHeader: String
    ): Response<PasswordResponse>



    @GET("user_management/check_is_google_user")
    suspend fun checkIsGoogleUser(
        @Header("Authorization") token: String
    ): Response<IsGoogleUserResponse>

}