package com.bedjamahdi.scanpestai.ViewModels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.bedjamahdi.scanpestai.RoomDatabase.DetectionResultDao
import com.bedjamahdi.scanpestai.RoomDatabase.BoundingBoxDao
import com.bedjamahdi.scanpestai.RoomDatabase.UserDao
import com.bedjamahdi.scanpestai.ViewModels.detection_result.DetectionSaveViewModel
import com.bedjamahdi.scanpestai.ViewModels.user.UserViewModelRoom

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
