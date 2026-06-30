package com.shanty.vault

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class ShantyVaultApp : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val uploadChannel = NotificationChannel(
                "upload_progress",
                "Upload Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "File upload progress notifications"
            }

            val downloadChannel = NotificationChannel(
                "download_progress",
                "Download Progress",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "File download progress notifications"
            }

            val generalChannel = NotificationChannel(
                "shanty_vault_notifications",
                "Shanty Vault",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "General notifications"
            }

            val securityChannel = NotificationChannel(
                "security_alerts",
                "Security Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Security related notifications"
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(uploadChannel)
            notificationManager.createNotificationChannel(downloadChannel)
            notificationManager.createNotificationChannel(generalChannel)
            notificationManager.createNotificationChannel(securityChannel)
        }
    }
}
