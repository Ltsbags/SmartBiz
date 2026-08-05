package com.example.features.audit.model

enum class RetentionPolicy(val label: String, val days: Int) {
    THIRTY_DAYS("30 Days", 30),
    NINETY_DAYS("90 Days", 90),
    ONE_HUNDRED_EIGHTY_DAYS("180 Days", 180),
    ONE_YEAR("1 Year", 365),
    FOREVER("Forever", -1);

    fun getThresholdTimestamp(): Long? {
        if (days < 0) return null
        val millisInDay = 24L * 60L * 60L * 1000L
        return System.currentTimeMillis() - (days * millisInDay)
    }

    companion object {
        fun fromLabel(label: String): RetentionPolicy {
            return entries.firstOrNull { it.label.equals(label, ignoreCase = true) } ?: NINETY_DAYS
        }
    }
}
