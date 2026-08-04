package com.example.features.dashboard.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.core.constants.AppIcons
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TrendingUp

data class QuickActionItem(
    val title: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color,
    val onClick: () -> Unit,
    val testTag: String
)

@Composable
fun QuickActionCard(
    item: QuickActionItem,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(Dimensions.cardCornerRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimensions.cardElevation),
        modifier = modifier
            .fillMaxWidth()
            .clickable { item.onClick() }
            .testTag(item.testTag)
    ) {
        Column(
            modifier = Modifier
                .padding(Spacing.m)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                shape = RoundedCornerShape(Dimensions.radius12),
                color = item.containerColor,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.contentColor,
                    modifier = Modifier
                        .padding(Spacing.s)
                        .size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(Spacing.s))
            Text(
                text = item.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DashboardGrid(
    onCreateInvoice: () -> Unit,
    onAddProduct: () -> Unit,
    onAddCustomer: () -> Unit,
    onPurchasesClick: () -> Unit = {},
    onSuppliersClick: () -> Unit = {},
    onExpensesClick: () -> Unit = {},
    onIncomeClick: () -> Unit = {},
    onCashBookClick: () -> Unit = {},
    onReportsClick: () -> Unit = {},
    testTag: String = "quick_actions_grid",
    modifier: Modifier = Modifier
) {
    val row1Items = listOf(
        QuickActionItem(
            title = "New Invoice",
            icon = AppIcons.AddInvoice,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.primary,
            onClick = onCreateInvoice,
            testTag = "quick_action_create_invoice"
        ),
        QuickActionItem(
            title = "Purchases",
            icon = Icons.Default.Receipt,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.secondary,
            onClick = onPurchasesClick,
            testTag = "quick_action_purchases"
        ),
        QuickActionItem(
            title = "Expenses",
            icon = Icons.Default.AccountBalanceWallet,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.error,
            onClick = onExpensesClick,
            testTag = "quick_action_expenses"
        ),
        QuickActionItem(
            title = "Cash Book",
            icon = Icons.Default.AccountBalance,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.tertiary,
            onClick = onCashBookClick,
            testTag = "quick_action_cashbook"
        )
    )

    val row2Items = listOf(
        QuickActionItem(
            title = "Income",
            icon = Icons.Default.TrendingUp,
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.primary,
            onClick = onIncomeClick,
            testTag = "quick_action_income"
        ),
        QuickActionItem(
            title = "Suppliers",
            icon = Icons.Default.LocalShipping,
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f),
            contentColor = MaterialTheme.colorScheme.secondary,
            onClick = onSuppliersClick,
            testTag = "quick_action_suppliers"
        ),
        QuickActionItem(
            title = "Products",
            icon = AppIcons.AddProduct,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onAddProduct,
            testTag = "quick_action_add_product"
        ),
        QuickActionItem(
            title = "Customers",
            icon = AppIcons.AddCustomer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            onClick = onAddCustomer,
            testTag = "quick_action_add_customer"
        )
    )

    Column(modifier = modifier.fillMaxWidth().testTag(testTag), verticalArrangement = Arrangement.spacedBy(Spacing.s)) {
        Text(
            text = "Business Modules & Quick Actions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 2.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            row1Items.forEach { item ->
                QuickActionCard(
                    item = item,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.s)
        ) {
            row2Items.forEach { item ->
                QuickActionCard(
                    item = item,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
