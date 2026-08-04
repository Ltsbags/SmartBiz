package com.example.features.customers.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.example.core.theme.Dimensions

@Composable
fun OutstandingBadge(
    amount: Double,
    currencySymbol: String = "₹",
    modifier: Modifier = Modifier
) {
    val isZeroOrNegative = amount <= 0.0
    val backgroundColor = if (isZeroOrNegative) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
    val textColor = if (isZeroOrNegative) Color(0xFF2E7D32) else Color(0xFFC62828)
    val label = if (isZeroOrNegative) "Settled" else "$currencySymbol${String.format("%.2f", amount)}"

    Box(
        modifier = modifier
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(Dimensions.radius8)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
