package com.example.features.customers.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.database.entity.CustomerEntity
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing

@Composable
fun CustomerCard(
    customer: CustomerEntity,
    currencySymbol: String = "₹",
    isGridView: Boolean = false,
    onCustomerClick: () -> Unit,
    onEditClick: () -> Unit,
    onArchiveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(Dimensions.radius12),
        colors = CardDefaults.cardColors(
            containerColor = if (customer.isArchived)
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
            else
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCustomerClick() }
            .testTag("customer_card_${customer.id}")
    ) {
        if (isGridView) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.m),
                verticalArrangement = Arrangement.spacedBy(Spacing.xs)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    CustomerAvatar(name = customer.name, size = 42.dp)
                    BoxActionMenu(
                        showMenu = showMenu,
                        isArchived = customer.isArchived,
                        onToggleMenu = { showMenu = !showMenu },
                        onEdit = {
                            showMenu = false
                            onEditClick()
                        },
                        onArchive = {
                            showMenu = false
                            onArchiveClick()
                        },
                        onDelete = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xxs))

                Text(
                    text = customer.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (customer.company.isNotBlank()) {
                    Text(
                        text = customer.company,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = customer.customerCode.ifEmpty { "CUST-${customer.id}" },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text(customer.customerType, fontSize = 10.sp) },
                        modifier = Modifier.height(24.dp),
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.xs))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Outstanding:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutstandingBadge(
                        amount = customer.outstandingBalance,
                        currencySymbol = currencySymbol
                    )
                }
            }
        } else {
            // List View
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.m),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CustomerAvatar(name = customer.name, size = 48.dp)

                Spacer(modifier = Modifier.width(Spacing.m))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (customer.isArchived) {
                            Text(
                                text = "(Archived)",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    if (customer.company.isNotBlank()) {
                        Text(
                            text = customer.company,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = customer.customerCode.ifEmpty { "CUST-${customer.id}" },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                        Text(
                            text = customer.customerType,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (customer.city.isNotBlank()) {
                            Text(text = "•", fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
                            Text(
                                text = customer.city,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.s))

                Column(horizontalAlignment = Alignment.End) {
                    OutstandingBadge(
                        amount = customer.outstandingBalance,
                        currencySymbol = currencySymbol
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    BoxActionMenu(
                        showMenu = showMenu,
                        isArchived = customer.isArchived,
                        onToggleMenu = { showMenu = !showMenu },
                        onEdit = {
                            showMenu = false
                            onEditClick()
                        },
                        onArchive = {
                            showMenu = false
                            onArchiveClick()
                        },
                        onDelete = {
                            showMenu = false
                            onDeleteClick()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxActionMenu(
    showMenu: Boolean,
    isArchived: Boolean,
    onToggleMenu: () -> Unit,
    onEdit: () -> Unit,
    onArchive: () -> Unit,
    onDelete: () -> Unit
) {
    androidx.compose.foundation.layout.Box {
        IconButton(
            onClick = onToggleMenu,
            modifier = Modifier.testTag("customer_menu_btn")
        ) {
            Icon(Icons.Default.MoreVert, contentDescription = "Actions")
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = onToggleMenu
        ) {
            DropdownMenuItem(
                text = { Text("Edit") },
                leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                onClick = onEdit
            )
            DropdownMenuItem(
                text = { Text(if (isArchived) "Restore" else "Archive") },
                leadingIcon = {
                    Icon(
                        if (isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = null
                    )
                },
                onClick = onArchive
            )
            DropdownMenuItem(
                text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                leadingIcon = {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                onClick = onDelete
            )
        }
    }
}
