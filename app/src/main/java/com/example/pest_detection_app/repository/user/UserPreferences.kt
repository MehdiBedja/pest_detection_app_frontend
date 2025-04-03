package com.example.pest_detection_app.repository.user

import com.example.pest_detection_app.preferences.Preferences

class UserPreferences(private val preferences: Preferences) {


    fun updateValues(connected: Boolean, userId: Int, tokenvalue: String) =
        preferences.updateValues(connected, userId, tokenvalue)

    fun clearCrediantials() = preferences.clearCredentials()
    fun getToken() = preferences.getToken()
    fun getUserId() = preferences.getUser()

}