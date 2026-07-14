package com.shanty.vault.util

object Constants {
    const val APP_NAME = "Shanty Vault"
    const val BASE_URL = "https://api.shantyvault.com/"

    // Supabase configuration - Replace with your Supabase project values
    const val SUPABASE_URL = "https://YOUR_PROJECT_ID.supabase.co"
    const val SUPABASE_ANON_KEY = "YOUR_SUPABASE_ANON_KEY"
    const val SUPABASE_STORAGE_BUCKET = "vault-files"

    const val DB_NAME = "shanty_vault.db"
    const val DATASTORE_NAME = "shanty_vault_prefs"

    const val KEY_USER_ID = "user_id"
    const val KEY_ACCESS_TOKEN = "access_token"
    const val KEY_REFRESH_TOKEN = "refresh_token"
    const val KEY_SESSION_TIMEOUT = "session_timeout"
    const val KEY_LAST_ACTIVITY = "last_activity"
    const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
    const val KEY_MFA_ENABLED = "mfa_enabled"
    const val KEY_THEME_MODE = "theme_mode"
    const val KEY_FIRST_LOGIN = "first_login"
    const val KEY_TRUSTED_DEVICE = "trusted_device"

    const val SESSION_TIMEOUT_MS = 300_000L
    const val LOCKOUT_THRESHOLD = 5
    const val LOCKOUT_DURATION_MS = 300_000L
    const val RATE_LIMIT_WINDOW_MS = 60_000L
    const val MAX_LOGIN_ATTEMPTS = 5
    const val MAX_UPLOAD_RETRIES = 3
    const val CHUNK_SIZE = 1024 * 1024

    const val STORAGE_WARNING_PERCENT = 90
    const val STORAGE_CRITICAL_PERCENT = 95

    val SUPPORTED_IMAGE_TYPES = listOf("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg")
    val SUPPORTED_VIDEO_TYPES = listOf("mp4", "avi", "mkv", "mov", "wmv", "flv")
    val SUPPORTED_DOCUMENT_TYPES = listOf("pdf", "doc", "docx", "xls", "xlsx", "txt", "rtf")
    val SUPPORTED_AUDIO_TYPES = listOf("mp3", "wav", "aac", "flac", "ogg", "wma")
    val SUPPORTED_ARCHIVE_TYPES = listOf("zip", "rar", "tar", "gz", "7z")

    const val NOTE_MAX_LENGTH = 50000
    const val PASSWORD_MIN_LENGTH = 12
    const val PASSWORD_REQUIRE_UPPERCASE = true
    const val PASSWORD_REQUIRE_LOWERCASE = true
    const val PASSWORD_REQUIRE_DIGIT = true
    const val PASSWORD_REQUIRE_SPECIAL = true
    const val SEARCH_DEBOUNCE_MS = 300L
}
