package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.db.entities.ProduceBatchEntity

object PendingBatchNotifier {

    private const val CHANNEL_ID = "pending_batches_overdue_channel"
    private const val CHANNEL_NAME = "Overdue Pending Tuber Batches"
    private const val CHANNEL_DESC = "Notifications for tuber batches pending processing for more than 24 hours"

    private val notifiedBatchIds = mutableSetOf<String>()

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

    fun checkAndNotifyPendingBatches(context: Context, batches: List<ProduceBatchEntity>) {
        createNotificationChannel(context)

        val now = System.currentTimeMillis()
        val twentyFourHoursMs = 24 * 60 * 60 * 1000L

        val overdueBatches = batches.filter { batch ->
            batch.payoutStatus == "PENDING" && (now - batch.timestamp) >= twentyFourHoursMs
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        for (batch in overdueBatches) {
            val hoursPending = ((now - batch.timestamp) / (1000 * 60 * 60)).coerceAtLeast(24)

            // Prevent duplicate notification spam for the same batch if already notified
            if (!notifiedBatchIds.contains(batch.id)) {
                notifiedBatchIds.add(batch.id)

                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                val pendingIntent = PendingIntent.getActivity(
                    context,
                    batch.id.hashCode(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )

                val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.stat_notify_error)
                    .setContentTitle("⚠️ Overdue Pending Batch (>24h)")
                    .setContentText("Batch ${batch.batchCode} (${batch.farmerName} - ${batch.weightKg}kg) has been pending for $hoursPending hours!")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText(
                                "Batch ${batch.batchCode} for farmer ${batch.farmerName} (${batch.cropType} - ${batch.weightKg}kg) " +
                                        "has been in PENDING status for $hoursPending hours ($%.2f LRD). Immediate processing required!".format(batch.totalPayoutLrd)
                            )
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setCategory(NotificationCompat.CATEGORY_ALARM)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setDefaults(NotificationCompat.DEFAULT_ALL)
                    .build()

                notificationManager.notify(batch.id.hashCode(), notification)
            }
        }
    }

    fun resetNotifiedBatches() {
        notifiedBatchIds.clear()
    }
}
