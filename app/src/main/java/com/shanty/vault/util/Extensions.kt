package com.shanty.vault.util

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.compose.ui.graphics.Color
import java.io.File
import java.io.InputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.log10
import kotlin.math.pow

fun Long.toFormattedFileSize(): String {
    if (this <= 0) return "0 B"
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    val digitGroups = (log10(this.toDouble()) / log10(1024.0)).toInt()
    val size = this / 1024.0.pow(digitGroups.toDouble())
    return "%.1f %s".format(size, units[digitGroups])
}

fun Long.toFormattedDate(pattern: String = "MMM dd, yyyy HH:mm"): String {
    val sdf = SimpleDateFormat(pattern, Locale.getDefault())
    return sdf.format(Date(this))
}

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        else -> toFormattedDate("MMM dd")
    }
}

fun File.extension(): String = name.substringAfterLast('.', "").lowercase()

fun String.isValidEmail(): Boolean =
    android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isStrongPassword(): Boolean {
    if (length < Constants.PASSWORD_MIN_LENGTH) return false
    if (Constants.PASSWORD_REQUIRE_UPPERCASE && !any { it.isUpperCase() }) return false
    if (Constants.PASSWORD_REQUIRE_LOWERCASE && !any { it.isLowerCase() }) return false
    if (Constants.PASSWORD_REQUIRE_DIGIT && !any { it.isDigit() }) return false
    if (Constants.PASSWORD_REQUIRE_SPECIAL && !any { !it.isLetterOrDigit() }) return false
    return true
}

fun String.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    return digest.digest(toByteArray()).joinToString("") { "%02x".format(it) }
}

fun Bitmap.toCircularBitmap(): Bitmap {
    val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(output)
    val paint = android.graphics.Paint().apply {
        isAntiAlias = true
        shader = android.graphics.BitmapShader(this@toCircularBitmap, android.graphics.Shader.TileMode.CLAMP, android.graphics.Shader.TileMode.CLAMP)
    }
    canvas.drawCircle(width / 2f, height / 2f, width / 2f, paint)
    return output
}

fun Uri.getFileName(contentResolver: ContentResolver): String {
    var name = "unknown"
    val cursor = contentResolver.query(this, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) name = it.getString(nameIndex)
        }
    }
    return name
}

fun Uri.getFileSize(contentResolver: ContentResolver): Long {
    var size = 0L
    val cursor = contentResolver.query(this, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)
            if (sizeIndex >= 0) size = it.getLong(sizeIndex)
        }
    }
    return size
}

fun InputStream.toByteArray(): ByteArray = use { it.readBytes() }

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun Color.toArgbHex(): String {
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    val alpha = (this.alpha * 255).toInt()
    return "#%02X%02X%02X%02X".format(alpha, red, green, blue)
}
