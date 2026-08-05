package com.example.features.reports.models

import java.util.Calendar

enum class DateFilterOption(val displayName: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    LAST_WEEK("Last Week"),
    THIS_MONTH("This Month"),
    LAST_MONTH("Last Month"),
    THIS_QUARTER("This Quarter"),
    THIS_YEAR("This Year"),
    CUSTOM("Custom Range");

    fun getTimeRange(customStartMs: Long? = null, customEndMs: Long? = null): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        val now = cal.timeInMillis

        // Reset time to start of today
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val startOfToday = cal.timeInMillis

        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        cal.set(Calendar.MILLISECOND, 999)
        val endOfToday = cal.timeInMillis

        return when (this) {
            TODAY -> Pair(startOfToday, endOfToday)
            YESTERDAY -> {
                val start = startOfToday - (24 * 60 * 60 * 1000L)
                val end = startOfToday - 1L
                Pair(start, end)
            }
            THIS_WEEK -> {
                cal.timeInMillis = startOfToday
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val start = cal.timeInMillis
                Pair(start, now)
            }
            LAST_WEEK -> {
                cal.timeInMillis = startOfToday
                cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
                val end = cal.timeInMillis - 1L
                val start = end - (7 * 24 * 60 * 60 * 1000L) + 1L
                Pair(start, end)
            }
            THIS_MONTH -> {
                cal.timeInMillis = startOfToday
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                Pair(start, now)
            }
            LAST_MONTH -> {
                cal.timeInMillis = startOfToday
                cal.set(Calendar.DAY_OF_MONTH, 1)
                cal.add(Calendar.MONTH, -1)
                val start = cal.timeInMillis
                cal.add(Calendar.MONTH, 1)
                val end = cal.timeInMillis - 1L
                Pair(start, end)
            }
            THIS_QUARTER -> {
                cal.timeInMillis = startOfToday
                val currentMonth = cal.get(Calendar.MONTH)
                val quarterStartMonth = (currentMonth / 3) * 3
                cal.set(Calendar.MONTH, quarterStartMonth)
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val start = cal.timeInMillis
                Pair(start, now)
            }
            THIS_YEAR -> {
                cal.timeInMillis = startOfToday
                cal.set(Calendar.DAY_OF_YEAR, 1)
                val start = cal.timeInMillis
                Pair(start, now)
            }
            CUSTOM -> {
                val start = customStartMs ?: (now - 30 * 24 * 60 * 60 * 1000L)
                val end = customEndMs ?: now
                Pair(start, end)
            }
        }
    }
}
