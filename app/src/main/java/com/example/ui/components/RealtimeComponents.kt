package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.PresenceEntity
import com.example.core.database.entity.RealtimeEventEntity
import com.example.core.realtime.ConnectionState
import com.example.services.ConnectionHealth
import com.example.services.NetworkType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ConnectionStatusBanner(
    connectionHealth: ConnectionHealth,
    onReconnectClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOnline = connectionHealth.realtimeState == ConnectionState.CONNECTED
    val isReconnecting = connectionHealth.realtimeState == ConnectionState.RECONNECTING || connectionHealth.realtimeState == ConnectionState.CONNECTING

    val backgroundColor = when {
        isOnline -> Color(0xFFE8F5E9)
        isReconnecting -> Color(0xFFFFF3E0)
        else -> Color(0xFFFFEBEE)
    }

    val contentColor = when {
        isOnline -> Color(0xFF1B5E20)
        isReconnecting -> Color(0xFFE65100)
        else -> Color(0xFFB71C1C)
    }

    val statusText = when (connectionHealth.realtimeState) {
        ConnectionState.CONNECTED -> "Realtime Live Channel Active (${connectionHealth.networkType.name})"
        ConnectionState.CONNECTING -> "Connecting to WebSocket Gateway..."
        ConnectionState.RECONNECTING -> "Reconnecting WebSocket (Exponential Backoff)..."
        ConnectionState.DISCONNECTED -> "Offline Mode Active (Cached SQLite Data)"
        ConnectionState.FAILED -> "Connection Failed - Operating Offline"
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("connection_status_banner"),
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                LiveIndicator(isConnected = isOnline)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = statusText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Latency: ${connectionHealth.latencyMs}ms | Auto Sync Ready",
                        fontSize = 10.sp,
                        color = contentColor.copy(alpha = 0.8f)
                    )
                }
            }

            if (!isOnline) {
                IconButton(
                    onClick = onReconnectClick,
                    modifier = Modifier.testTag("reconnect_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reconnect",
                        tint = contentColor
                    )
                }
            }
        }
    }
}

@Composable
fun LiveIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    val dotColor = if (isConnected) Color(0xFF4CAF50) else Color(0xFFFF5252)
    Box(
        modifier = modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(dotColor)
            .testTag("live_indicator")
    )
}

@Composable
fun PresenceCard(
    presence: PresenceEntity,
    onStatusClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val statusColor = when (presence.status.uppercase()) {
        "ONLINE" -> Color(0xFF4CAF50)
        "AWAY" -> Color(0xFFFF9800)
        "BUSY" -> Color(0xFFF44336)
        else -> Color(0xFF9E9E9E)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("presence_card_${presence.userId}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.BottomEnd
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = presence.userName,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                        .border(2.dp, Color.White, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = presence.userName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Text(
                    text = if (presence.customStatus.isNotEmpty()) presence.customStatus else "Device: ${presence.currentDevice}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = presence.status,
                    color = statusColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
fun RealtimeEventCard(
    event: RealtimeEventEntity,
    modifier: Modifier = Modifier
) {
    val severityColor = when (event.severity) {
        "WARNING" -> Color(0xFFFF9800)
        "CRITICAL", "ERROR" -> Color(0xFFF44336)
        else -> MaterialTheme.colorScheme.primary
    }

    val icon = when (event.module) {
        "SALES" -> Icons.Default.CheckCircle
        "INVENTORY" -> Icons.Default.Warning
        "SECURITY" -> Icons.Default.Warning
        "NOTIFICATION" -> Icons.Default.Notifications
        else -> Icons.Default.Info
    }

    val formattedTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(event.timestamp))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("realtime_event_card_${event.eventId}"),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(severityColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = event.eventType,
                    tint = severityColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = event.eventType.replace("_", " "),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = severityColor
                    )
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = event.payloadJson,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
