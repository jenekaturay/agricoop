package com.example.service

import android.content.Context
import android.util.Log
import com.example.security.RemoteWipeManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Firebase Cloud Messaging (FCM) Service for receiving high-priority remote push commands
 * including Co-op Manager Remote Wipe signals for lost or stolen handheld devices.
 */
class CoopFirebaseMessagingService : FirebaseMessagingService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.i(TAG, "New FCM Token generated: $token")
        val prefs = applicationContext.getSharedPreferences("agricoop_remote_wipe_vault", Context.MODE_PRIVATE)
        prefs.edit().putString("key_fcm_token", token).apply()
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.i(TAG, "Received FCM push payload from sender: ${remoteMessage.from}")

        val data = remoteMessage.data
        val action = data["action"] ?: data["command"] ?: data["type"]

        if (action == "REMOTE_WIPE" || action == "WIPE_DATA" || action == "EMERGENCY_CLEAR") {
            val managerId = data["manager_id"] ?: data["user_id"] ?: "COOP_MGR_THERESA"
            val reason = data["reason"] ?: "Device reported lost or stolen in rural sector"
            val msgId = remoteMessage.messageId ?: "FCM_MSG_${System.currentTimeMillis()}"

            Log.w(TAG, "CRITICAL: Remote Wipe payload received via FCM! Dispatching data clear command...")

            serviceScope.launch {
                RemoteWipeManager.executeRemoteWipe(
                    context = applicationContext,
                    managerId = managerId,
                    reason = reason,
                    fcmMessageId = msgId,
                    source = "FCM_REMOTE_PUSH"
                )
            }
        }
    }

    companion object {
        private const val TAG = "CoopFcmService"
    }
}
