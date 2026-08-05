package com.example.features.usermanagement.screens

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.features.usermanagement.components.AvatarWidget
import com.example.features.usermanagement.components.EditableProfileField
import com.example.features.usermanagement.components.SettingsGroup
import com.example.features.usermanagement.viewmodel.ProfileViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: ProfileViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit User Profile") },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("edit_profile_back")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.testTag("edit_profile_screen")
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AvatarWidget(
                    imagePath = uiState.profileImage,
                    fullName = uiState.fullName,
                    size = 110.dp,
                    isEditable = true,
                    onImageSelected = { uri ->
                        viewModel.updateProfilePhoto(uri)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsGroup(title = "General Details") {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditableProfileField(
                        label = "Full Name",
                        value = uiState.fullName,
                        onValueChange = { viewModel.updateField(fullName = it) },
                        icon = Icons.Default.Person,
                        testTag = "input_full_name"
                    )

                    EditableProfileField(
                        label = "Display Name",
                        value = uiState.displayName,
                        onValueChange = { viewModel.updateField(displayName = it) },
                        icon = Icons.Default.Badge,
                        testTag = "input_display_name",
                        placeholder = "e.g. Alex"
                    )

                    EditableProfileField(
                        label = "Business Name",
                        value = uiState.businessName,
                        onValueChange = { viewModel.updateField(businessName = it) },
                        icon = Icons.Default.Business,
                        testTag = "input_business_name"
                    )

                    EditableProfileField(
                        label = "Designation / Role",
                        value = uiState.designation,
                        onValueChange = { viewModel.updateField(designation = it) },
                        icon = Icons.Default.Badge,
                        testTag = "input_designation",
                        placeholder = "e.g. Owner / General Manager"
                    )
                }
            }

            SettingsGroup(title = "Contact Information") {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditableProfileField(
                        label = "Mobile Number",
                        value = uiState.mobileNumber,
                        onValueChange = { viewModel.updateField(mobileNumber = it) },
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        testTag = "input_mobile_number"
                    )

                    EditableProfileField(
                        label = "Alternate Mobile Number",
                        value = uiState.alternateNumber,
                        onValueChange = { viewModel.updateField(alternateNumber = it) },
                        icon = Icons.Default.Phone,
                        keyboardType = KeyboardType.Phone,
                        testTag = "input_alt_mobile"
                    )

                    EditableProfileField(
                        label = "Email Address",
                        value = uiState.email,
                        onValueChange = { viewModel.updateField(email = it) },
                        icon = Icons.Default.Email,
                        keyboardType = KeyboardType.Email,
                        testTag = "input_email"
                    )
                }
            }

            SettingsGroup(title = "Personal & Localization Settings") {
                Column(modifier = Modifier.padding(16.dp)) {
                    EditableProfileField(
                        label = "Date of Birth (YYYY-MM-DD)",
                        value = uiState.dob,
                        onValueChange = { viewModel.updateField(dob = it) },
                        icon = Icons.Default.CalendarMonth,
                        testTag = "input_dob",
                        placeholder = "1990-01-15"
                    )

                    EditableProfileField(
                        label = "Language Preference",
                        value = uiState.languagePreference,
                        onValueChange = { viewModel.updateField(languagePreference = it) },
                        icon = Icons.Default.Language,
                        testTag = "input_language"
                    )

                    EditableProfileField(
                        label = "Time Zone",
                        value = uiState.timeZone,
                        onValueChange = { viewModel.updateField(timeZone = it) },
                        icon = Icons.Default.Public,
                        testTag = "input_timezone"
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = { viewModel.saveProfile() },
                enabled = !uiState.isSaving,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_profile_button")
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                } else {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.padding(end = 8.dp))
                }
                Text(text = "Save Profile Changes")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
