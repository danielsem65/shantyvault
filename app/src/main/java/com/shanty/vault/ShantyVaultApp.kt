package com.shanty.vault

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkerFactory
import com.shanty.vault.data.worker.DownloadWorker
import com.shanty.vault.data.worker.UploadWorker
import com.shanty.vault.di.AppContainer
import com.shanty.vault.di.ViewModelFactory

class ShantyVaultApp : Application(), Configuration.Provider {
    lateinit var container: AppContainer
        private set
    lateinit var viewModelFactory: ViewModelFactory
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        viewModelFactory = ViewModelFactory(container)
        createNotificationChannels()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()

    private val workerFactory: WorkerFactory = object : WorkerFactory() {
        override fun createWorker(
            appContext: android.content.Context,
            workerClassName: String,
            workerParameters: androidx.work.WorkerParameters
        ): androidx.work.ListenableWorker? {
            val repo = container.vaultRepository
            return when (workerClassName) {
                UploadWorker::class.java.name -> UploadWorker(appContext, workerParameters, repo)
                DownloadWorker::class.java.name -> DownloadWorker(appContext, workerParameters, repo)
                else -> null
            }
        }
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(
                NotificationChannel("upload_progress", "Upload Progress", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "File upload progress notifications"
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel("download_progress", "Download Progress", NotificationManager.IMPORTANCE_LOW).apply {
                    description = "File download progress notifications"
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel("shanty_vault_notifications", "Shanty Vault", NotificationManager.IMPORTANCE_DEFAULT).apply {
                    description = "General notifications"
                }
            )
            notificationManager.createNotificationChannel(
                NotificationChannel("security_alerts", "Security Alerts", NotificationManager.IMPORTANCE_HIGH).apply {
                    description = "Security related notifications"
                }
            )
        }
    }
}
