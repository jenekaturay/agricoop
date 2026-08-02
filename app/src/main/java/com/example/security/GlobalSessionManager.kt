package com.example.security

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

data class BiometricAuthEvent(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val status: String, // "SUCCESS", "FAILED", "PIN_FALLBACK", "AUTO_LOCKED"
    val method: String, // "Biometric Scan", "Staff Security PIN", "Background Auto-Lock"
    val detail: String
)

/**
 * Global Session Manager for Co-op Staff Security Architecture.
 * Observes application lifecycle events (ProcessLifecycleOwner) to enforce
 * automatic background lock policy when the app is backgrounded beyond the threshold (default 5 mins).
 */
object GlobalSessionManager : DefaultLifecycleObserver {

    private val _isSessionLocked = MutableStateFlow(false)
    val isSessionLocked: StateFlow<Boolean> = _isSessionLocked.asStateFlow()

    // Default 5 minutes auto-lock timeout (-1 = Off, 0 = Immediate, 1 = 1m, 5 = 5m, 15 = 15m)
    private val _autoLockTimeoutMinutes = MutableStateFlow(5)
    val autoLockTimeoutMinutes: StateFlow<Int> = _autoLockTimeoutMinutes.asStateFlow()

    private val _lastLoginTimestamp = MutableStateFlow<Long>(System.currentTimeMillis())
    val lastLoginTimestamp: StateFlow<Long> = _lastLoginTimestamp.asStateFlow()

    private val _biometricEvents = MutableStateFlow<List<BiometricAuthEvent>>(
        listOf(
            BiometricAuthEvent(
                timestamp = System.currentTimeMillis() - (8 * 60 * 1000L),
                status = "SUCCESS",
                method = "Biometric Scan",
                detail = "Biometric fingerprint match verified"
            ),
            BiometricAuthEvent(
                timestamp = System.currentTimeMillis() - (42 * 60 * 1000L),
                status = "PIN_FALLBACK",
                method = "Staff Security PIN",
                detail = "Field agent PIN authorization verified"
            )
        )
    )
    val biometricEvents: StateFlow<List<BiometricAuthEvent>> = _biometricEvents.asStateFlow()

    private var lastBackgroundTimestamp = 0L

    /**
     * Initializes ProcessLifecycleOwner observer to track global app foreground / background transitions.
     */
    fun init() {
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
    }

    override fun onStop(owner: LifecycleOwner) {
        super.onStop(owner)
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    override fun onStart(owner: LifecycleOwner) {
        super.onStart(owner)
        checkSessionTimeout()
    }

    /**
     * Verifies if background duration exceeds configured timeout threshold and triggers re-authentication.
     */
    fun checkSessionTimeout() {
        val timeoutMins = _autoLockTimeoutMinutes.value
        if (timeoutMins >= 0 && lastBackgroundTimestamp > 0L && !_isSessionLocked.value) {
            val elapsedMillis = System.currentTimeMillis() - lastBackgroundTimestamp
            val thresholdMillis = timeoutMins * 60 * 1000L
            if (elapsedMillis >= thresholdMillis) {
                _isSessionLocked.value = true
                recordBiometricEvent(
                    status = "AUTO_LOCKED",
                    method = "Background Timeout",
                    detail = "App locked after $timeoutMins min background inactivity"
                )
            }
        }
    }

    fun recordLogin(timestamp: Long = System.currentTimeMillis()) {
        _lastLoginTimestamp.value = timestamp
        recordBiometricEvent(
            status = "SUCCESS",
            method = "Staff Login",
            detail = "Co-op staff account authenticated"
        )
    }

    fun recordBiometricEvent(status: String, method: String, detail: String, context: android.content.Context? = null) {
        val event = BiometricAuthEvent(
            timestamp = System.currentTimeMillis(),
            status = status,
            method = method,
            detail = detail
        )
        _biometricEvents.value = (listOf(event) + _biometricEvents.value).take(20)

        context?.let { ctx ->
            SecureAuditLogger.recordAction(
                context = ctx,
                action = "BIOMETRIC_$status",
                category = "AUTHENTICATION",
                detail = "$method: $detail"
            )
        }
    }

    fun lockSession() {
        _isSessionLocked.value = true
        recordBiometricEvent(
            status = "AUTO_LOCKED",
            method = "Manual Lock",
            detail = "Session manually locked by user"
        )
    }

    fun unlockSession() {
        _isSessionLocked.value = false
        lastBackgroundTimestamp = System.currentTimeMillis()
    }

    fun setAutoLockTimeout(minutes: Int) {
        _autoLockTimeoutMinutes.value = minutes
    }
}

