package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SignalCellular4Bar
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material.icons.filled.Bolt
import com.example.service.BandwidthCondition
import com.example.service.SyncStrategyType

@Composable
fun NetworkSyncBanner(
    isOnline: Boolean,
    isSyncing: Boolean,
    unsyncedCount: Int,
    syncStatusMessage: String,
    lastSyncedTimeText: String,
    bandwidthCondition: BandwidthCondition = BandwidthCondition.CELLULAR_LOW_BANDWIDTH,
    syncStrategy: SyncStrategyType = SyncStrategyType.METADATA_PACKET_PRIORITY,
    onToggleNetwork: () -> Unit,
    onToggleBandwidthCondition: () -> Unit = {},
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    var bannerDismissed by remember { mutableStateOf(false) }

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSyncing -> Color(0xFF0277BD) // Blue when actively syncing
            !isOnline -> Color(0xFFD84315) // Deep Amber/Red when Offline
            unsyncedCount > 0 -> Color(0xFFF57F17) // Orange when online with pending Room records
            else -> Color(0xFF2E7D32) // Forest green when Online & Synced
        },
        label = "bannerBgColor"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .testTag("network_sync_banner")
    ) {
        if (isSyncing) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp),
                color = Color(0xFFFFD54F),
                trackColor = Color(0x33FFFFFF)
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = Color(0x33FFFFFF),
                    shape = CircleShape,
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        val icon = when {
                            isSyncing -> Icons.Default.CloudSync
                            !isOnline -> Icons.Default.WifiOff
                            unsyncedCount > 0 -> Icons.Default.CloudOff
                            else -> Icons.Default.CloudDone
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Network Status",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    val headline = when {
                        isSyncing -> if (syncStrategy == SyncStrategyType.METADATA_PACKET_PRIORITY) "Pushing Light Metadata Packets (~180B)..." else "Pushing Full DB Snapshot..."
                        !isOnline -> "Offline Mode (${unsyncedCount} Queued in Room DB)"
                        unsyncedCount > 0 -> if (syncStrategy == SyncStrategyType.METADATA_PACKET_PRIORITY) "2G/3G Cellular • ${unsyncedCount} Metadata Packets Ready" else "Wi-Fi/4G • ${unsyncedCount} Records Ready"
                        else -> "Online • All Room Records Synced"
                    }

                    Text(
                        text = headline,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isSyncing) syncStatusMessage else if (!isOnline) "Data saved locally to Room SQLite" else "Last sync: $lastSyncedTimeText",
                            color = Color(0xFFE0E0E0),
                            fontSize = 10.sp
                        )
                        if (isOnline && syncStrategy == SyncStrategyType.METADATA_PACKET_PRIORITY) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(color = Color(0xFFFFD54F), shape = RoundedCornerShape(4.dp)) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.Black, modifier = Modifier.size(10.dp))
                                    Text("95% Data Saved", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                // Network Mode / Bandwidth Simulator Toggle Chip
                Surface(
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clickable { onToggleBandwidthCondition() }
                        .testTag("network_toggle_chip")
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                bannerDismissed = false
                                onToggleNetwork()
                            },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.Wifi else Icons.Default.WifiOff,
                                contentDescription = "Toggle Network",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (!isOnline) "OFFLINE" else if (bandwidthCondition == BandwidthCondition.CELLULAR_LOW_BANDWIDTH) "2G/3G" else "4G/Wi-Fi",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Sync Action Button
                if (unsyncedCount > 0 && !isSyncing) {
                    Button(
                        onClick = onTriggerSync,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color(0xFF0B3D2E)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("sync_now_banner_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync",
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Sync Now", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Persistent Warning Banner / Toast when Lack of Connectivity is detected or when data is queued locally
        AnimatedVisibility(
            visible = !isOnline && !bannerDismissed,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = Color(0xFFFFF3E0),
                contentColor = Color(0xFF3E2723),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp)
                    .border(1.dp, Color(0xFFFFB74D), RoundedCornerShape(10.dp))
                    .testTag("room_db_offline_warning_toast")
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE65100),
                            shape = CircleShape,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "No Network Warning",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Lack of Connectivity Detected",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBF360C)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFFE0B2),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "$unsyncedCount Queued in Room DB",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFE65100),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "All new weighings and farmer entries are safely stored in Room SQLite DB on this device. They will automatically sync to PostGIS Cloud once network connects.",
                                fontSize = 10.sp,
                                color = Color(0xFF5D4037),
                                lineHeight = 13.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = { bannerDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Banner",
                            tint = Color(0xFF8D6E63),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryLowAlertBanner(
    batteryLevel: Int,
    isLowBattery: Boolean,
    isCharging: Boolean,
    dismissed: Boolean,
    onDismiss: () -> Unit,
    onSimulateToggle: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = (isLowBattery || batteryLevel < 15) && !dismissed && !isCharging,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier
    ) {
        Surface(
            color = Color(0xFFFFEBEE),
            contentColor = Color(0xFFB71C1C),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .border(1.5.dp, Color(0xFFE53935), RoundedCornerShape(12.dp))
                .testTag("battery_low_warning_banner")
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFD32F2F),
                            shape = CircleShape,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.BatteryAlert,
                                    contentDescription = "Critical Battery Warning",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🪫 Critical Battery Observer ($batteryLevel%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB71C1C)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFFFCDD2),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "<15% THRESHOLD",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFC62828),
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = "Device power dropped below 15%! Save active tuber processing weighing data immediately to prevent unexpected shutdown and loss.",
                                fontSize = 10.sp,
                                color = Color(0xFF5C0000),
                                lineHeight = 13.sp
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss Battery Banner",
                            tint = Color(0xFFB71C1C),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Connect to solar charger or power bank",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFC62828)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onSimulateToggle(85) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(26.dp)
                                .testTag("sim_battery_charged_btn")
                        ) {
                            Text("Simulate 85% Charged", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}


