package com.example.features.expenses.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.database.entity.ExpenseCategoryEntity
import com.example.core.theme.Spacing
import com.example.shared.buttons.PrimaryButton
import com.example.shared.buttons.SecondaryButton
import com.example.shared.forms.SmartBizTextField

@Composable
fun CategoryManagementDialog(
    categories: List<ExpenseCategoryEntity>,
    onDismiss: () -> Unit,
    onAddCategory: (ExpenseCategoryEntity) -> Unit,
    onDeleteCategory: (ExpenseCategoryEntity) -> Unit
) {
    var isAddingNew by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryDesc by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Manage Expense Categories",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 420.dp),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                if (isAddingNew) {
                    Column(verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
                        Text(
                            text = "New Category",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        SmartBizTextField(
                            value = newCategoryName,
                            onValueChange = { newCategoryName = it },
                            label = "Category Name",
                            placeholder = "e.g. Travel & Transit"
                        )
                        SmartBizTextField(
                            value = newCategoryDesc,
                            onValueChange = { newCategoryDesc = it },
                            label = "Description (Optional)",
                            placeholder = "Category description..."
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SecondaryButton(
                                text = "Cancel",
                                onClick = { isAddingNew = false }
                            )
                            Spacer(modifier = Modifier.padding(horizontal = Spacing.xs))
                            PrimaryButton(
                                text = "Save",
                                onClick = {
                                    if (newCategoryName.isNotBlank()) {
                                        onAddCategory(
                                            ExpenseCategoryEntity(
                                                name = newCategoryName.trim(),
                                                description = newCategoryDesc.trim()
                                            )
                                        )
                                        newCategoryName = ""
                                        newCategoryDesc = ""
                                        isAddingNew = false
                                    }
                                },
                                enabled = newCategoryName.isNotBlank()
                            )
                        }
                    }
                    Divider(modifier = Modifier.padding(vertical = Spacing.xs))
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Existing Categories (${categories.size})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        PrimaryButton(
                            text = "Add Category",
                            onClick = { isAddingNew = true }
                        )
                    }
                }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(Spacing.xs),
                    modifier = Modifier.weight(1f)
                ) {
                    items(categories) { category ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.s),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = category.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (category.description.isNotBlank()) {
                                        Text(
                                            text = category.description,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                if (!category.isSystemDefault) {
                                    IconButton(onClick = { onDeleteCategory(category) }) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            SecondaryButton(
                text = "Close",
                onClick = onDismiss
            )
        }
    )
}
