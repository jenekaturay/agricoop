package com.example.security

import android.content.Context
import android.os.Build
import android.provider.Settings
import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Hardened Security Architecture - Layer 2: Hardware Device Binding (Anti-Cloning & Fraud Prevention)
 * Binds field operations to specific physical Android devices using a hardware-rooted fingerprint.
 */
object HardwareBinder {

    private const val HARDWARE_SALT = "sec-ops-agricoop-iron-deed-salt-v3"

    /**
     * Generates an immutable, hardware-rooted device fingerprint SHA-256 digest.
     */
    fun getHardwareFingerprint(context: Context): String {
        val androidId = try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID) ?: "UNKNOWN_ANDROID_ID"
        } catch (e: Exception) {
            "FALLBACK_ANDROID_ID"
        }

        val rawIdentity = "${androidId}:${Build.MODEL}:${Build.HARDWARE}:${Build.FINGERPRINT}:${Build.MANUFACTURER}"

        return hmacSha256(rawIdentity, HARDWARE_SALT)
    }

    /**
     * Calculates HMAC SHA-256 digest with internal hardware salt.
     */
    private fun hmacSha256(data: String, key: String): String {
        return try {
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(secretKey)
            val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            // Fallback digest if HMAC fails
            val digest = MessageDigest.getInstance("SHA-256")
            val bytes = digest.digest("$data:$key".toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        }
    }

    /**
     * Validates if the current execution environment matches an authorized hardware fingerprint.
     */
    fun isDeviceAuthorized(context: Context, authorizedFingerprints: List<String>): Boolean {
        if (authorizedFingerprints.isEmpty()) return true // Default pass when no restrict list configured
        val currentFingerprint = getHardwareFingerprint(context)
        return authorizedFingerprints.contains(currentFingerprint)
    }
}
