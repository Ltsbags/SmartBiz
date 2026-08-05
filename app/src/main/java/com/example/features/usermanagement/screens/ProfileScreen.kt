package com.example.features.usermanagement.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.features.usermanagement.components.ProfileHeader
import com.example.features.usermanagement.components.SecurityTile
import com.example.features.usermanagement.components.SettingsGroup
import com.example.features.usermanagement.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit,
    onNavigateEditProfile: () -> Unit,
    onNavigateSecurity: () -> Unit,
    onNavigateDevices: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateSessions: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Profile & Account") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("profile_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateEditProfile,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("edit_profile_fab")
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Profile")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.testTag("profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            val user = uiState.user

            ProfileHeader(
                fullName = user?.fullName ?: "Business User",
                displayName = user?.displayName ?: "",
                businessName = user?.businessName ?: "Commercial Store",
                designation = user?.designation ?: "Business Owner",
                profileImage = user?.profileImage ?: "",
                status = user?.status ?: "ACTIVE",
                onEditClick = onNavigateEditProfile
            )

            SettingsGroup(title = "Contact & Personal Information") {
                SecurityTile(
                    title = "Mobile Number",
                    subtitle = user?.mobileNumber?.ifBlank { "Not configured" } ?: "",
                    icon = Icons.Default.Phone,
                    testTag = "tile_mobile"
                )
                if (user?.alternateNumber?.isNotBlank() == true) {
                    SecurityTile(
                        title = "Alternate Mobile",
                        subtitle = user.alternateNumber,
                        icon = Icons.Default.Phone,
                        testTag = "tile_alt_mobile"
                    )
                }
                SecurityTile(
                    title = "Email Address",
                    subtitle = user?.email?.ifBlank { "Not configured" } ?: "",
                    icon = Icons.Default.Email,
                    testTag = "tile_email"
                )
                SecurityTile(
                    title = "Role & ID",
                    subtitle = "User ID: ${user?.userId ?: ""} • Role: ${user?.roleId ?: "ROLE_OWNER"}",
                    icon = Icons.Default.Badge,
                    testTag = "tile_role"
                )
            }

            SettingsGroup(title = "Localization Preferences") {
                SecurityTile(
                    title = "Language Preference",
                    subtitle = if (user?.languagePreference == "en") "English (US)" else user?.languagePreference ?: "English",
                    icon = Icons.Default.Language,
                    testTag = "tile_language"
                )
                SecurityTile(
                    title = "Time Zone",
                    subtitle = user?.timeZone?.ifBlank { "UTC (Coordinated Universal Time)" } ?: "UTC",
                    icon = Icons.Default.Public,
                    testTag = "tile_timezone"
                )
            }

            SettingsGroup(title = "Account Security & Management") {
                SecurityTile(
                    title = "Account Security & PIN",
                    subtitle = "Change PIN, Biometric login & device security",
                    icon = Icons.Default.Security,
                    testTag = "tile_security_settings",
                    onClick = onNavigateSecurity
                )
                SecurityTile(
                    title = "Device Management",
                    subtitle = "Manage trusted devices and active connections",
                    icon = Icons.Default.Smartphone,
                    testTag = "tile_device_management",
                    onClick = onNavigateDevices
                )
                SecurityTile(
                    title = "Active Sessions",
                    subtitle = "View current active login sessions",
                    icon = Icons.Default.LockClock,
                    testTag = "tile_active_sessions",
                    onClick = onNavigateSessions
                )
                SecurityTile(
                    title = "Login History & Audit Logs",
                    subtitle = "View complete security log of logins and PIN updates",
                    icon = Icons.Default.History,
                    testTag = "tile_login_history",
                    onClick = onNavigateHistory
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
