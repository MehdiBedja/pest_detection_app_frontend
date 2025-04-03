package com.example.pest_detection_app

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.example.pest_detection_app.model.BoundingBox
import com.example.pest_detection_app.model.Constants
import com.example.pest_detection_app.model.Detector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException
import android.graphics.Paint
import android.graphics.Typeface
import androidx.navigation.compose.rememberNavController
import com.example.pest_detection_app.RoomDatabase.DatabaseManager
import com.example.pest_detection_app.navigation.NavGraph
import com.example.pest_detection_app.preferences.initializeSession
import com.example.pest_detection_app.ui.theme.Pest_Detection_AppTheme


class MainActivity : ComponentActivity() {
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