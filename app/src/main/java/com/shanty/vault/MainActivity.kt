package com.shanty.vault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.shanty.vault.presentation.auth.AuthViewModel
import com.shanty.vault.presentation.navigation.AppNavHost
import com.shanty.vault.presentation.navigation.NavRoutes
import com.shanty.vault.presentation.theme.ShantyVaultTheme
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as ShantyVaultApp
        val prefs = app.container.userPreferences

        val isLoggedIn = runBlocking { prefs.userId.first() != null }
        val biometricEnabled = runBlocking { prefs.isBiometricEnabled.first() }

        val startDestination = when {
            isLoggedIn && biometricEnabled -> NavRoutes.BIOMETRIC_AUTH
            isLoggedIn -> NavRoutes.MAIN
            else -> NavRoutes.LOGIN
        }

        setContent {
            ShantyVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel(factory = app.viewModelFactory)

                    val authState by authViewModel.authState.collectAsState()

                    LaunchedEffect(authState) {
                        if (authState is com.shanty.vault.presentation.auth.AuthState.Unauthenticated) {
                            navController.navigate(NavRoutes.LOGIN) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    }

                    AppNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}
