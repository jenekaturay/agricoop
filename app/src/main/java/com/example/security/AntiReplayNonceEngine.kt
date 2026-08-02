package com.example.security

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/**
 * Hardened Security Architecture - Layer 4: Anti-Replay Nonce Engine
 * Injects 128-bit UUID v4 nonces and monotonic millisecond timestamps into offline sync payloads to prevent replay attacks.
 */
object AntiReplayNonceEngine {

    // Thread-safe in-memory cache of seen nonces
    private val seenNonces = ConcurrentHashMap<String, Long>()

    // Accept payloads up to 24 hours old
    private const val MAX_NONCE_AGE_MS = 24 * 3600 * 1000L

    data class SecuredPayload<T>(
        val nonce: String,
        val timestamp: Long,
        val data: T
    )

    /**
     * Wraps any raw payload with a cryptographically unique UUID v4 nonce and millisecond timestamp.
     */
    fun <T> wrapPayloadWithNonce(rawPayload: T): SecuredPayload<T> {
        val nonce = UUID.randomUUID().toString()
        val timestamp = System.currentTimeMillis()
        return SecuredPayload(
            nonce = nonce,
            timestamp = timestamp,
            data = rawPayload
        )
    }

    /**
     * Validates incoming nonces against replay attacks and timestamp freshness.
     * Returns true if valid and unique; false if replayed or expired.
     */
    fun validateNonce(nonce: String, timestamp: Long): NonceValidationResult {
        val currentTime = System.currentTimeMillis()

        // 1. Check if timestamp is too far in the past or future
        if (kotlin.math.abs(currentTime - timestamp) > MAX_NONCE_AGE_MS) {
            return NonceValidationResult.EXPIRED
        }

        // 2. Check if nonce has been used previously (Replay Attack)
        if (seenNonces.containsKey(nonce)) {
            return NonceValidationResult.REPLAY_DETECTED
        }

        // 3. Clean up stale nonces older than MAX_NONCE_AGE_MS
        cleanupStaleNonces(currentTime)

        // 4. Record nonce
        seenNonces[nonce] = timestamp
        return NonceValidationResult.VALID
    }

    private fun cleanupStaleNonces(currentTime: Long) {
        if (seenNonces.size > 5000) {
            val iterator = seenNonces.entries.iterator()
            while (iterator.hasNext()) {
                val entry = iterator.next()
                if (currentTime - entry.value > MAX_NONCE_AGE_MS) {
                    iterator.remove()
                }
            }
        }
    }

    fun getTrackedNonceCount(): Int = seenNonces.size

    enum class NonceValidationResult {
        VALID,
        REPLAY_DETECTED,
        EXPIRED
    }
}
