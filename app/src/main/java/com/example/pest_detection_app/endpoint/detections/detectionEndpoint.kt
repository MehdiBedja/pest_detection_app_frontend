package com.example.pest_detection_app.endpoint.detections



import com.example.pest_detection_app.data.user.NotesSyncRequest
import com.example.pest_detection_app.data.user.ServerIdsRequest
import com.example.pest_detection_app.data.user.ServerIdsRequestDel
import com.example.pest_detection_app.data.user.SoftDeletedDetectionsResponse
import com.example.pest_detection_app.data.user.SyncDetectionsResponse
import com.example.pest_detection_app.data.user.SyncDetectionsResponse1
import com.example.pest_detection_app.network.url
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query


interface detectionEndpoint {


    @POST("detection/fetch/")
    suspend fun fetchDetectionsFromServer(
        @Header("Authorization") authHeader: String,
        @Body request: ServerIdsRequest
    ): Response<SyncDetectionsResponse>



    @Multipart
    @POST("detection/upload/")
    suspend fun sendDetectionsToServer(
        @Header("Authorization") authHeader: String,
        @Part("detections") detections: RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<ResponseBody>



    @POST("detection/delete/batch/")
    suspend fun softDeleteDetections(
        @Header("Authorization") authToken: String,
        @Body request: ServerIdsRequestDel
    ): Response<ResponseBody>


    @GET("detection/deleted/")
    suspend fun getSoftDeletedDetections(
        @Header("Authorization") token: String): Response<SoftDeletedDetectionsResponse>


    @POST("detection/sync/notes/")
    suspend fun updateNotesOnServer(
        @Header("Authorization") authHeader: String,
        @Body request: NotesSyncRequest
    ): Response<SyncDetectionsResponse1>




    companion object {
        var endpoint: detectionEndpoint? = null
        fun createEndpoint(): detectionEndpoint {
            if (endpoint == null) {
                val logging = HttpLoggingInterceptor()
                logging.setLevel(HttpLoggingInterceptor.Level.BODY) // FULL logs

                val client = OkHttpClient.Builder()
                    .addInterceptor(logging)
                    .build()

                endpoint = Retrofit.Builder()
                    .baseUrl(url)
                    .client(client) // attach the client with logging
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(detectionEndpoint::class.java)
            }
            return endpoint!!
        }


    }




}