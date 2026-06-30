package com.shanty.vault.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.shanty.vault.presentation.auth.BiometricAuthScreen
import com.shanty.vault.presentation.auth.LoginScreen
import com.shanty.vault.presentation.auth.RegisterScreen
import com.shanty.vault.presentation.auth.AuthViewModel
import com.shanty.vault.presentation.dashboard.DashboardScreen
import com.shanty.vault.presentation.files.FilesScreen
import com.shanty.vault.presentation.folders.FolderDetailScreen
import com.shanty.vault.presentation.notes.NoteDetailScreen
import com.shanty.vault.presentation.notes.NotesScreen
import com.shanty.vault.presentation.search.SearchScreen
import com.shanty.vault.presentation.settings.SettingsScreen
import com.shanty.vault.presentation.viewer.MediaViewerScreen

@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val authState by authViewModel.authState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = startDestination,
        enterTransition = { fadeIn(initialAlpha = 0.3f) + slideInHorizontally(initialOffsetX = { it / 4 }) },
        exitTransition = { fadeOut(targetAlpha = 0.3f) + slideOutHorizontally(targetOffsetX = { -it / 4 }) },
        popEnterTransition = { fadeIn(initialAlpha = 0.3f) + slideInHorizontally(initialOffsetX = { -it / 4 }) },
        popExitTransition = { fadeOut(targetAlpha = 0.3f) + slideOutHorizontally(targetOffsetX = { it / 4 }) }
    ) {
        composable(NavRoutes.LOGIN) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(NavRoutes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(NavRoutes.FORGOT_PASSWORD) },
                onLoginSuccess = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.REGISTER) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(NavRoutes.VERIFY_EMAIL) {
                        popUpTo(NavRoutes.REGISTER) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.FORGOT_PASSWORD) {
            com.shanty.vault.presentation.auth.ForgotPasswordScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.VERIFY_EMAIL) {
            com.shanty.vault.presentation.auth.VerifyEmailScreen(
                onVerified = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.BIOMETRIC_AUTH) {
            BiometricAuthScreen(
                onAuthenticated = {
                    navController.navigate(NavRoutes.MAIN) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onUsePassword = {
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavRoutes.MAIN) {
            MainScreen(navController = navController)
        }

        composable(
            route = NavRoutes.FOLDER,
            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
        ) { entry ->
            val folderId = entry.arguments?.getString("folderId") ?: ""
            FolderDetailScreen(
                folderId = folderId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToFile = { fileId ->
                    navController.navigate(NavRoutes.mediaViewer(fileId))
                }
            )
        }

        composable(
            route = NavRoutes.NOTE_DETAIL,
            arguments = listOf(navArgument("noteId") { type = NavType.StringType })
        ) { entry ->
            val noteId = entry.arguments?.getString("noteId") ?: ""
            NoteDetailScreen(
                noteId = noteId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(NavRoutes.SEARCH) {
            SearchScreen(
                onNavigateToFile = { fileId ->
                    navController.navigate(NavRoutes.mediaViewer(fileId))
                },
                onNavigateToNote = { noteId ->
                    navController.navigate(NavRoutes.noteDetail(noteId))
                },
                onNavigateToFolder = { folderId ->
                    navController.navigate(NavRoutes.folderDetail(folderId))
                }
            )
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onLogout = {
                    authViewModel.logout()
                    navController.navigate(NavRoutes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = NavRoutes.MEDIA_VIEWER,
            arguments = listOf(navArgument("fileId") { type = NavType.StringType })
        ) { entry ->
            val fileId = entry.arguments?.getString("fileId") ?: ""
            MediaViewerScreen(
                fileId = fileId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
