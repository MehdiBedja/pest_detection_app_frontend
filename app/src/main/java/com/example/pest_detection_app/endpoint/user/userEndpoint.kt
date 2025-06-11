package com.example.pest_detection_app.endpoint.user

import com.example.pest_detection_app.data.user.GoogleSignInRequest
import com.example.pest_detection_app.data.user.GoogleSignInResponse
import com.example.pest_detection_app.data.user.GoogleSignUpRequest
import com.example.pest_detection_app.data.user.GoogleSignUpResponse
import com.example.pest_detection_app.data.user.LoginRequest
import com.example.pest_detection_app.data.user.LoginResponse
import com.example.pest_detection_app.data.user.SignUpRequest
import com.example.pest_detection_app.data.user.User
import com.example.pest_detection_app.network.url
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


}