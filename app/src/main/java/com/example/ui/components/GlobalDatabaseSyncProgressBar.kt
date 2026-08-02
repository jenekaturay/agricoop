package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.BandwidthCondition
import com.example.service.SyncStrategyType

@Composable
fun GlobalDatabaseSyncProgressBar(
    isOnline: Boolean,
    isSyncing: Boolean,
    syncProgress: Float,
    syncCompletedCount: Int,
    syncTotalCount: Int,
    unsyncedCount: Int,
    totalRecordsCount: Int,
    syncStatusMessage: String,
    lastSyncedTimeText: String,
    bandwidthCondition: BandwidthCondition = BandwidthCondition.CELLULAR_LOW_BANDWIDTH,
    syncStrategy: SyncStrategyType = SyncStrategyType.METADATA_PACKET_PRIORITY,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = when {
            isSyncing -> syncProgress.coerceIn(0.02f, 1f)
            unsyncedCount == 0 -> 1f
            totalRecordsCount > 0 -> ((totalRecordsCount - unsyncedCount).toFloat() / totalRecordsCount.toFloat()).coerceIn(0.05f, 0.95f)
            else -> 0f
        },
        label = "syncProgressAnimation"
    )

    val percentageInt = when {
        isSyncing -> (animatedProgress * 100).toInt().coerceIn(1, 100)
        unsyncedCount == 0 -> 100
        totalRecordsCount > 0 -> (((totalRecordsCount - unsyncedCount).toDouble() / totalRecordsCount) * 100).toInt().coerceIn(0, 99)
        else -> 0
    }

    val containerBgColor by animateColorAsState(
        targetValue = when {
            isSyncing -> Color(0xFF01579B) // Deep Sky Blue when actively syncing
            !isOnline -> Color(0xFF37474F) // Slate Charcoal when offline
            unsyncedCount > 0 -> Color(0xFF1B5E20) // Deep Emerald with pending
            else -> Color(0xFF0B3D2E) // Master Brand Forest Green
        },
        label = "syncContainerBg"
    )

    Surface(
        color = containerBgColor,
        contentColor = Color.White,
        modifier = modifier
            .fillMaxWidth()
            .testTag("global_sync_progress_bar_container")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            // Top Status Line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = when {
                            isSyncing -> Color(0xFFFFD54F)
                            unsyncedCount > 0 -> Color(0xFFFFB74D)
                            else -> Color(0xFF81C784)
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when {
                                    isSyncing -> Icons.Default.CloudSync
                                    !isOnline -> Icons.Default.WifiOff
                                    unsyncedCount > 0 -> Icons.Default.CloudUpload
                                    else -> Icons.Default.CloudDone
                                },
                                contentDescription = "Sync Status",
                                tint = Color(0xFF0B3D2E),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    isSyncing -> "Cloud DB Uploading..."
                                    !isOnline -> "Room DB Offline Queue"
                                    unsyncedCount > 0 -> "Room DB Pending Cloud Sync"
                                    else -> "Room DB Synced to Cloud"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            // Percentage Badge
                            Surface(
                                color = when {
                                    isSyncing -> Color(0xFFFFD54F)
                                    unsyncedCount > 0 -> Color(0x33FFD54F)
                                    else -> Color(0x3381C784)
                                },
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "$percentageInt%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSyncing) Color(0xFF0B3D2E) else Color.White,
                                    modifier = Modifier
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                        .testTag("global_sync_percentage_badge")
                                )
                            }
                        }

                        Text(
                            text = if (isSyncing) syncStatusMessage
                            else if (unsyncedCount > 0) "$unsyncedCount pending Room DB records queued ($lastSyncedTimeText)"
                            else "All local Room DB records updated in PostGIS Cloud ($lastSyncedTimeText)",
                            fontSize = 10.sp,
                            color = Color(0xFFE0E0E0),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Record Counter or Sync Now Action Button
                if (unsyncedCount > 0 && !isSyncing) {
                    Button(
                        onClick = onTriggerSync,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color(0xFF0B3D2E)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("global_sync_now_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Now",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (isSyncing) {
                    Surface(
                        color = Color(0x33FFFFFF),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "$syncCompletedCount / $syncTotalCount",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                } else {
                    Surface(
                        color = Color(0x2281C784),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Synced",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF81C784)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Global Real-Time Linear Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x33FFFFFF))
            ) {
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .testTag("global_realtime_linear_progress_indicator"),
                    color = when {
                        isSyncing -> Color(0xFFFFD54F) // Bright Gold/Amber during sync
                        unsyncedCount > 0 -> Color(0xFFFFB74D) // Light Orange when items pending
                        else -> Color(0xFF81C784) // Green when 100% complete
                    },
                    trackColor = Color.Transparent
                )
            }
        }
    }
}
