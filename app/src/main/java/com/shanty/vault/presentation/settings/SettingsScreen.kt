package com.shanty.vault.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            item {
                SectionHeader("APPEARANCE")
            }
            item {
                ThemeSelector(
                    selectedTheme = uiState.themeMode,
                    onThemeSelected = { viewModel.setThemeMode(it) }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                SectionHeader("SECURITY")
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Lock, null) },
                    title = "Change Password",
                    onClick = { viewModel.showChangePasswordDialog() }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Fingerprint, null) },
                    title = "Enable Biometrics",
                    trailing = {
                        Switch(
                            checked = uiState.biometricsEnabled,
                            onCheckedChange = { viewModel.toggleBiometrics() }
                        )
                    },
                    onClick = { viewModel.toggleBiometrics() }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Security, null) },
                    title = "Enable MFA",
                    trailing = {
                        Switch(
                            checked = uiState.mfaEnabled,
                            onCheckedChange = { viewModel.toggleMfa() }
                        )
                    },
                    onClick = { viewModel.toggleMfa() }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                SectionHeader("PRIVACY")
            }
            item {
                val usedGb = uiState.storageUsed / (1024.0 * 1024.0 * 1024.0)
                val limitGb = uiState.storageLimit / (1024.0 * 1024.0 * 1024.0)
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Storage, null) },
                    title = "Storage Usage",
                    subtitle = "%.1f GB / %d GB used".format(usedGb, limitGb.toInt()),
                    onClick = { }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Notifications, null) },
                    title = "Notification Preferences",
                    trailing = {
                        Switch(
                            checked = uiState.notificationsEnabled,
                            onCheckedChange = { viewModel.toggleNotifications() }
                        )
                    },
                    onClick = { viewModel.toggleNotifications() }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                SectionHeader("ABOUT")
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Info, null) },
                    title = "App Version",
                    trailing = { Text("1.0.0", color = MaterialTheme.colorScheme.onSurfaceVariant) }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.ContactSupport, null) },
                    title = "About Shanty Vault",
                    onClick = { viewModel.showAboutDialog() }
                )
            }

            item {
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer,
                        contentColor = MaterialTheme.colorScheme.onErrorContainer
                    )
                ) {
                    Icon(Icons.Filled.Logout, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Logout", fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }

    if (uiState.showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideChangePasswordDialog() },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = uiState.currentPassword,
                        onValueChange = { viewModel.updateCurrentPassword(it) },
                        label = { Text("Current Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.newPassword,
                        onValueChange = { viewModel.updateNewPassword(it) },
                        label = { Text("New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = uiState.confirmPassword,
                        onValueChange = { viewModel.updateConfirmPassword(it) },
                        label = { Text("Confirm New Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.changePassword() }) {
                    Text("Change")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideChangePasswordDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showAboutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAboutDialog() },
            title = { Text("About Shanty Vault") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Shanty Vault",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    HorizontalDivider()
                    Text(
                        "A secure personal cloud vault for private, encrypted cloud storage. Upload, organize, and access your files from anywhere with end-to-end encryption.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    HorizontalDivider()
                    Text(
                        "Features:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text("• AES-256-GCM encrypted storage", style = MaterialTheme.typography.bodySmall)
                    Text("• Biometric authentication", style = MaterialTheme.typography.bodySmall)
                    Text("• Encrypted notes with color coding", style = MaterialTheme.typography.bodySmall)
                    Text("• File organization with folders", style = MaterialTheme.typography.bodySmall)
                    Text("• Built-in media viewer", style = MaterialTheme.typography.bodySmall)
                    Text("• Dark mode support", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.hideAboutDialog() }) {
                    Text("Close")
                }
            }
        )
    }

    uiState.message?.let { msg ->
        Snackbar(
            modifier = Modifier.padding(16.dp),
            action = {
                TextButton(onClick = { viewModel.clearMessage() }) {
                    Text("OK")
                }
            }
        ) {
            Text(msg)
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

@Composable
private fun ThemeSelector(
    selectedTheme: String,
    onThemeSelected: (String) -> Unit
) {
    val themes = listOf("system" to "System", "light" to "Light", "dark" to "Dark")
    val icons = listOf(
        Icons.Outlined.BrightnessAuto,
        Icons.Outlined.LightMode,
        Icons.Outlined.DarkMode
    )

    Column(modifier = Modifier.selectableGroup()) {
        themes.forEachIndexed { index, (value, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = selectedTheme == value,
                        onClick = { onThemeSelected(value) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    icons[index],
                    contentDescription = null,
                    tint = if (selectedTheme == value) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(16.dp))
                Text(
                    label,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                RadioButton(
                    selected = selectedTheme == value,
                    onClick = null
                )
            }
        }
    }
}

@Composable
private fun SettingsRow(
    icon: @Composable () -> Unit,
    title: String,
    subtitle: String? = null,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon()
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                style = MaterialTheme.typography.bodyLarge
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}
