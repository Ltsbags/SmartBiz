package com.example.features.usermanagement.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.UserEntity
import com.example.repositories.UserRepository
import com.example.services.ImageService
import com.example.services.SessionHistoryService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val user: UserEntity? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    // Edit Form State
    val fullName: String = "",
    val displayName: String = "",
    val businessName: String = "",
    val designation: String = "",
    val mobileNumber: String = "",
    val alternateNumber: String = "",
    val email: String = "",
    val dob: String = "",
    val gender: String = "",
    val languagePreference: String = "en",
    val timeZone: String = "UTC",
    val profileImage: String = ""
)

class ProfileViewModel(
    private val userRepository: UserRepository,
    private val imageService: ImageService,
    private val sessionHistoryService: SessionHistoryService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            userRepository.getPrimaryUserFlow().collect { user ->
                if (user != null) {
                    _uiState.update { state ->
                        state.copy(
                            user = user,
                            isLoading = false,
                            fullName = user.fullName,
                            displayName = user.displayName,
                            businessName = user.businessName,
                            designation = user.designation,
                            mobileNumber = user.mobileNumber,
                            alternateNumber = user.alternateNumber,
                            email = user.email,
                            dob = user.dob,
                            gender = user.gender,
                            languagePreference = user.languagePreference,
                            timeZone = user.timeZone,
                            profileImage = user.profileImage
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    fun updateField(
        fullName: String? = null,
        displayName: String? = null,
        businessName: String? = null,
        designation: String? = null,
        mobileNumber: String? = null,
        alternateNumber: String? = null,
        email: String? = null,
        dob: String? = null,
        gender: String? = null,
        languagePreference: String? = null,
        timeZone: String? = null
    ) {
        _uiState.update { state ->
            state.copy(
                fullName = fullName ?: state.fullName,
                displayName = displayName ?: state.displayName,
                businessName = businessName ?: state.businessName,
                designation = designation ?: state.designation,
                mobileNumber = mobileNumber ?: state.mobileNumber,
                alternateNumber = alternateNumber ?: state.alternateNumber,
                email = email ?: state.email,
                dob = dob ?: state.dob,
                gender = gender ?: state.gender,
                languagePreference = languagePreference ?: state.languagePreference,
                timeZone = timeZone ?: state.timeZone,
                errorMessage = null,
                successMessage = null
            )
        }
    }

    fun saveProfile() {
        val state = _uiState.value
        val user = state.user ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, errorMessage = null) }

            val (success, message) = userRepository.updateUserProfile(
                userId = user.userId,
                fullName = state.fullName,
                displayName = state.displayName,
                businessName = state.businessName,
                designation = state.designation,
                mobileNumber = state.mobileNumber,
                alternateNumber = state.alternateNumber,
                email = state.email,
                dob = state.dob,
                gender = state.gender,
                languagePreference = state.languagePreference,
                timeZone = state.timeZone
            )

            if (success) {
                sessionHistoryService.recordAuditLog(
                    userId = user.userId,
                    action = "PROFILE_UPDATED",
                    details = "User profile information updated"
                )
                _uiState.update {
                    it.copy(isSaving = false, successMessage = message)
                }
            } else {
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = message)
                }
            }
        }
    }

    fun updateProfilePhoto(uri: Uri) {
        val user = _uiState.value.user ?: return
        viewModelScope.launch {
            val savedPath = imageService.saveProfileImageFromUri(uri, user.userId)
            if (savedPath != null) {
                userRepository.updateProfileImage(user.userId, savedPath)
                _uiState.update {
                    it.copy(profileImage = savedPath, successMessage = "Profile picture updated")
                }
                sessionHistoryService.recordAuditLog(
                    userId = user.userId,
                    action = "PROFILE_UPDATED",
                    details = "Profile picture updated"
                )
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Failed to save selected photo")
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    class Factory(
        private val userRepository: UserRepository,
        private val imageService: ImageService,
        private val sessionHistoryService: SessionHistoryService
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfileViewModel(userRepository, imageService, sessionHistoryService) as T
        }
    }
}
