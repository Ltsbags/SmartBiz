package com.example.core.utils

import com.example.core.constants.AppConstants
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatCurrency(amount: Double, symbol: String = AppConstants.DEFAULT_CURRENCY): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        formatter.minimumFractionDigits = 2
        formatter.maximumFractionDigits = 2
        return "$symbol${formatter.format(amount)}"
    }

    fun formatDate(timestamp: Long, pattern: String = "dd/MM/yyyy"): String {
        if (timestamp <= 0L) return "N/A"
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatDateWithTime(timestamp: Long): String {
        if (timestamp <= 0L) return "N/A"
        val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun formatNumber(number: Number): String {
        val formatter = NumberFormat.getNumberInstance(Locale.getDefault())
        return formatter.format(number)
    }

    fun formatPercentage(value: Double): String {
        return String.format(Locale.getDefault(), "%.1f%%", value)
    }

    fun truncateText(text: String, maxLength: Int = 30): String {
        if (text.length <= maxLength) return text
        return text.take(maxLength - 3) + "..."
    }
}
