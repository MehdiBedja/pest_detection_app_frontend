package com.example.pest_detection_app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.pest_detection_app.navigation.NavGraph
import com.example.pest_detection_app.preferences.initializeSession
import com.example.pest_detection_app.screen.LanguagePref
import com.example.pest_detection_app.screen.updateLocale
import com.example.pest_detection_app.ui.theme.Pest_Detection_AppTheme

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        val lang = LanguagePref.getLanguage(newBase)
        val updatedContext = newBase.updateLocale(lang)
        super.attachBaseContext(updatedContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Pest_Detection_AppTheme {
                initializeSession(applicationContext)
                val navController = rememberNavController()
                NavGraph(navController)
            }
        }
    }
}
