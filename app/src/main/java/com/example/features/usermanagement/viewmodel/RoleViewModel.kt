package com.example.features.usermanagement.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entity.PermissionEntity
import com.example.core.database.entity.RoleEntity
import com.example.core.database.model.RoleWithPermissions
import com.example.repositories.AppRepositoryProvider
import com.example.repositories.RbacRepository
import com.example.repositories.UserRepository
import com.example.services.AuthorizationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class RolesUiState(
    val roles: List<RoleWithPermissions> = emptyList(),
    val permissions: List<PermissionEntity> = emptyList(),
    val permissionsByCategory: Map<String, List<PermissionEntity>> = emptyMap(),
    val selectedRole: RoleWithPermissions? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class RoleViewModel(
    private val rbacRepository: RbacRepository = AppRepositoryProvider.getInstance().rbacRepository,
    private val userRepository: UserRepository = AppRepositoryProvider.getInstance().userRepository,
    private val authorizationService: AuthorizationService = AppRepositoryProvider.getInstance().authorizationService
) : ViewModel() {

    private val _uiState = MutableStateFlow(RolesUiState())
    val uiState: StateFlow<RolesUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            launch {
                rbacRepository.getAllRolesWithPermissions().collectLatest { rolesList ->
                    _uiState.value = _uiState.value.copy(roles = rolesList)
                }
            }

            launch {
                rbacRepository.getAllPermissions().collectLatest { permList ->
                    val grouped = permList.groupBy { it.category }
                    _uiState.value = _uiState.value.copy(
                        permissions = permList,
                        permissionsByCategory = grouped,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun selectRole(roleId: String) {
        viewModelScope.launch {
            val roleWithPerms = rbacRepository.getRoleWithPermissions(roleId)
            _uiState.value = _uiState.value.copy(selectedRole = roleWithPerms)
        }
    }

    fun createCustomRole(
        roleName: String,
        roleCode: String,
        description: String,
        permissionIds: List<String>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                if (roleName.isBlank() || roleCode.isBlank()) {
                    _uiState.value = _uiState.value.copy(errorMessage = "Role name and code are required")
                    return@launch
                }
                rbacRepository.createCustomRole(
                    roleName = roleName,
                    roleCode = roleCode,
                    description = description,
                    permissionIds = permissionIds
                )
                _uiState.value = _uiState.value.copy(successMessage = "Custom role '$roleName' created successfully")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to create role")
            }
        }
    }

    fun updateRolePermissions(roleId: String, permissionIds: List<String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                rbacRepository.updateRolePermissions(roleId, permissionIds)
                selectRole(roleId)
                _uiState.value = _uiState.value.copy(successMessage = "Role permissions updated successfully")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to update permissions")
            }
        }
    }

    fun deleteCustomRole(roleId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                rbacRepository.deleteCustomRole(roleId)
                _uiState.value = _uiState.value.copy(
                    selectedRole = null,
                    successMessage = "Role deleted successfully"
                )
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to delete role")
            }
        }
    }

    fun setUserRoles(userId: String, roleIds: List<String>, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                rbacRepository.setUserRoles(userId, roleIds)
                _uiState.value = _uiState.value.copy(successMessage = "User roles updated successfully")
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(errorMessage = e.localizedMessage ?: "Failed to set user roles")
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(errorMessage = null, successMessage = null)
    }
}
