package com.example.features.communication.widgets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.example.core.database.entity.CommunicationMessageEntity

@Composable
fun DeliveryTrackingWidget(
    messages: List<CommunicationMessageEntity>
) {
    val totalCount = messages.size
    val deliveredCount = messages.count { it.status.uppercase() == "DELIVERED" }
    val queuedCount = messages.count { it.status.uppercase() in listOf("QUEUED", "SENDING") }
    val retryCount = messages.count { it.status.uppercase() == "RETRY" }
    val failedCount = messages.count { it.status.uppercase() == "FAILED" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("delivery_tracking_metric_widget"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Live Dispatch Metrics",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MetricItem(label = "Total", count = totalCount, color = MaterialTheme.colorScheme.primary)
                MetricItem(label = "Delivered", count = deliveredCount, color = Color(0xFF2E7D32))
                MetricItem(label = "Queued", count = queuedCount, color = Color(0xFF0288D1))
                MetricItem(label = "Retries", count = retryCount, color = Color(0xFFED6C02))
                MetricItem(label = "Failed", count = failedCount, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun MetricItem(
    label: String,
    count: Int,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
