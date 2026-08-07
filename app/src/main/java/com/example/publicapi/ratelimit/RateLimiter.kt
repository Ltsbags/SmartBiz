package com.example.publicapi.ratelimit

import com.example.publicapi.auth.RateLimitTier
import java.util.concurrent.ConcurrentHashMap

data class RateLimitResult(
    val isAllowed: Boolean,
    val limit: Int,
    val remaining: Int,
    val resetSeconds: Long
)

class RateLimiter {

    private data class WindowState(
        var count: Int,
        var windowStartTimestamp: Long
    )

    private val requestWindows = ConcurrentHashMap<String, WindowState>()

    fun checkRateLimit(
        clientIdentifier: String,
        tier: RateLimitTier = RateLimitTier.FREE,
        customLimit: Int? = null
    ): RateLimitResult {
        val limit = customLimit ?: tier.requestsPerMinute
        val currentTime = System.currentTimeMillis()
        val windowSizeMs = 60_000L // 1 minute window

        val state = requestWindows.compute(clientIdentifier) { _, existing ->
            if (existing == null || (currentTime - existing.windowStartTimestamp) >= windowSizeMs) {
                WindowState(count = 1, windowStartTimestamp = currentTime)
            } else {
                existing.count += 1
                existing
            }
        }!!

        val timeElapsed = currentTime - state.windowStartTimestamp
        val resetSeconds = ((windowSizeMs - timeElapsed) / 1000L).coerceAtLeast(1L)
        val isAllowed = state.count <= limit
        val remaining = (limit - state.count).coerceAtLeast(0)

        return RateLimitResult(
            isAllowed = isAllowed,
            limit = limit,
            remaining = remaining,
            resetSeconds = resetSeconds
        )
    }

    fun resetLimit(clientIdentifier: String) {
        requestWindows.remove(clientIdentifier)
    }

    fun clearAll() {
        requestWindows.clear()
    }
}
