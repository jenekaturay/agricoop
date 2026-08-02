package com.example.worker

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

object SyncWorkScheduler {

    /**
     * Schedules both periodic and constraint-driven one-time background auto-sync
     * using Android WorkManager. Triggers whenever a stable network connection is detected.
     */
    fun scheduleAutoSync(context: Context) {
        val workManager = WorkManager.getInstance(context)

        // Constraints requiring a connected network (Wi-Fi or Cellular)
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // 1. Periodic Work Request (Runs every 15 minutes when connected)
        val periodicSyncRequest = PeriodicWorkRequestBuilder<AutoSyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            AutoSyncWorker.WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )

        // 2. Immediate One-Time Request triggered on network reconnect
        val oneTimeSyncRequest = OneTimeWorkRequestBuilder<AutoSyncWorker>()
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniqueWork(
            AutoSyncWorker.WORK_NAME_ONETIME,
            ExistingWorkPolicy.REPLACE,
            oneTimeSyncRequest
        )

        Log.d("SyncWorkScheduler", "Enqueued WorkManager background auto-sync with CONNECTED network constraint.")
    }

    /**
     * Forces an immediate WorkManager job regardless of network constraints for manual test triggering.
     */
    fun triggerImmediateWorker(context: Context) {
        val workManager = WorkManager.getInstance(context)
        val unconstrainedRequest = OneTimeWorkRequestBuilder<AutoSyncWorker>()
            .build()

        workManager.enqueueUniqueWork(
            "agricoop_manual_trigger_work",
            ExistingWorkPolicy.REPLACE,
            unconstrainedRequest
        )

        Log.d("SyncWorkScheduler", "Enqueued immediate unconstrained WorkManager task.")
    }
}
