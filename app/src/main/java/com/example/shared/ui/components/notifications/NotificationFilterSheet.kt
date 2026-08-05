package com.example.shared.ui.components.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationFilterSheet(
    selectedCategory: String?,
    selectedPriority: String?,
    onCategorySelected: (String?) -> Unit,
    onPrioritySelected: (String?) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categories = listOf("SECURITY", "SALES", "PURCHASES", "INVENTORY", "CUSTOMERS", "SUPPLIERS", "FINANCE", "SYSTEM", "REPORTS", "CUSTOM")
    val priorities = listOf("LOW", "MEDIUM", "HIGH", "CRITICAL")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Filter Notifications",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.weight(1f))
                TextButton(onClick = {
                    onCategorySelected(null)
                    onPrioritySelected(null)
                }) {
                    Text("Reset All")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Category",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                categories.chunked(3).forEach { rowCategories ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = {
                                    if (selectedCategory == cat) onCategorySelected(null) else onCategorySelected(cat)
                                },
                                label = { Text(cat, style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.padding(end = 6.dp, bottom = 6.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Priority Level",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                priorities.forEach { prio ->
                    FilterChip(
                        selected = selectedPriority == prio,
                        onClick = {
                            if (selectedPriority == prio) onPrioritySelected(null) else onPrioritySelected(prio)
                        },
                        label = { Text(prio) },
                        modifier = Modifier.padding(end = 8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
