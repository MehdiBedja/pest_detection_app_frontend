package com.example.pest_detection_app.repository.detection

import android.util.Log
import com.example.pest_detection_app.data.user.DetectionNoteUpdate
import com.example.pest_detection_app.data.user.DetectionNoteUpdate1
import com.example.pest_detection_app.data.user.NotesSyncRequest
import com.example.pest_detection_app.data.user.ServerIdsRequest
import com.example.pest_detection_app.data.user.ServerIdsRequestDel
import com.example.pest_detection_app.data.user.SoftDeletedDetectionsResponse
import com.example.pest_detection_app.data.user.SyncDetectionsResponse
import com.example.pest_detection_app.data.user.SyncDetectionsResponse1
import com.example.pest_detection_app.endpoint.detections.detectionEndpoint
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Response

class DetectionRepository(private val endpoint: detectionEndpoint) {

    suspend fun sendIds(localServerIds: List<String>, authToken: String): Response<SyncDetectionsResponse> {
        // Filter out any empty UUIDs
        //  Log.d("API Request", "Filtered IDs: $localServerIds")

        val requestBody = ServerIdsRequest(ids = localServerIds)
        val token = "Token $authToken"

        // Retrofit will automatically serialize the request body to JSON
        return endpoint.fetchDetectionsFromServer(token, requestBody)
    }



    suspend fun sendDetectionsToServer(
        authToken: String,
        detectionsJson: String,
        images: List<MultipartBody.Part>
    ): Response<ResponseBody> {
        val token = "Token $authToken"
        val detectionsRequestBody = detectionsJson.toRequestBody("application/json".toMediaType())
        return endpoint.sendDetectionsToServer(token, detectionsRequestBody, images)
    }



    suspend fun softDeleteLocalDetections(localServerIds: List<String>, authToken: String): Response<ResponseBody> {
        val requestBody = ServerIdsRequestDel(server_ids = localServerIds)
        val token = "Token $authToken"

        //     Log.d("API Request", "Deleting IDs: $localServerIds")

        return endpoint.softDeleteDetections(token, requestBody)
    }


    suspend fun getSoftDeletedDetectionsFromServer(authToken: String): Response<SoftDeletedDetectionsResponse> {
        val token = "Token $authToken"
        return endpoint.getSoftDeletedDetections(token)
    }



    suspend fun updateNotesOnServer(
        authToken: String,
        detections: List<DetectionNoteUpdate>
    ): Response<SyncDetectionsResponse1> {
        val request = NotesSyncRequest(detections)
        return endpoint.updateNotesOnServer("Token $authToken", request)
    }



}
