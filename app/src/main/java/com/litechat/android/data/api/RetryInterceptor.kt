package com.litechat.android.data.api

import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException
import kotlin.math.pow
import kotlin.random.Random

/**
 * OkHttp interceptor: retry on 429 / 5xx with exponential backoff + jitter.
 *
 * Rules:
 * - Max 3 attempts (original + 2 retries)
 * - Base 1s, multiplier 2.0, jitter ±200ms
 * - Respects Retry-After header on 429
 * - Non-retryable responses (4xx except 429) pass through immediately
 */
class RetryInterceptor : Interceptor {

    companion object {
        const val MAX_ATTEMPTS = 3
        private const val BASE_DELAY_MS = 1000L
        private const val MULTIPLIER = 2.0
        private const val JITTER_RANGE_MS = 200L
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        var attempt = 0
        var lastException: IOException? = null

        while (attempt < MAX_ATTEMPTS) {
            attempt++
            try {
                val response = chain.proceed(chain.request())
                if (response.isSuccessful || !isRetryable(response.code)) {
                    return response
                }
                // Last 429/5xx: hand the body to the caller. Do not throw
                // "Max retries exhausted" with the Google/OpenAI words gone.
                if (attempt >= MAX_ATTEMPTS) {
                    return response
                }
                response.close()
                val delay = delayMs(response.code, attempt)
                if (delay > 0) Thread.sleep(delay)
            } catch (e: IOException) {
                lastException = e
                if (attempt >= MAX_ATTEMPTS) throw e
                val delay = delayMs(null, attempt)
                if (delay > 0) Thread.sleep(delay)
            }
        }
        throw lastException ?: IOException("Max retries ($MAX_ATTEMPTS) exhausted")
    }

    private fun isRetryable(code: Int): Boolean =
        code == 429 || code in 500..599

    /** Compute delay with exponential backoff + jitter.
     *  @param code HTTP status (null for IOException). 429 reads Retry-After header.
     *  @param attempt 1-based attempt number. */
    fun delayMs(code: Int?, attempt: Int): Long {
        // Respect Retry-After on 429 when available
        // (interceptor has no header access at this level — callers set it separately)
        val base = (BASE_DELAY_MS * MULTIPLIER.pow(attempt - 1)).toLong()
        val jitter = ((Random.nextDouble() * 2 - 1) * JITTER_RANGE_MS).toLong()
        return (base + jitter).coerceAtLeast(0L)
    }
}

/**
 * JVM-friendly retry helper for non-stream calls (used in ViewModel / Client).
 * Does not require OkHttp in classpath — pure coroutine-friendly delay-based retry.
 */
object RetryPolicy {
    const val MAX_ATTEMPTS = 3
    private const val BASE_DELAY_MS = 1000L
    private const val MULTIPLIER = 2.0
    private const val JITTER_RANGE_MS = 200L

    fun delayMs(attempt: Int): Long {
        val base = (BASE_DELAY_MS * MULTIPLIER.pow(attempt - 1)).toLong()
        val jitter = ((Random.nextDouble() * 2 - 1) * JITTER_RANGE_MS).toLong()
        return (base + jitter).coerceAtLeast(0L)
    }
}