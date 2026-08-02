package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.ProduceBatchEntity
import com.example.service.TextToSpeechHelper

/**
 * Summary dashboard element displaying the number of pending unsynced records,
 * total volume, total payout value, and detailed batch breakdown awaiting cloud upload.
 */
@Composable
fun PendingUnsyncedSummaryCard(
    batches: List<ProduceBatchEntity>,
    onTriggerSync: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    val unsyncedBatches = remember(batches) {
        batches.filter { !it.isSynced }
    }

    val unsyncedCount = unsyncedBatches.size
    val totalCount = batches.size
    val totalPendingWeightKg = remember(unsyncedBatches) {
        unsyncedBatches.sumOf { it.weightKg }
    }
    val totalPendingPayoutLrd = remember(unsyncedBatches) {
        unsyncedBatches.sumOf { it.totalPayoutLrd }
    }

    var isExpanded by remember { mutableStateOf(false) }
    var showCsvModal by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (unsyncedCount > 0) Color(0xFFFFF8E1) else Color(0xFFF1F8E9)
        ),
        border = BorderStroke(
            width = 1.5.dp,
            color = if (unsyncedCount > 0) Color(0xFFFFB300) else Color(0xFF81C784)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("pending_unsynced_summary_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
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
                        color = if (unsyncedCount > 0) Color(0xFFFF8F00) else Color(0xFF2E7D32),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (unsyncedCount > 0) Icons.Default.CloudQueue else Icons.Default.CloudDone,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "PENDING CLOUD SYNC QUEUE",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (unsyncedCount > 0) Color(0xFFB71C1C) else Color(0xFF1B5E20),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = if (unsyncedCount > 0) Color(0xFFFFECB3) else Color(0xFFC8E6C9),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (unsyncedCount > 0) "$unsyncedCount PENDING" else "ALL SYNCED",
                                    color = if (unsyncedCount > 0) Color(0xFFE65100) else Color(0xFF2E7D32),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (unsyncedCount > 0)
                                "$unsyncedCount of $totalCount total records queued locally in Room DB"
                            else
                                "All $totalCount records are synchronized with central cloud server",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }
                }

                if (unsyncedCount > 0) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                isExpanded = true
                                showCsvModal = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B3D2E)),
                            border = BorderStroke(1.dp, Color(0xFF0B3D2E)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("export_unsynced_csv_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.UploadFile,
                                contentDescription = "Export CSV",
                                tint = Color(0xFF0B3D2E),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("CSV Share", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = {
                                ttsHelper.speakText("Uploading $unsyncedCount pending records to central cloud database")
                                onTriggerSync()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0B3D2E),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("upload_pending_records_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3-Metric Summary Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Metric 1: Pending Count
                UnsyncedMetricBox(
                    label = "Unsynced Records",
                    value = "$unsyncedCount",
                    subValue = if (totalCount > 0) "%.0f%% of total".format((unsyncedCount.toDouble() / totalCount) * 100) else "0%",
                    accentColor = if (unsyncedCount > 0) Color(0xFFD84315) else Color(0xFF2E7D32),
                    modifier = Modifier.weight(1f)
                )

                // Metric 2: Pending Volume
                UnsyncedMetricBox(
                    label = "Volume Pending",
                    value = if (totalPendingWeightKg >= 1000) "%.2f T".format(totalPendingWeightKg / 1000.0) else "%.1f kg".format(totalPendingWeightKg),
                    subValue = "Tuber produce",
                    accentColor = Color(0xFF00695C),
                    modifier = Modifier.weight(1f)
                )

                // Metric 3: Financial Value
                UnsyncedMetricBox(
                    label = "Payout Value",
                    value = "LRD $%.0f".format(totalPendingPayoutLrd),
                    subValue = "Queued payouts",
                    accentColor = Color(0xFF1565C0),
                    modifier = Modifier.weight(1f)
                )
            }

            // Expandable breakdown for pending batch records
            if (unsyncedCount > 0) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isExpanded) "Hide Pending Batch Items ▲" else "View Detailed Pending Queue ($unsyncedCount Items) ▼",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B3D2E)
                    )

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle List",
                        tint = Color(0xFF0B3D2E),
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Divider(color = Color(0xFFFFD54F), thickness = 1.dp)

                        unsyncedBatches.forEachIndexed { index, batch ->
                            Surface(
                                color = Color.White,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFFFE082)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("unsynced_record_item_$index")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = batch.batchCode,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 12.sp,
                                                fontFamily = FontFamily.Monospace,
                                                color = Color(0xFF0B3D2E)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                color = Color(0xFFFFF3E0),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = batch.cropType,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFFE65100),
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Farmer: ${batch.farmerName} • ${batch.locationName}",
                                            fontSize = 11.sp,
                                            color = Color.DarkGray
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${batch.weightKg} kg",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF2E7D32)
                                        )
                                        Text(
                                            text = "LRD $%.2f".format(batch.totalPayoutLrd),
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCsvModal) {
        LightweightBatchExportModal(
            batches = batches,
            onDismiss = { showCsvModal = false }
        )
    }
}

@Composable
private fun UnsyncedMetricBox(
    label: String,
    value: String,
    subValue: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor
            )
            Text(
                text = subValue,
                fontSize = 9.sp,
                color = Color.DarkGray
            )
        }
    }
}
