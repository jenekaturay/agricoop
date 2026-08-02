package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CompareArrows
import androidx.compose.material.icons.filled.DataUsage
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedCard
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.service.BandwidthCondition
import com.example.service.SmsPayoutTemplateService
import com.example.service.SyncLogEntry
import com.example.service.SyncPayloadMetrics
import com.example.service.SyncStrategyType
import com.example.ui.components.RoomCsvExportModal
import com.example.ui.components.UssdSmsPayoutModal

@Composable
fun OfflineSyncScreen(
    unsyncedBatches: List<ProduceBatchEntity>,
    allBatches: List<ProduceBatchEntity>,
    farmers: List<FarmerEntity> = emptyList(),
    cooperatives: List<CooperativeEntity> = emptyList(),
    hubs: List<HubOperationEntity> = emptyList(),
    momoFloats: List<MoMoFloatEntity>,
    isSyncing: Boolean,
    syncStatusMessage: String,
    encryptedSmsPreview: String,
    compressedJsonPreview: String,
    bandwidthCondition: BandwidthCondition = BandwidthCondition.CELLULAR_LOW_BANDWIDTH,
    syncStrategy: SyncStrategyType = SyncStrategyType.METADATA_PACKET_PRIORITY,
    syncMetrics: SyncPayloadMetrics? = null,
    syncLogHistory: List<SyncLogEntry> = emptyList(),
    onSelectBandwidthCondition: (BandwidthCondition) -> Unit = {},
    onSelectSyncStrategy: (SyncStrategyType) -> Unit = {},
    onPerformSync: () -> Unit,
    onTriggerMoMo: (String) -> Unit,
    onSelectBatchForQr: (ProduceBatchEntity) -> Unit
) {
    val pendingMoMoCount = allBatches.count { it.payoutStatus == "PENDING" }
    var selectedBatchForUssdModal by remember { mutableStateOf<ProduceBatchEntity?>(null) }
    var showCsvExportModal by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9F7))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Offline Sync & MoMo Liquidity",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B3D2E)
        )

        // Intelligent Adaptive Sync Strategy Controller Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3D2E)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sync_controller_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFFFD54F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Intelligent Adaptive Sync Engine",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Prioritizes small metadata packets over 2G/3G cellular links",
                                color = Color(0xFF80CBC4),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Badge(containerColor = Color(0x33FFFFFF), contentColor = Color.White) {
                        Text("${unsyncedBatches.size} Pending Batches", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Network Bandwidth Selector
                Text(
                    text = "1. Detected Device Connection Condition:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    BandwidthCondition.values().forEach { cond ->
                        val isSelected = bandwidthCondition == cond
                        Surface(
                            color = if (isSelected) Color(0xFFFFD54F) else Color(0x22FFFFFF),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onSelectBandwidthCondition(cond) }
                                .testTag("bandwidth_cond_${cond.name}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = if (cond == BandwidthCondition.CELLULAR_LOW_BANDWIDTH) Icons.Default.SignalCellularAlt else Icons.Default.Wifi,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.Black else Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = cond.networkType,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.Black else Color.White
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Strategy Mode Switcher
                Text(
                    text = "2. Active Payload Transmission Strategy:",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                SyncStrategyType.values().forEach { strat ->
                    val isSelected = syncStrategy == strat
                    Surface(
                        color = if (isSelected) Color(0xFF1B5E20) else Color(0x15FFFFFF),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFFFFD54F) else Color(0x33FFFFFF)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable { onSelectSyncStrategy(strat) }
                            .testTag("sync_strategy_${strat.name}")
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (strat == SyncStrategyType.METADATA_PACKET_PRIORITY) Icons.Default.Bolt else Icons.Default.Storage,
                                contentDescription = null,
                                tint = if (isSelected) Color(0xFFFFD54F) else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = strat.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Color.LightGray
                                )
                                Text(
                                    text = strat.description,
                                    fontSize = 10.sp,
                                    color = Color(0xFFB0BEC5),
                                    lineHeight = 12.sp
                                )
                            }
                            if (isSelected) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payload Comparison Gauge Card
                syncMetrics?.let { metrics ->
                    Surface(
                        color = Color(0x22000000),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.DataUsage, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Payload Byte Comparison", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                                if (metrics.savingsPercentage > 0) {
                                    Surface(color = Color(0xFF2E7D32), shape = RoundedCornerShape(6.dp)) {
                                        Text(
                                            text = "-${metrics.savingsPercentage}% Data Saved",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Active Transmission", fontSize = 9.sp, color = Color.LightGray)
                                    Text("${metrics.totalTransmittedBytes} Bytes", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFFFD54F))
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Full DB Snapshot Size", fontSize = 9.sp, color = Color.LightGray)
                                    Text("${metrics.totalFullSnapshotBytes} Bytes", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            val ratio = if (metrics.totalFullSnapshotBytes > 0) {
                                (metrics.totalTransmittedBytes.toFloat() / metrics.totalFullSnapshotBytes.toFloat()).coerceIn(0.02f, 1.0f)
                            } else 1.0f

                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp),
                                color = Color(0xFFFFD54F),
                                trackColor = Color(0x55FFFFFF)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (syncStrategy == SyncStrategyType.METADATA_PACKET_PRIORITY) {
                                    "⚡ Prioritizing: Batch Code, Farmer ID, Weight (kg), Payout Amount (LRD), Starch %, Checksum Hash. Deferred: Photos, scale logs, GPS telemetry."
                                } else {
                                    "📦 Full Snapshot: Pushing complete Room DB objects, Bluetooth scale telemetry, and audit logs."
                                },
                                fontSize = 10.sp,
                                color = Color(0xFFB2DFDB),
                                lineHeight = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = syncStatusMessage,
                    color = Color.LightGray,
                    fontSize = 11.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = onPerformSync,
                    enabled = !isSyncing && unsyncedBatches.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("trigger_offline_sync_button")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.Black,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transmitting Packets...", color = Color.Black, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = if (syncStrategy == SyncStrategyType.METADATA_PACKET_PRIORITY) Icons.Default.Bolt else Icons.Default.CloudSync,
                            contentDescription = null,
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (unsyncedBatches.isEmpty()) "All Room Records Synced to Cloud" else if (syncStrategy == SyncStrategyType.METADATA_PACKET_PRIORITY) "Sync ${unsyncedBatches.size} Metadata Packets Now" else "Push Full DB Snapshot (${unsyncedBatches.size} Batches)",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // WorkManager Background Auto-Sync Worker Card
        val context = LocalContext.current
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA5D6A7)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("workmanager_autosync_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CloudSync,
                            contentDescription = "WorkManager Auto-Sync",
                            tint = Color(0xFF1B5E20),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "WorkManager Background Worker",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF1B5E20)
                            )
                            Text(
                                text = "Auto-syncs pending records upon network detection",
                                fontSize = 11.sp,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Surface(
                        color = Color(0xFF2E7D32),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "CONNECTED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "🤖 Android WorkManager monitors device network status in the background. When stable network connectivity (Wi-Fi/4G) is detected, pending unsynced Room DB batches are automatically pushed to the PostGIS Cloud server.",
                    fontSize = 11.sp,
                    color = Color(0xFF333333),
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = {
                        com.example.worker.SyncWorkScheduler.triggerImmediateWorker(context)
                    },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1B5E20)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B5E20)),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(36.dp)
                        .testTag("trigger_workmanager_auto_sync_test_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.SettingsBackupRestore,
                        contentDescription = "Test AutoSyncWorker",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test WorkManager AutoSyncWorker Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Encrypted SMS Fallback & Compressed JSON Previews
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Code, contentDescription = null, tint = Color(0xFF0B3D2E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Low-Data Payload Previews",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0B3D2E)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text("Compressed JSON Stream (2G/3G/Wi-Fi):", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (compressedJsonPreview.isNotEmpty()) compressedJsonPreview else "{\"status\":\"idle\",\"unsynced_count\":${unsyncedBatches.size}}",
                        color = Color(0xFF4EC9B0),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text("Encrypted SMS Fallback Data String:", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = Color(0xFFF0F4F2),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (encryptedSmsPreview.isNotEmpty()) encryptedSmsPreview else "SMS://+231770001122?body=BATCH_001,420,35700",
                        color = Color(0xFF0B3D2E),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }
        }

        // Live Packet Sync Audit History Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("sync_audit_history_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.History, contentDescription = null, tint = Color(0xFF0B3D2E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Packet Sync Log History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0B3D2E)
                        )
                    }

                    Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                        Text("${syncLogHistory.size} Transmissions", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (syncLogHistory.isEmpty()) {
                    Surface(
                        color = Color(0xFFFAFAFA),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "No sync transmissions logged in this session yet. Tap 'Sync Metadata Packets Now' above to transmit lightweight payload packets.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                } else {
                    syncLogHistory.forEach { log ->
                        Surface(
                            color = Color(0xFFF6F9F7),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (log.strategyUsed == SyncStrategyType.METADATA_PACKET_PRIORITY) Icons.Default.Bolt else Icons.Default.CloudSync,
                                        contentDescription = null,
                                        tint = if (log.strategyUsed == SyncStrategyType.METADATA_PACKET_PRIORITY) Color(0xFF2E7D32) else Color(0xFF1565C0),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = log.statusText,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color(0xFF0B3D2E)
                                        )
                                        Text(
                                            text = "Strategy: ${log.strategyUsed.title} • ${log.timestampText}",
                                            fontSize = 9.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                if (log.bytesSaved > 0) {
                                    Surface(
                                        color = Color(0xFFE8F5E9),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${log.bytesSaved}B Saved",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF2E7D32),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Room SQLite Database CSV Audit Export Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("room_csv_export_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFF0B3D2E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Local Room DB Backup & Audit CSV",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0B3D2E)
                            )
                            Text(
                                text = "${allBatches.size} Batches • ${farmers.size} Farmers • ${cooperatives.size} Co-ops",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                            Text("🔒 SQLCipher 256-bit AES", modifier = Modifier.padding(4.dp), fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = Color(0xFFE3F2FD), contentColor = Color(0xFF1565C0)) {
                            Text("CSV Format", modifier = Modifier.padding(4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Export local Room database records to standardized CSV spreadsheets for manual storage backups, co-op audits, or financial reconciliation without internet.",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = { showCsvExportModal = true },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("open_csv_export_modal_button")
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export Room Records to CSV", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }

        // Regional Carrier Mobile Money Cash Float Status
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("momo_float_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = Color(0xFF0B3D2E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Carrier MoMo Cash-Out Float",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0B3D2E)
                        )
                    }

                    Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                        Text("Corporate Bulk Agreement", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                momoFloats.forEach { floatItem ->
                    MoMoFloatRow(floatItem = floatItem)
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray)
                }
            }
        }

        // Pending Mobile Money Dispatches List
        Text(
            text = "Pending Farmer Payout Dispatches ($pendingMoMoCount)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B3D2E)
        )

        allBatches.filter { it.payoutStatus == "PENDING" }.forEach { batch ->
            PendingMoMoBatchCard(
                batch = batch,
                onTriggerMoMo = { onTriggerMoMo(batch.id) },
                onOpenUssdModal = { selectedBatchForUssdModal = batch },
                onQrClick = { onSelectBatchForQr(batch) }
            )
        }

        selectedBatchForUssdModal?.let { batch ->
            UssdSmsPayoutModal(
                batch = batch,
                onDismiss = { selectedBatchForUssdModal = null },
                onPayoutTriggered = {
                    onTriggerMoMo(batch.id)
                    selectedBatchForUssdModal = null
                }
            )
        }

        if (showCsvExportModal) {
            RoomCsvExportModal(
                batches = allBatches,
                farmers = farmers,
                cooperatives = cooperatives,
                hubs = hubs,
                floats = momoFloats,
                onDismiss = { showCsvExportModal = false }
            )
        }
    }
}

@Composable
private fun MoMoFloatRow(floatItem: MoMoFloatEntity) {
    Column {
        Text(text = floatItem.hubLocation, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0B3D2E))
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Orange MoMo Float:", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "LRD $%.0f".format(floatItem.orangeMoMoFloatLrd), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "MTN MoMo Float:", fontSize = 11.sp, color = Color.Gray)
                Spacer(modifier = Modifier.width(4.dp))
                Text(text = "LRD $%.0f".format(floatItem.mtnMoMoFloatLrd), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1565C0))
            }
        }
    }
}

@Composable
private fun PendingMoMoBatchCard(
    batch: ProduceBatchEntity,
    onTriggerMoMo: () -> Unit,
    onOpenUssdModal: () -> Unit,
    onQrClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(onClick = onQrClick, modifier = Modifier.size(36.dp)) {
                        Icon(Icons.Default.QrCode, contentDescription = "QR", tint = Color(0xFF0B3D2E), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(text = batch.batchCode, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0B3D2E))
                        Text(text = "${batch.farmerName} • ${batch.cropType}", fontSize = 10.sp, color = Color.Gray)
                        Text(text = "Payout: LRD $%.2f".format(batch.totalPayoutLrd), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF228B62))
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onOpenUssdModal,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("open_ussd_template_${batch.id}")
                    ) {
                        Text("USSD/SMS", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                    }

                    Button(
                        onClick = onTriggerMoMo,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("dispatch_momo_${batch.id}")
                    ) {
                        Text("Pay Now", fontSize = 10.sp, color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Surface(
                color = Color(0xFFFFF3E0),
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFB74D)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_sync_momo_pending_badge_${batch.id}")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Pending Confirmation",
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "🔔 Mobile Money payout status confirmation pending",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE65100)
                        )
                    }

                    Text(
                        text = "Status: ${batch.payoutStatus}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFD84315)
                    )
                }
            }
        }
    }
}
