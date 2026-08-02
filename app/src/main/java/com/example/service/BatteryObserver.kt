package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity

object BatteryObserver {

    private const val CHANNEL_ID = "low_battery_warning_channel"
    private const val CHANNEL_NAME = "Critical Battery Alerts"
    private const val CHANNEL_DESC = "Alerts for low battery during active tuber processing sessions"

    private const val NOTIFICATION_ID = 8815

    private var hasNotifiedThisLowCycle = false

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Reads current battery percentage from system battery intent.
     */
    fun getCurrentBatteryLevel(context: Context): Int {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val level: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        return if (level >= 0 && scale > 0) {
            (level * 100 / scale)
        } else {
            100 // fallback if battery status unavailable
        }
    }

    /**
     * Checks if device is currently plugged in and charging.
     */
    fun isCharging(context: Context): Boolean {
        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { filter ->
            context.registerReceiver(null, filter)
        }
        val status: Int = batteryStatus?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    /**
     * Evaluates battery percentage and sends notification if battery < 15%.
     */
    fun evaluateAndNotifyLowBattery(context: Context, batteryLevel: Int, isProcessingActive: Boolean = true): Boolean {
        createNotificationChannel(context)

        val isLow = batteryLevel in 1..14

        if (isLow && isProcessingActive && !hasNotifiedThisLowCycle) {
            hasNotifiedThisLowCycle = true
            sendLowBatteryNotification(context, batteryLevel)
        } else if (batteryLevel >= 15) {
            hasNotifiedThisLowCycle = false
        }

        return isLow
    }

    private fun sendLowBatteryNotification(context: Context, batteryLevel: Int) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning)
            .setContentTitle("🪫 Critical Battery Warning ($batteryLevel%)")
            .setContentText("Device battery dropped below 15%! Save active tuber weighing batch to prevent data loss.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "Battery level is now $batteryLevel%. Active tuber processing and digital scale data collection are at risk of unexpected shutdown. Please connect to a solar charger or save your current batch to Room SQLite DB immediately!"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}
