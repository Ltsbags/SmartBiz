package com.example.features.search

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Money
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.example.repositories.SearchResultItem
import com.example.repositories.SearchResultType
import com.example.shared.widgets.EnhancedEmptyStateWidget
import com.example.shared.widgets.PageHeader
import com.example.shared.widgets.SkeletonList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GlobalSearchScreen(
    viewModel: GlobalSearchViewModel,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = "Global Search",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            // Search Bar
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onQueryChanged(it) },
                placeholder = { Text("Search products, invoices, customers, expenses...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (state.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.onQueryChanged("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Searches Chips
            if (state.query.isEmpty() && state.recentSearches.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recent Searches",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = { viewModel.clearSearchHistory() }) {
                        Text("Clear All")
                    }
                }

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.recentSearches.forEach { recent ->
                        FilterChip(
                            selected = false,
                            onClick = { viewModel.executeRecentSearch(recent) },
                            label = { Text(recent) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Domain Filter Chips
            if (state.query.isNotEmpty()) {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val filterTypes = listOf(
                        SearchResultType.PRODUCT to "Products",
                        SearchResultType.CUSTOMER to "Customers",
                        SearchResultType.INVOICE to "Invoices",
                        SearchResultType.PURCHASE to "Purchases",
                        SearchResultType.EXPENSE to "Expenses",
                        SearchResultType.SUPPLIER to "Suppliers"
                    )

                    filterTypes.forEach { (type, label) ->
                        FilterChip(
                            selected = state.selectedFilterType == type,
                            onClick = { viewModel.selectFilterType(type) },
                            label = { Text(label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Search Content / Skeleton / Results / Empty State
            when {
                state.isLoading -> {
                    SkeletonList(count = 6)
                }
                state.query.isEmpty() -> {
                    EnhancedEmptyStateWidget(
                        title = "Search SmartBiz Database",
                        description = "Type keywords above to instantly locate products, invoices, customers, suppliers, purchases, or expense entries across your business.",
                        icon = Icons.Default.Search
                    )
                }
                state.filteredResults.isEmpty() -> {
                    EnhancedEmptyStateWidget(
                        title = "No Matches Found",
                        description = "No business records matched '${state.query}'. Try adjusting your search term or domain filter.",
                        icon = Icons.Default.Search
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(state.filteredResults) { item ->
                            SearchResultCard(
                                item = item,
                                query = state.query,
                                onClick = { viewModel.saveCurrentSearchToHistory() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    query: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = getIconBgColor(item.type),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = getIconForType(item.type),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.padding(8.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = highlightMatches(item.title, query),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (item.details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = item.details,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun highlightMatches(text: String, query: String) = buildAnnotatedString {
    if (query.isBlank()) {
        append(text)
        return@buildAnnotatedString
    }
    val lowerText = text.lowercase()
    val lowerQuery = query.lowercase()
    var startIndex = 0

    while (startIndex < text.length) {
        val matchIndex = lowerText.indexOf(lowerQuery, startIndex)
        if (matchIndex == -1) {
            append(text.substring(startIndex))
            break
        }
        if (matchIndex > startIndex) {
            append(text.substring(startIndex, matchIndex))
        }
        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold, background = MaterialTheme.colorScheme.primaryContainer)) {
            append(text.substring(matchIndex, matchIndex + query.length))
        }
        startIndex = matchIndex + query.length
    }
}

@Composable
private fun getIconForType(type: SearchResultType): ImageVector {
    return when (type) {
        SearchResultType.PRODUCT -> Icons.Default.Inventory
        SearchResultType.CATEGORY -> Icons.Default.Category
        SearchResultType.CUSTOMER -> Icons.Default.People
        SearchResultType.SUPPLIER -> Icons.Default.Store
        SearchResultType.INVOICE -> Icons.Default.Receipt
        SearchResultType.PURCHASE -> Icons.Default.ShoppingCart
        SearchResultType.EXPENSE -> Icons.Default.Money
        SearchResultType.INCOME -> Icons.Default.Money
    }
}

@Composable
private fun getIconBgColor(type: SearchResultType) = when (type) {
    SearchResultType.PRODUCT -> MaterialTheme.colorScheme.primary
    SearchResultType.CUSTOMER -> MaterialTheme.colorScheme.secondary
    SearchResultType.INVOICE -> MaterialTheme.colorScheme.tertiary
    else -> MaterialTheme.colorScheme.outline
}
