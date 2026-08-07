package com.example.features.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.core.constants.AppConstants
import com.example.shared.widgets.PageHeader
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(state.userMessage) {
        state.userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reset All Preferences?") },
            text = { Text("This will restore all business details, invoice defaults, and application settings back to original defaults. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetPreferences()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Reset Defaults")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PageHeader(
                title = "Settings & Configuration",
                subtitle = "Control business profile, invoice defaults & system preferences",
                actionIcon = Icons.Default.Refresh,
                onActionClick = { showResetDialog = true },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category Tab Row
            val tabs = listOf(
                "Profile",
                "Security",
                "Invoice",
                "App Defaults",
                "Backup",
                "Data",
                "Theme",
                "About"
            )

            PrimaryTabRow(
                selectedTabIndex = state.selectedTab,
                modifier = Modifier.fillMaxWidth()
            ) {
                tabs.forEachIndexed { idx, title ->
                    Tab(
                        selected = state.selectedTab == idx,
                        onClick = { viewModel.selectTab(idx) },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (state.selectedTab == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            // Scrollable Settings Content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (state.selectedTab) {
                    0 -> BusinessProfileSection(state = state, viewModel = viewModel)
                    1 -> SecuritySection(viewModel = viewModel)
                    2 -> InvoiceSettingsSection(state = state, viewModel = viewModel)
                    3 -> AppDefaultsSection(state = state, viewModel = viewModel)
                    4 -> BackupRestoreSection(state = state, viewModel = viewModel)
                    5 -> DataManagementSection(state = state, viewModel = viewModel)
                    6 -> ThemeSettingsSection(state = state, viewModel = viewModel)
                    7 -> AboutSection(state = state)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun BusinessProfileSection(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.Business, title = "Business Profile")

            OutlinedTextField(
                value = state.businessName,
                onValueChange = { viewModel.updateBusinessName(it) },
                label = { Text("Business / Store Name") },
                leadingIcon = { Icon(Icons.Default.Business, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            OutlinedTextField(
                value = state.businessTagline,
                onValueChange = { viewModel.updateBusinessTagline(it) },
                label = { Text("Tagline / Subtitle") },
                leadingIcon = { Icon(Icons.Default.Description, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.businessPhone,
                    onValueChange = { viewModel.updateBusinessPhone(it) },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.businessEmail,
                    onValueChange = { viewModel.updateBusinessEmail(it) },
                    label = { Text("Business Email") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.businessGst,
                    onValueChange = { viewModel.updateBusinessGst(it) },
                    label = { Text("GSTIN / Tax ID") },
                    leadingIcon = { Icon(Icons.Default.Receipt, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.businessPan,
                    onValueChange = { viewModel.updateBusinessPan(it) },
                    label = { Text("PAN Number") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = state.businessAddress,
                onValueChange = { viewModel.updateBusinessAddress(it) },
                label = { Text("Full Postal Address") },
                leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = state.businessWebsite,
                onValueChange = { viewModel.updateBusinessWebsite(it) },
                label = { Text("Website URL") },
                leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun InvoiceSettingsSection(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.Receipt, title = "Invoice & Billing Settings")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.invoicePrefix,
                    onValueChange = { viewModel.updateInvoicePrefix(it) },
                    label = { Text("Invoice Prefix") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.nextInvoiceNumber.toString(),
                    onValueChange = { num ->
                        num.toIntOrNull()?.let { viewModel.updateNextInvoiceNumber(it) }
                    },
                    label = { Text("Starting Number") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            OutlinedTextField(
                value = state.invoiceTerms,
                onValueChange = { viewModel.updateInvoiceTerms(it) },
                label = { Text("Standard Payment Terms & Conditions") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            OutlinedTextField(
                value = state.invoiceNotes,
                onValueChange = { viewModel.updateInvoiceNotes(it) },
                label = { Text("Footer Thank You Note") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            SwitchRow(
                title = "Tax Inclusive Prices",
                subtitle = "Calculates item tax as built into unit price by default",
                checked = state.isTaxInclusive,
                onCheckedChange = { viewModel.updateTaxInclusive(it) }
            )

            SwitchRow(
                title = "Show Store Logo on Invoices",
                subtitle = "Includes branding header on PDF exports & thermal receipts",
                checked = state.showLogoOnInvoice,
                onCheckedChange = { viewModel.updateShowLogoOnInvoice(it) }
            )
        }
    }
}

@Composable
private fun AppDefaultsSection(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.Tune, title = "Application Preferences & Defaults")

            Text(
                text = "Currency Symbol",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val currencies = listOf("$", "₹", "€", "£", "¥", "A$", "C$", "AED")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                currencies.forEach { curr ->
                    FilterChip(
                        selected = state.currencySymbol == curr,
                        onClick = { viewModel.updateCurrencySymbol(curr) },
                        label = { Text(curr, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.defaultTaxRate.toString(),
                    onValueChange = { rate ->
                        rate.toFloatOrNull()?.let { viewModel.updateDefaultTaxRate(it) }
                    },
                    label = { Text("Default Tax Rate (%)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.defaultPaymentTermsDays.toString(),
                    onValueChange = { days ->
                        days.toIntOrNull()?.let { viewModel.updateDefaultPaymentTermsDays(it) }
                    },
                    label = { Text("Credit Terms (Days)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = state.lowStockThreshold.toString(),
                    onValueChange = { thresh ->
                        thresh.toIntOrNull()?.let { viewModel.updateLowStockThreshold(it) }
                    },
                    label = { Text("Low Stock Alert Qty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = state.dateFormat,
                    onValueChange = { viewModel.updateDateFormat(it) },
                    label = { Text("Date Format") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            SwitchRow(
                title = "Compact Screen Layout Mode",
                subtitle = "Reduces vertical padding for higher information density",
                checked = state.compactUiEnabled,
                onCheckedChange = { viewModel.updateCompactUi(it) }
            )
        }
    }
}

@Composable
private fun BackupRestoreSection(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.CloudUpload, title = "Database Backup & Safety")

            Text(
                text = "Keep your store inventory, customer balances, and invoice history safely backed up in local app storage or cloud export.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val lastBackupText = if (state.lastBackupTimestamp > 0) {
                SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date(state.lastBackupTimestamp))
            } else {
                "Never"
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Last System Backup",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = lastBackupText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { viewModel.createBackup(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CloudUpload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Backup")
                }

                OutlinedButton(
                    onClick = { viewModel.restoreBackup(context) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.CloudDownload, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Restore")
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            SwitchRow(
                title = "Automated Daily Backups",
                subtitle = "Automatically saves encrypted store snapshot every midnight",
                checked = state.autoBackupEnabled,
                onCheckedChange = { viewModel.updateAutoBackup(it) }
            )
        }
    }
}

@Composable
private fun DataManagementSection(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.Storage, title = "Data Management & CSV Export")

            Text(
                text = "Export primary database tables directly to standard CSV format for accounting in Excel / Tally.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.exportCsv(context, "Invoices") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Invoices CSV")
                }
                OutlinedButton(
                    onClick = { viewModel.exportCsv(context, "Inventory") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Products CSV")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.exportCsv(context, "Customers") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Customers CSV")
                }
                OutlinedButton(
                    onClick = { viewModel.exportCsv(context, "Expenses") },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Expenses CSV")
                }
            }
        }
    }
}

@Composable
private fun ThemeSettingsSection(
    state: SettingsState,
    viewModel: SettingsViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.Palette, title = "Theme & Visual Styling")

            Text(
                text = "Appearance Theme Mode",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val modes = listOf("SYSTEM", "LIGHT", "DARK")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                modes.forEach { mode ->
                    FilterChip(
                        selected = state.themeMode == mode,
                        onClick = { viewModel.updateThemeMode(mode) },
                        label = { Text(mode) }
                    )
                }
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp))

            SwitchRow(
                title = "Material You Dynamic Colors",
                subtitle = "Adapts accent colors dynamically to device wallpaper on Android 12+",
                checked = state.dynamicColorEnabled,
                onCheckedChange = { viewModel.updateDynamicColor(it) }
            )

            Text(
                text = "Primary Accent Palette",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            val colors = listOf(
                "#2196F3" to Color(0xFF2196F3),
                "#4CAF50" to Color(0xFF4CAF50),
                "#FF9800" to Color(0xFFFF9800),
                "#E91E63" to Color(0xFFE91E63),
                "#9C27B0" to Color(0xFF9C27B0),
                "#00BCD4" to Color(0xFF00BCD4)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                colors.forEach { (hex, col) ->
                    val isSelected = state.accentColorHex == hex
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(col, CircleShape)
                            .clickable { viewModel.updateAccentColor(hex) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AboutSection(
    state: SettingsState
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionHeader(icon = Icons.Default.Info, title = "About BillNova")

            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = AppConstants.APP_NAME,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = AppConstants.APP_TAGLINE,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version ${AppConstants.APP_VERSION} (Build 2026.08)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            InfoRow(label = "Platform Framework", value = "Kotlin Jetpack Compose & Clean Architecture")
            InfoRow(label = "Database Engine", value = "SQLite Room v2.6.1")
            InfoRow(label = "State Management", value = "StateFlow & Coroutines")
            InfoRow(label = "Developer Support", value = "support@smartbiz.com")
            InfoRow(label = "License & TOS", value = "Commercial Enterprise Edition")
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SecuritySection(
    viewModel: SettingsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader(icon = Icons.Default.Lock, title = "Security & Session Control")

            SwitchRow(
                title = "Require Application Lock PIN",
                subtitle = "Require PIN verification when launching or reopening BillNova",
                checked = state.isAppLockEnabled,
                onCheckedChange = { viewModel.toggleAppLock(it) }
            )

            Divider()

            SwitchRow(
                title = "Enable Biometric Unlock",
                subtitle = "Use Fingerprint or Face Unlock to bypass PIN entry",
                checked = state.isBiometricsEnabled,
                onCheckedChange = { viewModel.toggleBiometrics(it) }
            )

            Divider()

            InfoRow(label = "PIN Encryption", value = "SHA-256 Salted Hash")
            InfoRow(label = "Biometric API", value = "AndroidX Biometric Standard")
            InfoRow(label = "Failed Attempt Protection", value = "5 Attempts / 5-Min Cooldown")
            InfoRow(label = "Offline Storage", value = "AES Encrypted SharedPreferences")
        }
    }
}
