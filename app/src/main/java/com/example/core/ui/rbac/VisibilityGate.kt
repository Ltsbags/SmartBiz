package com.example.core.ui.rbac

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.repositories.AppRepositoryProvider
import com.example.repositories.AuthRepository
import com.example.services.AuthorizationService

@Composable
fun VisibilityGate(
    permissionCode: String,
    content: @Composable () -> Unit
) {
    val provider = AppRepositoryProvider.getInstance()
    val authService: AuthorizationService = remember { provider.authorizationService }
    val authRepo: AuthRepository = remember { provider.authRepository }

    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(permissionCode) {
        val currentUser = authRepo.getPrimaryUser()
        if (currentUser != null) {
            isVisible = authService.hasPermission(currentUser.userId, permissionCode)
        } else {
            isVisible = false
        }
    }

    if (isVisible) {
        content()
    }
}
