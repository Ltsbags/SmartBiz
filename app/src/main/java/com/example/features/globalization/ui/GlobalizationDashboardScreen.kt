package com.example.features.globalization.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.FormatPaint
import androidx.compose.material.icons.filled.GTranslate
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.features.globalization.models.AppLocale
import com.example.features.globalization.models.CurrencyConfig
import com.example.features.globalization.models.DateFormatPattern
import com.example.features.globalization.models.NumberFormatStyle
import com.example.features.globalization.models.TaxProfile
import com.example.features.globalization.viewmodel.GlobalizationViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun GlobalizationDashboardScreen(
    onNavigateBack: () -> Unit,
    viewModel: GlobalizationViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddTranslationDialog by remember { mutableStateOf(false) }

    val layoutDirection = if (state.isRtl) LayoutDirection.Rtl else LayoutDirection.Ltr

    CompositionLocalProvider(LocalLayoutDirection provides layoutDirection) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = viewModel.translate("dashboard_title"),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Locale: ${state.currentLocale.name} (${state.currentLocale.code.uppercase()}) • Currency: ${state.activeCurrency.code}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.testTag("globalization_back_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = { showAddTranslationDialog = true },
                            modifier = Modifier.testTag("add_translation_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Override")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                )
            },
            modifier = Modifier.testTag("globalization_screen")
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Category Segmented Bar
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    SegmentedButton(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        shape = SegmentedButtonDefaults.itemShape(index = 0, count = 4),
                        icon = { Icon(Icons.Default.Language, contentDescription = null) }
                    ) {
                        Text("Languages")
                    }
                    SegmentedButton(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        shape = SegmentedButtonDefaults.itemShape(index = 1, count = 4),
                        icon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) }
                    ) {
                        Text("Currency")
                    }
                    SegmentedButton(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        shape = SegmentedButtonDefaults.itemShape(index = 2, count = 4),
                        icon = { Icon(Icons.Default.Receipt, contentDescription = null) }
                    ) {
                        Text("Tax Engine")
                    }
                    SegmentedButton(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        shape = SegmentedButtonDefaults.itemShape(index = 3, count = 4),
                        icon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                    ) {
                        Text("Formatting")
                    }
                }

                // Banner for Active Context
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = state.currentLocale.flagEmoji,
                                fontSize = 32.sp,
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            Column {
                                Text(
                                    text = "${state.currentLocale.nativeName} (${state.currentLocale.name})",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Text(
                                    text = if (state.isRtl) "Layout: Right-to-Left (RTL)" else "Layout: Left-to-Right (LTR)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = state.activeCurrency.symbol,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }
                        }
                    }
                }

                when (selectedTab) {
                    0 -> LanguageSelectionSection(
                        currentLocale = state.currentLocale,
                        availableLocales = state.availableLocales,
                        onSelectLocale = { viewModel.selectLocale(it) }
                    )
                    1 -> CurrencyManagementSection(
                        activeCurrency = state.activeCurrency,
                        currencies = state.availableCurrencies,
                        sampleAmount = state.sampleAmount,
                        convertedAmount = state.convertedSampleAmount,
                        onSelectCurrency = { viewModel.selectCurrency(it) },
                        onUpdateAmount = { viewModel.setSampleAmount(it) },
                        onUpdateRate = { code, rate -> viewModel.updateExchangeRate(code, rate) },
                        viewModel = viewModel
                    )
                    2 -> TaxEngineSection(
                        activeTaxProfile = state.activeTaxProfile,
                        taxProfiles = state.availableTaxProfiles,
                        sampleAmount = state.sampleAmount,
                        taxResult = state.sampleTaxResult,
                        onSelectTaxProfile = { viewModel.selectTaxProfile(it) }
                    )
                    3 -> RegionalFormattingSection(
                        state = state,
                        onSetDateFormat = { viewModel.setDateFormat(it) },
                        onToggle24Hour = { viewModel.set24HourFormat(it) },
                        viewModel = viewModel
                    )
                }
            }
        }

        if (showAddTranslationDialog) {
            AddTranslationOverrideDialog(
                currentLocaleCode = state.currentLocale.code,
                onDismiss = { showAddTranslationDialog = false },
                onSave = { key, value ->
                    viewModel.saveTranslationOverride(key, value)
                    showAddTranslationDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LanguageSelectionSection(
    currentLocale: AppLocale,
    availableLocales: List<AppLocale>,
    onSelectLocale: (AppLocale) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Supported Enterprise Languages",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Dynamic RTL layout adaptation, localized resource lookups, and missing translation fallback.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                availableLocales.forEach { locale ->
                    val isSelected = locale.code == currentLocale.code
                    Surface(
                        onClick = { onSelectLocale(locale) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = locale.flagEmoji,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(end = 12.dp)
                                )
                                Column {
                                    Text(
                                        text = locale.nativeName,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${locale.name} • Code: ${locale.code}${if (locale.isRtl) " • RTL" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrencyManagementSection(
    activeCurrency: CurrencyConfig,
    currencies: List<CurrencyConfig>,
    sampleAmount: Double,
    convertedAmount: Double,
    onSelectCurrency: (CurrencyConfig) -> Unit,
    onUpdateAmount: (Double) -> Unit,
    onUpdateRate: (String, Double) -> Unit,
    viewModel: GlobalizationViewModel
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Currency & Exchange Engine",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Automated cross-currency conversions, decimal precision, separators, and accounting formatting.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Conversion Interactive Sandbox
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Base Amount (USD): $12,500.50",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Converted: ${viewModel.formatCurrency(convertedAmount)}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                currencies.forEach { currency ->
                    val isSelected = currency.code == activeCurrency.code
                    Surface(
                        onClick = { onSelectCurrency(currency) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "${currency.code} (${currency.symbol})",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (currency.isDefault) {
                                        Text(
                                            text = " [DEFAULT]",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Text(
                                    text = "${currency.name} • Decimals: ${currency.decimalPrecision} • Rate: ${currency.exchangeRateToUsd}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Active",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TaxEngineSection(
    activeTaxProfile: TaxProfile,
    taxProfiles: List<TaxProfile>,
    sampleAmount: Double,
    taxResult: com.example.features.globalization.models.TaxCalculationResult?,
    onSelectTaxProfile: (TaxProfile) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Country Tax Engine (GST / VAT / Sales Tax)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configurable regional tax rules supporting inclusive, exclusive, and compound calculation profiles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            taxResult?.let { res ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Tax Profile: ${res.taxName} (${res.ratePercentage}% ${res.taxType})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Amount:", style = MaterialTheme.typography.bodyMedium)
                            Text("$${String.format("%.2f", res.netAmount)}", fontWeight = FontWeight.Bold)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Tax Amount:", style = MaterialTheme.typography.bodyMedium)
                            Text("$${String.format("%.2f", res.taxAmount)}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        }
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Gross Amount:", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            Text("$${String.format("%.2f", res.grossAmount)}", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                taxProfiles.forEach { profile ->
                    val isSelected = profile.id == activeTaxProfile.id
                    Surface(
                        onClick = { onSelectTaxProfile(profile) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "${profile.name} (${profile.countryCode})",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rate: ${profile.ratePercentage}% • Mode: ${profile.taxType}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (isSelected) {
                                Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RegionalFormattingSection(
    state: com.example.features.globalization.viewmodel.GlobalizationUiState,
    onSetDateFormat: (DateFormatPattern) -> Unit,
    onToggle24Hour: (Boolean) -> Unit,
    viewModel: GlobalizationViewModel
) {
    val now = System.currentTimeMillis()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Regional Formatting Rules",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Custom date, time, and numerical format patterns tailored for target global deployment territories.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Live Preview Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Live Formatting Output:", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Text("Formatted Date: ${viewModel.formatDate(now)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Formatted Time: ${viewModel.formatTime(now)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Accounting Number: ${viewModel.formatNumber(-12500.5, NumberFormatStyle.ACCOUNTING)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Percentage: ${viewModel.formatNumber(0.185, NumberFormatStyle.PERCENTAGE)}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Date Pattern", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            DateFormatPattern.entries.forEach { pattern ->
                val isSelected = state.dateFormat == pattern
                Surface(
                    onClick = { onSetDateFormat(pattern) },
                    shape = RoundedCornerShape(8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(pattern.label, style = MaterialTheme.typography.bodyMedium)
                        if (isSelected) Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("24-Hour Time Format", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                    Text("Use HH:mm:ss instead of hh:mm:ss a", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Switch(
                    checked = state.is24HourFormat,
                    onCheckedChange = onToggle24Hour
                )
            }
        }
    }
}

@Composable
private fun AddTranslationOverrideDialog(
    currentLocaleCode: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var key by remember { mutableStateOf("") }
    var value by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Translation Override ($currentLocaleCode)") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = key,
                    onValueChange = { key = it },
                    label = { Text("Translation Key (e.g. invoice_header)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = { value = it },
                    label = { Text("Localized String Value") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { if (key.isNotBlank() && value.isNotBlank()) onSave(key, value) },
                enabled = key.isNotBlank() && value.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
