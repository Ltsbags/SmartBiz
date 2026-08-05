package com.example.features.usermanagement.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.LoginHistoryEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun LoginHistoryCard(
    history: LoginHistoryEntity
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm:ss a", Locale.getDefault())

    val (icon, iconTint) = when (history.action) {
        "SUCCESSFUL_LOGIN" -> Pair(Icons.Default.Lock, MaterialTheme.colorScheme.primary)
        "FAILED_LOGIN" -> Pair(Icons.Default.Error, MaterialTheme.colorScheme.error)
        "LOGOUT" -> Pair(Icons.Default.Logout, MaterialTheme.colorScheme.outline)
        "PIN_CHANGED" -> Pair(Icons.Default.Key, MaterialTheme.colorScheme.secondary)
        "BIOMETRIC_ENABLED", "BIOMETRIC_DISABLED" -> Pair(Icons.Default.Fingerprint, MaterialTheme.colorScheme.tertiary)
        else -> Pair(Icons.Default.Person, MaterialTheme.colorScheme.primary)
    }

    val actionTitle = when (history.action) {
        "SUCCESSFUL_LOGIN" -> "Successful Login"
        "FAILED_LOGIN" -> "Failed Login Attempt"
        "LOGOUT" -> "User Logout"
        "PIN_CHANGED" -> "Security PIN Changed"
        "BIOMETRIC_ENABLED" -> "Biometric Login Enabled"
        "BIOMETRIC_DISABLED" -> "Biometric Login Disabled"
        "PROFILE_UPDATED" -> "Profile Updated"
        else -> history.action.replace("_", " ")
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("login_history_card_${history.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.padding(end = 16.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = actionTitle,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (history.status == "SUCCESS") {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Success",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Error,
                            contentDescription = "Failure",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${history.deviceName} • ${dateFormat.format(Date(history.timestamp))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (history.details.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = history.details,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}
