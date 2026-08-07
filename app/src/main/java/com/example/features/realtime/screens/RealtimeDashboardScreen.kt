package com.example.features.realtime.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.features.realtime.RealtimeViewModel
import com.example.ui.components.ConnectionStatusBanner
import com.example.ui.components.PresenceCard
import com.example.ui.components.RealtimeEventCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealtimeDashboardScreen(
    viewModel: RealtimeViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    val modules = listOf("ALL", "SALES", "INVENTORY", "SECURITY", "PRESENCE", "NOTIFICATION")

    val filteredEvents = if (uiState.selectedModuleFilter == "ALL") {
        uiState.recentEvents
    } else {
        uiState.recentEvents.filter { it.module.equals(uiState.selectedModuleFilter, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Real-Time & Presence",
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = "${uiState.onlineCount} Online",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("back_button")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.reconnectRealtime() },
                        modifier = Modifier.testTag("refresh_realtime_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Connection")
                    }
                }
            )
        },
        modifier = modifier.testTag("realtime_dashboard_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top Connection Health Status Banner
            ConnectionStatusBanner(
                connectionHealth = uiState.connectionHealth,
                onReconnectClick = { viewModel.reconnectRealtime() }
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Presence Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Branch Team Presence",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                items(uiState.onlineUsers, key = { it.userId }) { presence ->
                    PresenceCard(presence = presence)
                }

                // Presence Toggle Actions
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your Presence Status",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.updateUserStatus("ONLINE", "Active on Terminal") },
                            modifier = Modifier.weight(1f).testTag("status_online_button")
                        ) {
                            Text("Online", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.updateUserStatus("AWAY", "In Store Break") },
                            modifier = Modifier.weight(1f).testTag("status_away_button")
                        ) {
                            Text("Away", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { viewModel.updateUserStatus("BUSY", "Serving Billing Line") },
                            modifier = Modifier.weight(1f).testTag("status_busy_button")
                        ) {
                            Text("Busy", fontSize = 11.sp)
                        }
                    }
                }

                // Real-time Event Broadcaster & Simulation Tools
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Live Event Simulation Engine",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.simulateLiveInvoiceEvent() },
                                    modifier = Modifier.weight(1f).testTag("sim_pos_sale_button"),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("POS Sale", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.simulateLiveStockEvent() },
                                    modifier = Modifier.weight(1f).testTag("sim_stock_change_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) {
                                    Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stock Alert", fontSize = 11.sp)
                                }

                                Button(
                                    onClick = { viewModel.simulateSecurityAlert() },
                                    modifier = Modifier.weight(1f).testTag("sim_security_alert_button"),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Sec Alert", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                // Live Event Feed Header & Module Filters
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Real-Time Event Stream Log",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(modules) { mod ->
                            FilterChip(
                                selected = uiState.selectedModuleFilter == mod,
                                onClick = { viewModel.setModuleFilter(mod) },
                                label = { Text(mod, fontSize = 12.sp) },
                                modifier = Modifier.testTag("filter_chip_$mod")
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }

                // Event Items Stream
                if (filteredEvents.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No real-time events logged yet for module '${uiState.selectedModuleFilter}'",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    items(filteredEvents, key = { it.eventId }) { event ->
                        RealtimeEventCard(event = event)
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
