// 3. OnboardingManager.kt - SharedPreferences utility
package com.example.pest_detection_app.screen

import android.content.Context
import android.content.SharedPreferences

class OnboardingManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val HAS_SEEN_ONBOARDING = "has_seen_onboarding"
    }

    fun setOnboardingSeen(seen: Boolean) {
        sharedPreferences.edit()
            .putBoolean(HAS_SEEN_ONBOARDING, seen)
            .apply()
    }

    fun hasSeenOnboarding(): Boolean {
        return sharedPreferences.getBoolean(HAS_SEEN_ONBOARDING, false)
    }
}