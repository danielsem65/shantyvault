package com.shanty.vault.presentation.navigation

object NavRoutes {
    const val AUTH = "auth"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot_password"
    const val VERIFY_EMAIL = "verify_email"
    const val BIOMETRIC_AUTH = "biometric_auth"

    const val MAIN = "main"
    const val DASHBOARD = "dashboard"
    const val FILES = "files"
    const val FILES_LIST = "files_list/{folderId}"
    const val FOLDER = "folder/{folderId}"
    const val NOTES = "notes"
    const val NOTE_DETAIL = "note_detail/{noteId}"
    const val SEARCH = "search"
    const val SETTINGS = "settings"
    const val MEDIA_VIEWER = "media_viewer/{fileId}"
    const val PDF_VIEWER = "pdf_viewer/{fileId}"

    fun filesList(folderId: String?) = if (folderId != null) "files_list/$folderId" else "files_list/null"
    fun folderDetail(folderId: String) = "folder/$folderId"
    fun noteDetail(noteId: String) = "note_detail/$noteId"
    fun mediaViewer(fileId: String) = "media_viewer/$fileId"
    fun pdfViewer(fileId: String) = "pdf_viewer/$fileId"
}
