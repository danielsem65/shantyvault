package com.shanty.vault.presentation.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.shanty.vault.presentation.dashboard.DashboardScreen
import com.shanty.vault.presentation.files.FilesScreen
import com.shanty.vault.presentation.notes.NotesScreen
import com.shanty.vault.presentation.settings.SettingsScreen
import com.shanty.vault.presentation.dashboard.DashboardViewModel
import com.shanty.vault.presentation.files.FilesViewModel
import com.shanty.vault.presentation.notes.NotesViewModel

data class BottomNavItem(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem("dashboard", "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItem("files", "Files", Icons.Filled.Folder, Icons.Outlined.Folder),
    BottomNavItem("notes", "Notes", Icons.Filled.Description, Icons.Outlined.Description),
    BottomNavItem("settings", "Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavHostController,
    dashboardViewModel: DashboardViewModel = hiltViewModel(),
    filesViewModel: FilesViewModel = hiltViewModel(),
    notesViewModel: NotesViewModel = hiltViewModel()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            NavigationBar {
                bottomNavItems.forEach { item ->
                    val selected = currentRoute == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        label = { Text(item.title) },
                        selected = selected,
                        onClick = {
                            if (currentRoute != item.route) {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") {
                DashboardScreen(
                    viewModel = dashboardViewModel,
                    onNavigateToSearch = { navController.navigate(NavRoutes.SEARCH) },
                    onNavigateToFiles = { navController.navigate("files") },
                    onNavigateToFile = { fileId ->
                        navController.navigate(NavRoutes.mediaViewer(fileId))
                    }
                )
            }
            composable("files") {
                FilesScreen(
                    viewModel = filesViewModel,
                    onNavigateToFolder = { folderId ->
                        navController.navigate(NavRoutes.folderDetail(folderId))
                    },
                    onNavigateToFile = { fileId ->
                        navController.navigate(NavRoutes.mediaViewer(fileId))
                    },
                    onNavigateToSearch = { navController.navigate(NavRoutes.SEARCH) }
                )
            }
            composable("notes") {
                NotesScreen(
                    viewModel = notesViewModel,
                    onNavigateToNote = { noteId ->
                        navController.navigate(NavRoutes.noteDetail(noteId))
                    }
                )
            }
            composable("settings") {
                SettingsScreen(
                    onNavigateBack = { },
                    onLogout = {
                        navController.navigate(NavRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
