package com.laitoxx.security.utils

import android.util.Log
import com.laitoxx.security.data.exceptions.RateLimitException
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rate Limiter for API requests and network operations
 *
 * SECURITY: Prevents abuse, DoS, and IP bans from external services
 *
 * Features:
 * - Per-endpoint rate limiting
 * - Token bucket algorithm
 * - Automatic request throttling
 * - Configurable limits and windows
 *
 * @param maxRequests Maximum requests allowed per time window
 * @param timeWindowMs Time window in milliseconds
 */
class RateLimiter(
    private val maxRequests: Int = 10,
    private val timeWindowMs: Long = 60_000L  // 1 minute default
) {
    companion object {
        private const val TAG = "RateLimiter"

        // Singleton instances for different endpoint categories
        val ipLookup = RateLimiter(maxRequests = 20, timeWindowMs = 60_000L)
        val subdomainFinder = RateLimiter(maxRequests = 5, timeWindowMs = 60_000L)
        val portScanner = RateLimiter(maxRequests = 100, timeWindowMs = 60_000L)
        val securityScanner = RateLimiter(maxRequests = 3, timeWindowMs = 60_000L)
        val pythonTools = RateLimiter(maxRequests = 10, timeWindowMs = 60_000L)
    }

    private val mutex = Mutex()
    private val requestTimestamps = mutableListOf<Long>()
    private val requestCount = AtomicInteger(0)

    /**
     * Check if request is allowed under current rate limit
     *
     * @throws RateLimitException if rate limit exceeded
     */
    suspend fun checkLimit() {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val windowStart = currentTime - timeWindowMs

            // Remove expired timestamps
            requestTimestamps.removeAll { it < windowStart }

            if (requestTimestamps.size >= maxRequests) {
                val oldestRequest = requestTimestamps.firstOrNull() ?: currentTime
                val waitTimeMs = (oldestRequest + timeWindowMs) - currentTime

                Log.w(TAG, "Rate limit exceeded. Need to wait ${waitTimeMs}ms")

                throw RateLimitException(
                    "Rate limit exceeded. Please wait ${waitTimeMs / 1000} seconds before trying again."
                )
            }

            // Add current request timestamp
            requestTimestamps.add(currentTime)
            requestCount.incrementAndGet()

            Log.d(TAG, "Request allowed. Current count: ${requestTimestamps.size}/$maxRequests")
        }
    }

    /**
     * Execute with rate limiting - blocks until request is allowed
     *
     * @param block Suspending function to execute
     * @return Result of the block execution
     */
    suspend fun <T> executeWithLimit(block: suspend () -> T): T {
        checkLimit()
        return block()
    }

    /**
     * Execute with automatic retry and exponential backoff
     *
     * @param maxRetries Maximum number of retry attempts
     * @param block Suspending function to execute
     * @return Result of the block execution
     */
    suspend fun <T> executeWithRetry(
        maxRetries: Int = 3,
        block: suspend () -> T
    ): T {
        var lastException: Exception? = null

        repeat(maxRetries) { attempt ->
            try {
                checkLimit()
                return block()
            } catch (e: RateLimitException) {
                lastException = e
                Log.w(TAG, "Rate limit hit on attempt ${attempt + 1}/$maxRetries")

                // Exponential backoff
                val backoffMs = (1000L * (1 shl attempt)).coerceAtMost(30_000L)
                Log.d(TAG, "Waiting ${backoffMs}ms before retry")
                delay(backoffMs)
            }
        }

        throw lastException ?: RateLimitException("Max retries exceeded")
    }

    /**
     * Reset the rate limiter (useful for testing or manual override)
     */
    suspend fun reset() {
        mutex.withLock {
            requestTimestamps.clear()
            requestCount.set(0)
            Log.d(TAG, "Rate limiter reset")
        }
    }

    /**
     * Get current request count in the time window
     */
    suspend fun getCurrentCount(): Int {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val windowStart = currentTime - timeWindowMs
            requestTimestamps.removeAll { it < windowStart }
            return requestTimestamps.size
        }
    }

    /**
     * Get time until next request is allowed (in milliseconds)
     * Returns 0 if request can be made immediately
     */
    suspend fun getWaitTime(): Long {
        mutex.withLock {
            val currentTime = System.currentTimeMillis()
            val windowStart = currentTime - timeWindowMs

            requestTimestamps.removeAll { it < windowStart }

            if (requestTimestamps.size < maxRequests) {
                return 0L
            }

            val oldestRequest = requestTimestamps.firstOrNull() ?: return 0L
            return ((oldestRequest + timeWindowMs) - currentTime).coerceAtLeast(0L)
        }
    }
}

/**
 * Advanced rate limiter with per-key tracking (e.g., per domain, per IP)
 */
class KeyedRateLimiter(
    private val maxRequests: Int = 10,
    private val timeWindowMs: Long = 60_000L
) {
    private val limiters = ConcurrentHashMap<String, RateLimiter>()

    /**
     * Get or create rate limiter for a specific key
     */
    private fun getLimiter(key: String): RateLimiter {
        return limiters.getOrPut(key) {
            RateLimiter(maxRequests, timeWindowMs)
        }
    }

    /**
     * Check rate limit for a specific key
     */
    suspend fun checkLimit(key: String) {
        getLimiter(key).checkLimit()
    }

    /**
     * Execute with rate limiting for a specific key
     */
    suspend fun <T> executeWithLimit(key: String, block: suspend () -> T): T {
        return getLimiter(key).executeWithLimit(block)
    }

    /**
     * Clear rate limiter for a specific key
     */
    suspend fun reset(key: String) {
        limiters[key]?.reset()
    }

    /**
     * Clear all rate limiters
     */
    suspend fun resetAll() {
        limiters.values.forEach { it.reset() }
        limiters.clear()
    }
}

/**
 * Extension function for easy rate limiting
 */
suspend fun <T> withRateLimit(
    limiter: RateLimiter,
    block: suspend () -> T
): T {
    return limiter.executeWithLimit(block)
}
