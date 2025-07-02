package com.bedjamahdi.scanpestai.repository.user

import com.bedjamahdi.scanpestai.preferences.Preferences

class UserPreferences(private val preferences: Preferences) {


    fun updateValues(connected: Boolean, userId: Int, tokenvalue: String ) =
        preferences.updateValues(connected, userId, tokenvalue )

    fun clearCrediantials() = preferences.clearCredentials()
    fun getToken() = preferences.getToken()
    fun getUserId() = preferences.getUser()

}