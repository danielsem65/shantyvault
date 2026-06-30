package com.shanty.vault.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    var themeMode by remember { mutableStateOf("system") }
    var biometricsEnabled by remember { mutableStateOf(false) }
    var mfaEnabled by remember { mutableStateOf(false) }
    var notificationsEnabled by remember { mutableStateOf(true) }

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
                    selectedTheme = themeMode,
                    onThemeSelected = { themeMode = it }
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
                    onClick = { }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Fingerprint, null) },
                    title = "Enable Biometrics",
                    trailing = {
                        Switch(
                            checked = biometricsEnabled,
                            onCheckedChange = { biometricsEnabled = it }
                        )
                    },
                    onClick = { biometricsEnabled = !biometricsEnabled }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Security, null) },
                    title = "Enable MFA",
                    trailing = {
                        Switch(
                            checked = mfaEnabled,
                            onCheckedChange = { mfaEnabled = it }
                        )
                    },
                    onClick = { mfaEnabled = !mfaEnabled }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Devices, null) },
                    title = "Manage Sessions",
                    subtitle = "2 active sessions",
                    onClick = { }
                )
            }

            item { HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp)) }

            item {
                SectionHeader("PRIVACY")
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Storage, null) },
                    title = "Storage Usage",
                    subtitle = "2.4 GB / 15 GB used",
                    onClick = { }
                )
            }
            item {
                SettingsRow(
                    icon = { Icon(Icons.Outlined.Notifications, null) },
                    title = "Notification Preferences",
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { notificationsEnabled = it }
                        )
                    },
                    onClick = { notificationsEnabled = !notificationsEnabled }
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
                    title = "About",
                    onClick = { }
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
