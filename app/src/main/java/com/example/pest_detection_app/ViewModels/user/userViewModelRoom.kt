package com.example.pest_detection_app.ViewModels.user

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.pest_detection_app.RoomDatabase.User
import com.example.pest_detection_app.RoomDatabase.UserDao
import kotlinx.coroutines.launch

class UserViewModelRoom(
    application: Application,
    private val userDao: UserDao) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    fun fetchUserById(userId: Int) {
        viewModelScope.launch {
            val fetchedUser = userDao.getUserById(userId)
            if (fetchedUser != null) {
                Log.d("UserFetch", "✅ Loaded user from local Room DB (not from server)")
            } else {
                Log.d("UserFetch", "⚠️ No user found in local DB with id: $userId")
            }
            _user.postValue(fetchedUser)
        }
    }

}