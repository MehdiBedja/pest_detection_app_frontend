package com.bedjamahdi.scanpestai

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.bedjamahdi.scanpestai.navigation.NavGraph
import com.bedjamahdi.scanpestai.preferences.initializeSession
import com.bedjamahdi.scanpestai.screen.LanguagePref
import com.bedjamahdi.scanpestai.screen.updateLocale
import com.bedjamahdi.scanpestai.screen.user.DarkModePref
import com.bedjamahdi.scanpestai.ui.theme.Pest_Detection_AppTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = LanguagePref.getLanguage(newBase)
        val updatedContext = newBase.updateLocale(lang)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Get the saved dark mode preference
            val isDarkMode = DarkModePref.getDarkMode(this)

            Pest_Detection_AppTheme(darkTheme = isDarkMode,dynamicColor = false) {
                initializeSession(applicationContext)
                val navController = rememberNavController()
                NavGraph(navController)
            }
        }
    }
}
