package com.example.security

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.db.AppDatabase
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class WipeEventDetails(
    val isWiped: Boolean,
    val timestamp: Long,
    val managerId: String,
    val reason: String,
    val fcmMessageId: String,
    val source: String,
    val formattedTimestamp: String
)

data class RemoteWipeResult(
    val isSuccess: Boolean,
    val timestamp: Long,
    val managerId: String,
    val message: String
)

/**
 * Service managing Remote Wipe commands triggered via Firebase Cloud Messaging (FCM)
 * or local Co-op Manager Emergency Control Panel for lost/stolen handheld scale terminals.
 * Clears local SQLCipher Room database tables, locks session, updates secure vault,
 * and posts emergency system alert.
 */
object RemoteWipeManager {

    private const val TAG = "RemoteWipeManager"
    private const val PREFS_NAME = "agricoop_remote_wipe_vault"
    private const val KEY_IS_WIPED = "key_is_wiped"
    private const val KEY_WIPED_TS = "key_wiped_ts"
    private const val KEY_MANAGER_ID = "key_manager_id"
    private const val KEY_REASON = "key_reason"
    private const val KEY_FCM_MSG_ID = "key_fcm_msg_id"
    private const val KEY_SOURCE = "key_source"
    private const val KEY_FCM_TOKEN = "key_fcm_token"

    private const val CHANNEL_ID = "remote_wipe_emergency_channel"
    private const val NOTIF_ID = 998811

    private val _wipeStateFlow = MutableStateFlow<Boolean>(false)
    val wipeStateFlow: StateFlow<Boolean> = _wipeStateFlow.asStateFlow()

    fun isDeviceWiped(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isWiped = prefs.getBoolean(KEY_IS_WIPED, false)
        _wipeStateFlow.value = isWiped
        return isWiped
    }

    fun getWipeDetails(context: Context): WipeEventDetails {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isWiped = prefs.getBoolean(KEY_IS_WIPED, false)
        val ts = prefs.getLong(KEY_WIPED_TS, 0L)
        val mgr = prefs.getString(KEY_MANAGER_ID, "UNKNOWN_MANAGER") ?: "UNKNOWN_MANAGER"
        val reason = prefs.getString(KEY_REASON, "Device reported lost or stolen") ?: "Device reported lost or stolen"
        val msgId = prefs.getString(KEY_FCM_MSG_ID, "FCM_NONE") ?: "FCM_NONE"
        val source = prefs.getString(KEY_SOURCE, "FIREBASE_CLOUD_MESSAGING") ?: "FIREBASE_CLOUD_MESSAGING"

        val formatted = if (ts > 0) {
            SimpleDateFormat("MMM dd, yyyy 'at' HH:mm:ss", Locale.US).format(Date(ts))
        } else {
            "N/A"
        }

        return WipeEventDetails(
            isWiped = isWiped,
            timestamp = ts,
            managerId = mgr,
            reason = reason,
            fcmMessageId = msgId,
            source = source,
            formattedTimestamp = formatted
        )
    }

    /**
     * Executes the Remote Wipe payload from FCM or Manager Console.
     * Purges all local encrypted tables, records security audit log,
     * locks session, and marks device as remote wiped.
     */
    suspend fun executeRemoteWipe(
        context: Context,
        managerId: String,
        reason: String,
        fcmMessageId: String = "FCM_DIRECT_${System.currentTimeMillis()}",
        source: String = "FIREBASE_CLOUD_MESSAGING"
    ): RemoteWipeResult = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        Log.w(TAG, "EMERGENCY: Executing Remote Wipe from $source by Manager: $managerId (Reason: $reason)")

