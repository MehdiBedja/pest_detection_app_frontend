package com.example.pest_detection_app.preferences

import android.content.Context
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class Preferences(private val context: Context) {

    private val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)

    fun getToken(): String? {
        return sharedPreferences.getString("token", null)
    }

    fun getUser(): Int {
        return sharedPreferences.getInt("userId", -1)
    }

    fun updateValues(connected: Boolean, userId: Int, token: String) {
        Globals.savedUsername = userId
        Globals.savedToken = token
        sharedPreferences.edit {
            putInt("userId", userId)
            putString("token", token)
            putBoolean("connected", connected)
            apply()
        }
    }

    fun clearCredentials() {
        Globals.savedUsername = null
        Globals.savedToken = null
        Globals.isGoogle = null

        sharedPreferences.edit {
            remove("userId")
            remove("token")
            remove("connected")
            apply()
        }
    }

    // Generic methods for boolean preferences
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return sharedPreferences.getBoolean(key, defaultValue)
    }

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit {
            putBoolean(key, value)
            apply()
        }
    }

    // Generic methods for string preferences
    fun getString(key: String, defaultValue: String? = null): String? {
        return sharedPreferences.getString(key, defaultValue)
    }

    fun putString(key: String, value: String) {
        sharedPreferences.edit {
            putString(key, value)
            apply()
        }
    }

    // Generic methods for int preferences
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return sharedPreferences.getInt(key, defaultValue)
    }

    fun putInt(key: String, value: Int) {
        sharedPreferences.edit {
            putInt(key, value)
            apply()
        }
    }

    // Generic methods for long preferences
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return sharedPreferences.getLong(key, defaultValue)
    }

    fun putLong(key: String, value: Long) {
        sharedPreferences.edit {
            putLong(key, value)
            apply()
        }
    }

    // Generic methods for float preferences
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return sharedPreferences.getFloat(key, defaultValue)
    }

    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit {
            putFloat(key, value)
            apply()
        }
    }

    // Method to remove a specific key
    fun remove(key: String) {
        sharedPreferences.edit {
            remove(key)
            apply()
        }
    }

    // Method to check if a key exists
    fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    // Method to clear all preferences (be careful with this)
    fun clearAll() {
        sharedPreferences.edit {
            clear()
            apply()
        }
    }
}

object Globals {
    var savedUsername: Int? = null
    var savedToken: String? = null
    var isGoogle: Boolean? = null
}

fun initializeSession(context: Context) {
    val sharedPreferences = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    Globals.savedUsername = sharedPreferences.getInt("userId", -1)
    Globals.savedToken = sharedPreferences.getString("token", null)
}