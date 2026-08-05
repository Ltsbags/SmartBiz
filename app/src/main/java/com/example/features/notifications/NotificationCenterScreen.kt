package com.example.features.notifications

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.shared.ui.components.notifications.EmptyNotificationState
import com.example.shared.ui.components.notifications.NotificationBadge
import com.example.shared.ui.components.notifications.NotificationCard
import com.example.shared.ui.components.notifications.NotificationFilterSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCenterScreen(
    viewModel: NotificationCenterViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedTab by viewModel.selectedTab.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()
    val priorityFilter by viewModel.priorityFilter.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadCount by viewModel.unreadCount.collectAsState()

    var showFilterSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Notification Center",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        NotificationBadge(count = unreadCount)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("back_btn")) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.markAllAsRead() },
                        modifier = Modifier.testTag("mark_all_read_btn")
                    ) {
                        Icon(imageVector = Icons.Default.DoneAll, contentDescription = "Mark All Read")
                    }
                    IconButton(
                        onClick = { showFilterSheet = true },
                        modifier = Modifier.testTag("filter_btn")
                    ) {
                        Icon(imageVector = Icons.Default.FilterList, contentDescription = "Filter")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search notifications by title or category...") },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("notification_search_input")
            )

            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                edgePadding = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                NotificationTab.values().forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { viewModel.selectTab(tab) },
                        text = {
                            Text(
                                text = when (tab) {
                                    NotificationTab.ALL -> "All"
                                    NotificationTab.UNREAD -> "Unread ($unreadCount)"
                                    NotificationTab.PINNED -> "Pinned"
                                    NotificationTab.ARCHIVED -> "Archive"
                                },
                                fontWeight = if (selectedTab == tab) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("tab_${tab.name}")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (notifications.isEmpty()) {
                EmptyNotificationState(
                    title = when (selectedTab) {
                        NotificationTab.UNREAD -> "No Unread Notifications"
                        NotificationTab.PINNED -> "No Pinned Alerts"
                        NotificationTab.ARCHIVED -> "Archive is Empty"
                        else -> "No Notifications"
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp)
                ) {
                    items(notifications, key = { it.id }) { notif ->
                        NotificationCard(
                            notification = notif,
                            onMarkAsRead = { viewModel.markAsRead(notif.id) },
                            onTogglePin = { viewModel.togglePin(notif.id, notif.isPinned) },
                            onArchive = { viewModel.archiveNotification(notif.id) },
                            onDelete = { viewModel.deleteNotification(notif.id) }
                        )
                    }
                }
            }
        }
    }

    if (showFilterSheet) {
        NotificationFilterSheet(
            selectedCategory = categoryFilter,
            selectedPriority = priorityFilter,
            onCategorySelected = { viewModel.setCategoryFilter(it) },
            onPrioritySelected = { viewModel.setPriorityFilter(it) },
            onDismiss = { showFilterSheet = false }
        )
    }
}