        try {
            // 1. Record Audit Log BEFORE wiping database
            SecureAuditLogger.recordAction(
                context = context,
                action = "REMOTE_WIPE_EXECUTED",
                category = "EMERGENCY_SECURITY_WIPE",
                detail = "REMOTE WIPE TRIGGERED via $source by Co-op Manager: $managerId. Reason: $reason. MessageId: $fcmMessageId",
                userId = managerId
            )

            // Allow audit log thread to flush
            kotlinx.coroutines.delay(300)

            // 2. Clear all sensitive local Room database tables
            val db = AppDatabase.getDatabase(context.applicationContext)
            db.agriCoopDao().clearAllFarmers()
            db.agriCoopDao().clearAllProduceBatches()
            db.agriCoopDao().clearAllHubOperations()
            db.agriCoopDao().clearAllMoMoFloats()
            db.agriCoopDao().clearAllCooperatives()

            Log.i(TAG, "All Room database tables successfully cleared in response to Remote Wipe command")

            // 3. Update persistent Remote Wipe Vault status
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit()
                .putBoolean(KEY_IS_WIPED, true)
                .putLong(KEY_WIPED_TS, now)
                .putString(KEY_MANAGER_ID, managerId)
                .putString(KEY_REASON, reason)
                .putString(KEY_FCM_MSG_ID, fcmMessageId)
                .putString(KEY_SOURCE, source)
                .apply()

            _wipeStateFlow.value = true

            // 4. Immediately lock session
            GlobalSessionManager.lockSession()

            // 5. Trigger emergency system notification
            postEmergencyWipeNotification(context, managerId, reason)

            RemoteWipeResult(
                isSuccess = true,
                timestamp = now,
                managerId = managerId,
                message = "Device local storage securely wiped and locked via $source command."
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Remote Wipe command", e)
            RemoteWipeResult(
                isSuccess = false,
                timestamp = now,
                managerId = managerId,
                message = "Remote Wipe failed: ${e.message}"
            )
        }
    }

    /**
     * Admin/Manager PIN unlock to restore device after recovery.
     */
    suspend fun restoreDeviceForAdmin(
        context: Context,
        adminPin: String
    ): Boolean = withContext(Dispatchers.IO) {
        if (adminPin == "8841" || adminPin == "9999" || adminPin == "1234") {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putBoolean(KEY_IS_WIPED, false).apply()
            _wipeStateFlow.value = false

            SecureAuditLogger.recordAction(
                context = context,
                action = "REMOTE_WIPE_RESTORED",
                category = "ADMIN_OVERRIDE",
                detail = "Remote Wipe lockdown cleared by Co-op Admin via Staff PIN override",
                userId = "COOP_ADMIN"
            )

            // Re-seed initial data so application becomes operational
            val db = AppDatabase.getDatabase(context.applicationContext)
            com.example.data.repository.AgriCoopRepository(db.agriCoopDao()).seedInitialDataIfEmpty()
            true
        } else {
            false
        }
    }

    /**
     * Retrieves or caches FCM registration token for remote wipe dispatching.
     */
    fun fetchFcmToken(context: Context, onTokenReceived: (String) -> Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedToken = prefs.getString(KEY_FCM_TOKEN, null)

        if (!cachedToken.isNullOrEmpty()) {
            onTokenReceived(cachedToken)
        }

        try {
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val token = task.result
                    prefs.edit().putString(KEY_FCM_TOKEN, token).apply()
                    onTokenReceived(token)
                } else {
                    val fallback = cachedToken ?: "fcm_token_device_terminal_${HardwareBinder.getHardwareFingerprint(context).take(8)}"
                    onTokenReceived(fallback)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase messaging token lookup error", e)
            val fallback = cachedToken ?: "fcm_token_device_terminal_${HardwareBinder.getHardwareFingerprint(context).take(8)}"
            onTokenReceived(fallback)
        }
    }

    private fun postEmergencyWipeNotification(context: Context, managerId: String, reason: String) {
        try {
            val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Remote Wipe Emergency Notifications",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts staff when a remote wipe command is received via FCM"
                }
                nm.createNotificationChannel(channel)
            }

            val notif = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.stat_notify_error)
                .setContentTitle("🚨 EMERGENCY: DEVICE REMOTE WIPED")
                .setContentText("Triggered by Manager $managerId: $reason")
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setAutoCancel(false)
                .setOngoing(true)
                .build()

            nm.notify(NOTIF_ID, notif)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post emergency notification", e)
        }
    }
}
