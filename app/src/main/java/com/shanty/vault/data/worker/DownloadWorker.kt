package com.shanty.vault.data.worker

import android.content.Context
import androidx.work.*
import com.shanty.vault.domain.repository.VaultRepository
import java.io.File
import java.util.concurrent.TimeUnit

class DownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val vaultRepository: VaultRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val fileId = inputData.getString(KEY_FILE_ID) ?: return Result.failure()
        val destinationPath = inputData.getString(KEY_DESTINATION_PATH)

        return try {
            val destination = if (destinationPath != null) File(destinationPath)
            else File(applicationContext.cacheDir, "download_$fileId")

            val result = vaultRepository.downloadFile(fileId, destination)
            if (result.isSuccess) Result.success() else {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_FILE_ID = "file_id"
        const val KEY_DESTINATION_PATH = "destination_path"
        const val WORK_NAME = "download_work"

        fun createRequest(fileId: String, destinationPath: String? = null): OneTimeWorkRequest {
            val inputData = workDataOf(
                KEY_FILE_ID to fileId,
                KEY_DESTINATION_PATH to destinationPath
            )
            return OneTimeWorkRequestBuilder<DownloadWorker>()
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    30,
                    TimeUnit.SECONDS
                )
                .addTag(WORK_NAME)
                .build()
        }
    }
}
