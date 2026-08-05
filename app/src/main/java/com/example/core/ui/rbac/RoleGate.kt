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

@Composable
fun RoleGate(
    allowedRoles: List<String>,
    modifier: Modifier = Modifier,
    hideOnUnauthorized: Boolean = false,
    unauthorizedContent: @Composable () -> Unit = { AccessDeniedCard(permissionCode = "Roles: ${allowedRoles.joinToString()}") },
    content: @Composable () -> Unit
) {
    val rbacRepo = remember { AppRepositoryProvider.getInstance().rbacRepository }
    val authRepo = remember { AppRepositoryProvider.getInstance().authRepository }

    var isAuthorized by remember { mutableStateOf<Boolean?>(null) }

    LaunchedEffect(allowedRoles) {
        val currentUser = authRepo.getPrimaryUser()
        if (currentUser != null) {
            rbacRepo.getRolesForUser(currentUser.userId).collect { roles ->
                val userRoleCodes = roles.map { it.roleCode }.toSet()
                isAuthorized = allowedRoles.any { userRoleCodes.contains(it) }
            }
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
