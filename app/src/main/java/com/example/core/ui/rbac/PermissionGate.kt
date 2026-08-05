package com.example.core.ui.rbac

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.repositories.AppRepositoryProvider
import com.example.repositories.AuthRepository
import com.example.services.AuthorizationService

@Composable
fun PermissionGate(
    permissionCode: String,
    modifier: Modifier = Modifier,
    hideOnUnauthorized: Boolean = false,
    unauthorizedContent: @Composable () -> Unit = { AccessDeniedCard(permissionCode = permissionCode) },
    content: @Composable () -> Unit
) {
    val provider = AppRepositoryProvider.getInstance()
    val authService: AuthorizationService = remember { provider.authorizationService }
    val authRepo: AuthRepository = remember { provider.authRepository }

    var isAuthorized by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(permissionCode) {
        val currentUser = authRepo.getPrimaryUser()
        if (currentUser != null) {
            isAuthorized = authService.hasPermission(currentUser.userId, permissionCode)
        } else {
            isAuthorized = false
        }
    }

    when (isAuthorized) {
        true -> {
            content()
        }
        false -> {
            if (!hideOnUnauthorized) {
                unauthorizedContent()
            }
        }
        null -> {
            Box(modifier = modifier, contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
    }
}
