package com.shanty.vault.data.worker

import android.content.Context
import android.net.Uri
import androidx.work.*
import com.shanty.vault.domain.repository.VaultRepository
import java.io.File
import java.util.concurrent.TimeUnit

class UploadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
    private val vaultRepository: VaultRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val filePath = inputData.getString(KEY_FILE_PATH) ?: return Result.failure()
        val folderId = inputData.getString(KEY_FOLDER_ID)
        val fileUri = inputData.getString(KEY_FILE_URI)

        return try {
            val file = if (filePath != null) File(filePath) else if (fileUri != null) {
                val uri = Uri.parse(fileUri)
                val tempFile = File(applicationContext.cacheDir, "upload_temp")
                applicationContext.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output -> input.copyTo(output) }
                }
                tempFile
            } else return Result.failure()

            val result = vaultRepository.uploadFileWithEncryption(file, folderId)
            if (result.isSuccess) {
                file.delete()
                Result.success()
            } else {
                if (runAttemptCount < 3) Result.retry() else Result.failure()
            }
        } catch (e: Exception) {
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val KEY_FILE_PATH = "file_path"
        const val KEY_FILE_URI = "file_uri"
        const val KEY_FOLDER_ID = "folder_id"
        const val WORK_NAME = "upload_work"

        fun createRequest(filePath: String, folderId: String? = null): OneTimeWorkRequest {
            val inputData = workDataOf(
                KEY_FILE_PATH to filePath,
                KEY_FOLDER_ID to folderId
            )
            return OneTimeWorkRequestBuilder<UploadWorker>()
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
