package com.example.features.communication.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.CommunicationMessageEntity
import com.example.features.communication.widgets.ConversationHistoryWidget
import com.example.features.communication.widgets.DeliveryTrackingWidget

@Composable
fun CommunicationDashboardScreen(
    messages: List<CommunicationMessageEntity>,
    channelFilter: String,
    statusFilter: String,
    onSelectChannelFilter: (String) -> Unit,
    onSelectStatusFilter: (String) -> Unit,
    onRetryMessage: (Long) -> Unit
) {
    val channels = listOf("ALL", "WHATSAPP", "EMAIL", "SMS", "PUSH", "TELEGRAM", "SLACK")
    val statuses = listOf("ALL", "DELIVERED", "QUEUED", "SENDING", "FAILED", "RETRY")
    var searchQuery by remember { mutableStateOf("") }

    val filteredMessages = remember(messages, searchQuery) {
        if (searchQuery.isBlank()) messages else {
            val q = searchQuery.lowercase()
            messages.filter {
                it.recipient.lowercase().contains(q) ||
                it.recipientName.lowercase().contains(q) ||
                it.subject.lowercase().contains(q) ||
                it.body.lowercase().contains(q)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Live Metric Banner
            DeliveryTrackingWidget(messages = messages)

            Spacer(modifier = Modifier.height(16.dp))

            // Search input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search logs by recipient, subject or body...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("communication_search_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Channel Filter Chips
            Text("Filter Channel:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                items(channels) { ch ->
                    FilterChip(
                        selected = channelFilter == ch,
                        onClick = { onSelectChannelFilter(ch) },
                        label = { Text(ch, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("filter_channel_$ch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Filter Chips
            Text("Filter Status:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                items(statuses) { st ->
                    FilterChip(
                        selected = statusFilter == st,
                        onClick = { onSelectStatusFilter(st) },
                        label = { Text(st, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("filter_status_$st")
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredMessages.isEmpty()) {
                Text(
                    text = "No communication activity logs match selected criteria.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(filteredMessages, key = { it.id }) { message ->
                        ConversationHistoryWidget(
                            message = message,
                            onRetry = { onRetryMessage(message.id) },
                            onInspectLogs = {}
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
