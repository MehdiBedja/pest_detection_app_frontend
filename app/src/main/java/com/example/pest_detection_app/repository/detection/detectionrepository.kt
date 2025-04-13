package com.example.pest_detection_app.repository.detection

import android.util.Log
import com.example.pest_detection_app.data.user.ServerIdsRequest
import com.example.pest_detection_app.data.user.SyncDetectionsResponse
import com.example.pest_detection_app.endpoint.detections.detectionEndpoint
import retrofit2.Response

class DetectionRepository(private val endpoint: detectionEndpoint) {

    suspend fun sendIds(localServerIds: List<String>, authToken: String): Response<SyncDetectionsResponse> {
        // Filter out any empty UUIDs
        Log.d("API Request", "Filtered IDs: $localServerIds")

        val requestBody = ServerIdsRequest(ids = localServerIds)
        val token = "Token $authToken"

        // Retrofit will automatically serialize the request body to JSON
        return endpoint.fetchDetectionsFromServer(token, requestBody)
    }
}
