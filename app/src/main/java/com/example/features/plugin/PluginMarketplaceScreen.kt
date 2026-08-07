package com.example.features.plugin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.PluginEntity
import com.example.services.plugin.MarketplacePluginItem
import com.example.services.plugin.PluginType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginMarketplaceScreen(
    viewModel: PluginViewModel,
    onBackClick: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Platform Extensions & Marketplace",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "SmartBiz Plugin SDK Architecture",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.testTag("plugin_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.refreshMarketplaceCatalog() },
                        modifier = Modifier.testTag("plugin_refresh_btn")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Header Tabs
            val tabs = listOf("Marketplace", "Installed (${state.installedPlugins.size})", "SDK & Active Hooks")
            ScrollableTabRow(
                selectedTabIndex = state.selectedTab,
                edgePadding = 16.dp,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = state.selectedTab == index,
                        onClick = { viewModel.setSelectedTab(index) },
                        text = {
                            Text(
                                title,
                                fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            when (state.selectedTab) {
                0 -> MarketplaceTabContent(state = state, viewModel = viewModel)
                1 -> InstalledPluginsTabContent(state = state, viewModel = viewModel)
                2 -> SdkHooksTabContent(state = state, viewModel = viewModel)
            }
        }
    }

    // Marketplace Plugin Detail Dialog
    state.selectedMarketplaceItem?.let { item ->
        MarketplacePluginDetailDialog(
            item = item,
            installedPlugin = state.installedPlugins.find { it.id == item.manifest.pluginId },
            onDismiss = { viewModel.selectMarketplaceItem(null) },
            onInstall = {
                viewModel.installPlugin(item)
                viewModel.selectMarketplaceItem(null)
            }
        )
    }

    // Installed Plugin Management Dialog
    state.selectedInstalledPlugin?.let { plugin ->
        InstalledPluginDetailDialog(
            plugin = plugin,
            permissions = state.permissionsForSelectedPlugin,
            onDismiss = { viewModel.selectInstalledPlugin(null) },
            onToggleStatus = { viewModel.togglePluginStatus(plugin) },
            onUninstall = { viewModel.uninstallPlugin(plugin.id) },
            onPermissionChange = { permKey, granted -> viewModel.updatePermission(plugin.id, permKey, granted) },
            onActivateLicense = { key, type -> viewModel.activateLicense(plugin.id, key, type) },
            onExecuteCommand = { cmd, args -> viewModel.executePluginCommand(plugin.id, cmd, args) }
        )
    }
}

