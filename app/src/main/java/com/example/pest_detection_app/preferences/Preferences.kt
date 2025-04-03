package com.example.pest_detection_app.preferences

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Preferences(private val context: Context) {

    fun getToken(): String? {
        val pref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return pref.getString("token", null)
    }

    fun getUser(): Int {
        val pref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        return pref.getInt("userId", -1)
    }


    fun updateValues(connected: Boolean, userId: Int, token: String) {
        val pref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        Globals.savedUsername = userId
        pref.edit {
            putInt("userId", userId)
            putString("token", token)  // Save the token
            putBoolean("connected", connected)
            apply()  // Apply changes

        }
    }

    fun clearCredentials() {

        Globals.savedUsername = null
        Globals.savedToken = null

        val pref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        pref.edit {
            remove("userId")
            remove("token")  // Clear the token
            remove("connected")
            apply()  // Apply changes

        }
    }
}

object Globals {
    var savedUsername: Int? = null
    var savedToken: String? = null
}

fun initializeSession(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    Globals.savedUsername = sharedPreferences.getInt("userId", -1)
    Globals.savedToken = sharedPreferences.getString("token", null)
}
