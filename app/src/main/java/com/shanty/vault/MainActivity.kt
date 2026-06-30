package com.shanty.vault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.shanty.vault.data.local.UserPreferences
import com.shanty.vault.presentation.auth.AuthViewModel
import com.shanty.vault.presentation.navigation.AppNavHost
import com.shanty.vault.presentation.navigation.NavRoutes
import com.shanty.vault.presentation.theme.ShantyVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val isLoggedIn = runBlocking {
            userPreferences.userId.first() != null
        }

        val biometricEnabled = runBlocking {
            userPreferences.isBiometricEnabled.first()
        }

        val startDestination = when {
            isLoggedIn && biometricEnabled -> NavRoutes.BIOMETRIC_AUTH
            isLoggedIn -> NavRoutes.MAIN
            else -> NavRoutes.LOGIN
        }

        setContent {
            ShantyVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = hiltViewModel()

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
