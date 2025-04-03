/* package com.example.pest_detection_app.endpoint.detections


import com.example.pest_detection_app.network.url
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ReservationEndpoint {
    @GET("reservations/myReservations/{id}/") // Update the endpoint URL as needed
    suspend fun getAllReservations(@Path("id") id: Int): Response<List<>>


    @POST("reservations/addReservation/") // Include the trailing slash
    suspend fun createReservation(@Body reservationDTO: ReservationDTO)




    @GET("reservations/getReservation/{id}/")
    suspend fun getReservation(@Path("id") id: Int?): Response<ReservationDTO2>



    companion object {
        var endpoint: ReservationEndpoint? = null
        fun createEndpoint(): ReservationEndpoint {
            if (endpoint == null) {
                endpoint = Retrofit.Builder()
                    .baseUrl(url) // Make sure 'url' is defined or replace it with your base URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(ReservationEndpoint::class.java)
            }
            return endpoint!!
        }
    }
}


 */
