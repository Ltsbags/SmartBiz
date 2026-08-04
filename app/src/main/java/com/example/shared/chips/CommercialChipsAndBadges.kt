package com.example.shared.chips

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.core.theme.Dimensions
import com.example.core.theme.Spacing

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    testTag: String = "category_chip",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(Dimensions.chipCornerRadius),
        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
        contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = Spacing.m, vertical = Spacing.s)
        )
    }
}

@Composable
fun StatusChip(
    text: String,
    backgroundColor: Color,
    contentColor: Color,
    testTag: String = "status_chip",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(Dimensions.radius8),
        color = backgroundColor,
        contentColor = contentColor,
        modifier = modifier.testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xxs),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(contentColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun PaymentStatusChip(
    status: String,
    testTag: String = "payment_status_chip",
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (status.uppercase()) {
        "PAID" -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        "OVERDUE" -> MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
        "DRAFT" -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
        else -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.tertiary
    }

    StatusChip(
        text = status,
        backgroundColor = bg,
        contentColor = fg,
        testTag = testTag,
        modifier = modifier
    )
}

@Composable
fun NotificationBadge(
    count: Int,
    testTag: String = "notification_badge",
    modifier: Modifier = Modifier
) {
    if (count > 0) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
            modifier = modifier.testTag(testTag)
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = Spacing.xs, vertical = 2.dp)
            )
        }
    }
}

@Composable
fun OutstandingBadge(
    amountText: String,
    testTag: String = "outstanding_badge",
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(Dimensions.radius8),
        color = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.error,
        modifier = modifier.testTag(testTag)
    ) {
        Text(
            text = "Due $amountText",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.s, vertical = Spacing.xxs)
        )
    }
}

@Composable
fun StockBadgeChip(
    stockQuantity: Int,
    lowStockThreshold: Int = 5,
    testTag: String = "stock_badge",
    modifier: Modifier = Modifier
) {
    val isLow = stockQuantity <= lowStockThreshold
    val (bg, fg) = if (isLow) {
        MaterialTheme.colorScheme.errorContainer to MaterialTheme.colorScheme.error
    } else {
        MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
    }

    StatusChip(
        text = if (isLow) "Low Stock ($stockQuantity)" else "$stockQuantity In Stock",
        backgroundColor = bg,
        contentColor = fg,
        testTag = testTag,
        modifier = modifier
    )
}
