package com.example.shared.ui.components.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PriorityBadge(
    priority: String,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (priority.uppercase()) {
        "CRITICAL", "URGENT" -> Color(0xFFFDE8E8) to Color(0xFFE53935)
        "HIGH" -> Color(0xFFFFF3E0) to Color(0xFFF57C00)
        "MEDIUM" -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    Text(
        text = priority.uppercase(),
        color = textColor,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        modifier = modifier
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}