@Composable
private fun MarketplaceTabContent(
    state: PluginUiState,
    viewModel: PluginViewModel
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Search & Category Filter
        Surface(
            tonalElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = state.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    placeholder = { Text("Search extension marketplace...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (state.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("marketplace_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                val categories = listOf("ALL") + PluginType.values().map { it.name }
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categories) { cat ->
                        val isSelected = state.selectedCategory == cat
                        val label = if (cat == "ALL") "All Categories" else cat.replace("_", " ")
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.clickable { viewModel.setSelectedCategory(cat) }
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Available Extension Plugins (${state.marketplaceCatalog.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                items(state.marketplaceCatalog) { item ->
                    val installed = state.installedPlugins.find { it.id == item.manifest.pluginId }
                    MarketplacePluginCard(
                        item = item,
                        installedPlugin = installed,
                        onClick = { viewModel.selectMarketplaceItem(item) },
                        onQuickInstall = { viewModel.installPlugin(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplacePluginCard(
    item: MarketplacePluginItem,
    installedPlugin: PluginEntity?,
    onClick: () -> Unit,
    onQuickInstall: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Extension,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.manifest.pluginName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (item.isOfficial) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    Icons.Default.Verified,
                                    contentDescription = "Verified Official",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Text(
                            text = "by ${item.manifest.developer} • v${item.manifest.version}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Price badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = item.priceText,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = item.manifest.description,
                fontSize = 13.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = " ${item.rating} (${item.reviewCount}) • ${item.downloadsCount} installs",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (installedPlugin != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (installedPlugin.status == "ENABLED")
                                    Color(0xFFE8F5E9)
                                else Color(0xFFFFF3E0)
                            )
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (installedPlugin.status == "ENABLED") "ACTIVE" else "INSTALLED",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (installedPlugin.status == "ENABLED") Color(0xFF2E7D32) else Color(0xFFE65100)
                        )
                    }
                } else {
                    Button(
                        onClick = onQuickInstall,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(36.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Extension", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun InstalledPluginsTabContent(
    state: PluginUiState,
    viewModel: PluginViewModel
) {
    if (state.installedPlugins.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.Default.Storefront,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "No Extension Plugins Installed Yet",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Browse the Marketplace tab to install modules like Payments, AI Assistant, WhatsApp messaging, and Custom Reports.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = { viewModel.setSelectedTab(0) }) {
                    Text("Browse Marketplace")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Installed Enterprise Modules (${state.installedPlugins.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(state.installedPlugins) { plugin ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.selectInstalledPlugin(plugin) },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = plugin.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = plugin.pluginType,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "v${plugin.version} • ${plugin.developer} • License: ${plugin.licenseType}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = plugin.description,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(horizontalAlignment = Alignment.End) {
                            Switch(
                                checked = plugin.status == "ENABLED",
                                onCheckedChange = { viewModel.togglePluginStatus(plugin) },
                                modifier = Modifier.testTag("plugin_switch_${plugin.id}")
                            )
                            Text(
                                text = if (plugin.status == "ENABLED") "Enabled" else "Disabled",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (plugin.status == "ENABLED") Color(0xFF2E7D32) else Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SdkHooksTabContent(
    state: PluginUiState,
    viewModel: PluginViewModel
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Security,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Plugin SDK Isolation & Event Bus Safety",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Plugins communicate exclusively through SmartBizPluginSdk and EventBus. Direct database access and repository references are strictly blocked in sandbox mode.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        // Registered Navigation Items
        item {
            Text(
                text = "Registered Navigation Destinations (${state.registeredNavItems.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (state.registeredNavItems.isEmpty()) {
            item {
                Text("No dynamic navigation items registered by active plugins.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(state.registeredNavItems) { item ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = item.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text(text = "Route: ${item.route} • Plugin: ${item.pluginId}", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Registered Dashboard Widgets
        item {
            Text(
                text = "Registered UI Dashboard Widgets (${state.registeredWidgets.size})",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        if (state.registeredWidgets.isEmpty()) {
            item {
                Text("No dynamic UI widgets registered by active plugins.", fontSize = 12.sp, color = Color.Gray)
            }
        } else {
            items(state.registeredWidgets) { widget ->
                Card(shape = RoundedCornerShape(12.dp)) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = widget.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Spacer(modifier = Modifier.weight(1f))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(text = widget.targetScreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Text(text = widget.description, fontSize = 12.sp, color = Color.Gray)
                    }
                }
            }
        }

        // Test Command Execution Output
        item {
            Text(
                text = "Sandboxed Command Output Logs",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = state.commandResultOutput ?: "No plugin command executed yet. Open an installed plugin details dialog to trigger sandboxed commands.",
                        fontSize = 12.sp,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun MarketplacePluginDetailDialog(
    item: MarketplacePluginItem,
    installedPlugin: PluginEntity?,
    onDismiss: () -> Unit,
    onInstall: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Extension, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = item.manifest.pluginName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column {
                Text(text = "by ${item.manifest.developer} • v${item.manifest.version}", fontSize = 12.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = item.manifest.description, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Required Permissions:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                item.manifest.permissions.forEach { perm ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = perm.title, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(text = "Release Notes:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Text(text = item.releaseNotes, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        confirmButton = {
            if (installedPlugin == null) {
                Button(onClick = onInstall) {
                    Text("Install Extension")
                }
            } else {
                OutlinedButton(onClick = onDismiss) {
                    Text("Already Installed")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
private fun InstalledPluginDetailDialog(
    plugin: PluginEntity,
    permissions: List<com.example.core.database.entity.PluginPermissionEntity>,
    onDismiss: () -> Unit,
    onToggleStatus: () -> Unit,
    onUninstall: () -> Unit,
    onPermissionChange: (String, Boolean) -> Unit,
    onActivateLicense: (String, String) -> Unit,
    onExecuteCommand: (String, String) -> Unit
) {
    var licenseInput by remember { mutableStateOf(plugin.licenseKey) }
    var selectedLicenseType by remember { mutableStateOf(plugin.licenseType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(text = plugin.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(text = "ID: ${plugin.id} • v${plugin.version}", fontSize = 11.sp, color = Color.Gray)
            }
        },
        text = {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.height(380.dp)
            ) {
                item {
                    Text(text = plugin.description, fontSize = 13.sp)
                }

                item {
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Plugin Sandbox Permissions", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(permissions) { perm ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = perm.permissionName, fontSize = 12.sp)
                        Switch(
                            checked = perm.isGranted,
                            onCheckedChange = { onPermissionChange(perm.permissionName, it) }
                        )
                    }
                }

                item {
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "License Manager", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = licenseInput,
                        onValueChange = { licenseInput = it },
                        label = { Text("License Key") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onActivateLicense(licenseInput, "PAID") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Activate Paid", fontSize = 11.sp)
                        }
                        OutlinedButton(
                            onClick = { onActivateLicense("", "TRIAL") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("14-Day Trial", fontSize = 11.sp)
                        }
                    }
                }

                item {
                    Divider()
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Test Sandboxed Commands", fontWeight = FontWeight.Bold, fontSize = 14.sp)

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = {
                            val cmd = when (plugin.id) {
                                "com.smartbiz.plugin.advanced_reports" -> "EXPORT_GST_REPORT"
                                "com.smartbiz.plugin.razorpay_gateway" -> "INITIATE_UPI_REFUND"
                                "com.smartbiz.plugin.whatsapp_alerts" -> "SEND_WHATSAPP_INVOICE"
                                else -> "ANALYZE_STOCK_TRENDS"
                            }
                            onExecuteCommand(cmd, "{\"user\":\"admin\"}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Execute Command")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onToggleStatus,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (plugin.status == "ENABLED") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            ) {
                Text(if (plugin.status == "ENABLED") "Disable" else "Enable")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onUninstall,
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Uninstall")
            }
        }
    )
}
