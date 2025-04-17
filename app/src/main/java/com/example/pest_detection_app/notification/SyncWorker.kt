package com.example.pest_detection_app.notification
/*
import android.app.Application
import androidx.compose.ui.platform.LocalContext
import com.example.pest_detection_app.MyApp
import com.example.pest_detection_app.RoomDatabase.DatabaseManager
import com.example.pest_detection_app.ViewModels.DetectionSaveViewModelFactory
import com.example.pest_detection_app.ViewModels.DetectionViewModel
import com.example.pest_detection_app.ViewModels.UserViewModelFactory
import com.example.pest_detection_app.ViewModels.detection_result.DetectionSaveViewModel
import com.example.pest_detection_app.ViewModels.user.UserViewModelRoom
import com.example.pest_detection_app.preferences.Globals
import com.example.pest_detection_app.repository.detection.DetectionRepository

class SyncWorker(
    workerParams: WorkerParameters
) : CoroutineWorker( workerParams) {

    val context = MyApp.getContext()

    override suspend fun doWork(): Result {
        showNotification("Pest Sync", "🔄 Syncing your data...")

        val userId = Globals.savedUsername // Implement this
        val token = Globals.savedToken   // Implement this

        val context = LocalContext.current.applicationContext as Application


        val detectionSaveViewModel: DetectionSaveViewModel = androidx.lifecycle.viewmodel.compose.viewModel(
            factory = DetectionSaveViewModelFactory(
                context,
                DatabaseManager.getDatabase(MyApp.getContext()).detectionResultDao(),
                DatabaseManager.getDatabase(MyApp.getContext()).boundingBoxDao()
            )
        )


        val userViewModelRoom: UserViewModelRoom = androidx.lifecycle.viewmodel.compose.viewModel(
            factory = UserViewModelFactory(
                context,
                DatabaseManager.getDatabase(MyApp.getContext()).userDao(),

                )
        )

        val detectionViewModel: DetectionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()



        return try {
            if (userId != null && token != null) {


                repo.softDeleteLocalDetections(token, userId)
                repo.syncSoftDeletedDetections()
                repo.syncNotes(token, userId)

                showNotification("Pest Sync", "✅ Sync complete!")
                delay(1500) // keep it briefly visible
                cancelNotification()
                Result.success()
            } else {
                cancelNotification()
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            showNotification("Pest Sync", "❌ Sync failed")
            delay(2000)
            cancelNotification()
            Result.retry()
        }
    }


    private val NOTIFICATION_ID = 1001
    private val CHANNEL_ID = "pest_sync_channel"

    private fun showNotification(title: String, content: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pest Sync Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_sync) // Add your icon
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun cancelNotification() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }


}


fun enqueueSyncWorker() {
    val workRequest = OneTimeWorkRequestBuilder<SyncWorker>()
        .setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        )
        .build()

    WorkManager.getInstance(MyApp.getContext()).enqueueUniqueWork(
        "pest_sync_job",
        ExistingWorkPolicy.REPLACE, // replace if already queued
        workRequest
    )
}


 */