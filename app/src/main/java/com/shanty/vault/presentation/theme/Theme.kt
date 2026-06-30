package com.shanty.vault.presentation.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val VaultPrimary = Color(0xFF6C63FF)
val VaultPrimaryVariant = Color(0xFF5A52D5)
val VaultSecondary = Color(0xFF03DAC6)
val VaultSecondaryVariant = Color(0xFF018786)
val VaultBackground = Color(0xFFF8F9FE)
val VaultSurface = Color(0xFFFFFFFF)
val VaultError = Color(0xFFB00020)
val VaultOnPrimary = Color(0xFFFFFFFF)
val VaultOnBackground = Color(0xFF1C1B1F)
val VaultOnSurface = Color(0xFF1C1B1F)

val VaultDarkPrimary = Color(0xFF8B83FF)
val VaultDarkPrimaryVariant = Color(0xFF6C63FF)
val VaultDarkSecondary = Color(0xFF03DAC6)
val VaultDarkBackground = Color(0xFF121212)
val VaultDarkSurface = Color(0xFF1E1E1E)
val VaultDarkOnPrimary = Color(0xFF000000)
val VaultDarkOnBackground = Color(0xFFE6E1E5)
val VaultDarkOnSurface = Color(0xFFE6E1E5)

val StorageGradientStart = Color(0xFF6C63FF)
val StorageGradientEnd = Color(0xFF9B8FFF)
val UploadGradientStart = Color(0xFF6C63FF)
val UploadGradientEnd = Color(0xFF48C6EF)

private val DarkColorScheme = darkColorScheme(
    primary = VaultDarkPrimary,
    secondary = VaultDarkSecondary,
    tertiary = Pink80,
    background = VaultDarkBackground,
    surface = VaultDarkSurface,
    onPrimary = VaultDarkOnPrimary,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = VaultDarkOnBackground,
    onSurface = VaultDarkOnSurface,
    error = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary = VaultPrimary,
    secondary = VaultSecondary,
    tertiary = Pink40,
    background = VaultBackground,
    surface = VaultSurface,
    onPrimary = VaultOnPrimary,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = VaultOnBackground,
    onSurface = VaultOnSurface,
    error = VaultError
)

@Composable
fun ShantyVaultTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
