package com.example.pest_detection_app.ViewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pest_detection_app.RoomDatabase.DetectionResultDao
import com.example.pest_detection_app.RoomDatabase.BoundingBoxDao
import com.example.pest_detection_app.RoomDatabase.UserDao
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.UserViewModelRoom

class DetectionSaveViewModelFactory(
    private val application: Application, // Add Application parameter
    private val detectionResultDao: DetectionResultDao,
    private val boundingBoxDao: BoundingBoxDao
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetectionSaveViewModel::class.java)) {
            // Pass the application context when creating the ViewModel
            return DetectionSaveViewModel(application, detectionResultDao, boundingBoxDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class UserViewModelFactory (
    private val application : Application ,
    private val userdao : UserDao

) : ViewModelProvider.Factory{
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModelRoom::class.java)) {
            // Pass the application context when creating the ViewModel
            return UserViewModelRoom(application, userdao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
