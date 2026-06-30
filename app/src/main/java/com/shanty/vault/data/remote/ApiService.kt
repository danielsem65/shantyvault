package com.shanty.vault.data.remote

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/verify-email")
    suspend fun verifyEmail(@Body request: VerifyEmailRequest): Response<Unit>

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): Response<Unit>

    @POST("auth/change-password")
    suspend fun changePassword(@Body request: ChangePasswordRequest): Response<Unit>

    @POST("auth/mfa/setup")
    suspend fun setupMfa(): Response<MfaSetupResponse>

    @POST("auth/mfa/verify")
    suspend fun verifyMfa(@Body request: VerifyMfaRequest): Response<AuthResponse>

    @POST("auth/mfa/disable")
    suspend fun disableMfa(@Body request: VerifyMfaRequest): Response<Unit>

    @GET("storage/usage")
    suspend fun getStorageUsage(): Response<StorageUsageResponse>

    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Part file: MultipartBody.Part,
        @Part("folderId") folderId: RequestBody?,
        @Part("encryptionIv") encryptionIv: RequestBody
    ): Response<FileUploadResponse>

    @GET("files/download/{fileId}")
    @Streaming
    suspend fun downloadFile(@Path("fileId") fileId: String): Response<ResponseBody>

    @GET("files")
    suspend fun getFiles(
        @Query("folderId") folderId: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 50
    ): Response<FileListResponse>

    @DELETE("files/{fileId}")
    suspend fun deleteFile(@Path("fileId") fileId: String): Response<Unit>

    @PUT("files/{fileId}/rename")
    suspend fun renameFile(
        @Path("fileId") fileId: String,
        @Body request: RenameRequest
    ): Response<Unit>

    @PUT("files/{fileId}/move")
    suspend fun moveFile(
        @Path("fileId") fileId: String,
        @Body request: MoveRequest
    ): Response<Unit>

    @POST("folders")
    suspend fun createFolder(@Body request: CreateFolderRequest): Response<FolderResponse>

    @PUT("folders/{folderId}")
    suspend fun updateFolder(
        @Path("folderId") folderId: String,
        @Body request: UpdateFolderRequest
    ): Response<Unit>

    @DELETE("folders/{folderId}")
    suspend fun deleteFolder(@Path("folderId") folderId: String): Response<Unit>

    @GET("folders")
    suspend fun getFolders(
        @Query("parentId") parentId: String? = null
    ): Response<FolderListResponse>

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String? = null,
        @Query("page") page: Int = 1
    ): Response<SearchResponse>

    @POST("notes")
    suspend fun createNote(@Body request: CreateNoteRequest): Response<NoteResponse>

    @PUT("notes/{noteId}")
    suspend fun updateNote(
        @Path("noteId") noteId: String,
        @Body request: UpdateNoteRequest
    ): Response<Unit>

    @DELETE("notes/{noteId}")
    suspend fun deleteNote(@Path("noteId") noteId: String): Response<Unit>

    @GET("notes")
    suspend fun getNotes(): Response<NoteListResponse>
}

data class LoginRequest(val email: String, val password: String)
data class RegisterRequest(val email: String, val password: String, val name: String)
data class RefreshTokenRequest(val refreshToken: String)
data class VerifyEmailRequest(val code: String)
data class ResetPasswordRequest(val email: String)
data class ChangePasswordRequest(val currentPassword: String, val newPassword: String)
data class VerifyMfaRequest(val code: String)
data class MfaSetupResponse(val secret: String, val qrCodeUrl: String)
data class AuthResponse(val userId: String, val accessToken: String, val refreshToken: String, val expiresIn: Long)
data class StorageUsageResponse(val used: Long, val limit: Long, val percentUsed: Double)

data class FileUploadResponse(val fileId: String, val remotePath: String, val checksum: String)
data class FileListResponse(val files: List<FileDto>, val totalCount: Int, val page: Int, val hasMore: Boolean)
data class FileDto(
    val id: String, val name: String, val extension: String, val mimeType: String,
    val size: Long, val folderId: String?, val remotePath: String, val thumbnailPath: String?,
    val isFavorite: Boolean, val createdAt: Long, val updatedAt: Long, val checksum: String?
)

data class RenameRequest(val name: String)
data class MoveRequest(val folderId: String?)

data class CreateFolderRequest(val name: String, val parentId: String?)
data class UpdateFolderRequest(val name: String)
data class FolderResponse(val id: String, val name: String, val parentId: String?, val path: String)
data class FolderListResponse(val folders: List<FolderDto>)
data class FolderDto(
    val id: String, val name: String, val parentId: String?,
    val path: String, val itemCount: Int, val createdAt: Long
)

data class SearchResponse(val results: List<SearchResultDto>, val totalCount: Int)
data class SearchResultDto(
    val id: String, val type: String, val name: String,
    val description: String?, val thumbnailUrl: String?
)

data class CreateNoteRequest(val title: String, val content: String, val isEncrypted: Boolean)
data class UpdateNoteRequest(val title: String?, val content: String?, val isPinned: Boolean?)
data class NoteResponse(val id: String, val title: String, val createdAt: Long)
data class NoteListResponse(val notes: List<NoteDto>)
data class NoteDto(val id: String, val title: String, val content: String, val isPinned: Boolean, val createdAt: Long, val updatedAt: Long)
