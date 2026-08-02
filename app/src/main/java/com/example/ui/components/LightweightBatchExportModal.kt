package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.ProduceBatchEntity
import com.example.service.CsvExportResult
import com.example.service.RoomCsvExporter
import com.example.service.TextToSpeechHelper

enum class LightweightBatchFilterMode(val key: String, val title: String, val description: String) {
    UNSYNCED("UNSYNCED", "Unsynced Records Only", "Only batches awaiting cloud upload (ideal for Bluetooth mesh transfer)"),
    RECENT("RECENT", "Recent Batches (Top 50)", "Last 50 batch entries for quick local sharing"),
    ALL("ALL", "All Batch Records", "Full batch history dataset")
}

@Composable
fun LightweightBatchExportModal(
    batches: List<ProduceBatchEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    val unsyncedCount = remember(batches) { batches.count { !it.isSynced } }
    var selectedMode by remember {
        mutableStateOf(
            if (unsyncedCount > 0) LightweightBatchFilterMode.UNSYNCED else LightweightBatchFilterMode.RECENT
        )
    }

    var exportResult by remember { mutableStateOf<CsvExportResult?>(null) }

    val csvContent = remember(selectedMode, batches) {
        RoomCsvExporter.exportLightweightBatchCsv(batches, selectedMode.key)
    }

    val targetRecordCount = remember(selectedMode, batches) {
        when (selectedMode) {
            LightweightBatchFilterMode.UNSYNCED -> unsyncedCount
            LightweightBatchFilterMode.RECENT -> batches.sortedByDescending { it.timestamp }.take(50).size
            LightweightBatchFilterMode.ALL -> batches.size
        }
    }

    val estimatedSizeKb = remember(csvContent) {
        (csvContent.toByteArray(Charsets.UTF_8).size / 1024.0)
    }

    val previewText = remember(csvContent) {
        csvContent.lines().take(10).joinToString("\n")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("lightweight_batch_export_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0B3D2E),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bluetooth,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "Export Batch History CSV",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B3D2E)
                            )
                            Text(
                                text = "Lightweight CSV for Bluetooth & Offline Transfer",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scope Selection Options
                Text(
                    text = "Select Batch Export Scope:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0B3D2E)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LightweightBatchFilterMode.values().forEach { mode ->
                        val isSelected = selectedMode == mode
                        val count = when (mode) {
                            LightweightBatchFilterMode.UNSYNCED -> unsyncedCount
                            LightweightBatchFilterMode.RECENT -> batches.sortedByDescending { it.timestamp }.take(50).size
                            LightweightBatchFilterMode.ALL -> batches.size
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF9F9F9)
                            ),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF2E7D32) else Color(0xFFE0E0E0)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedMode = mode
                                    exportResult = null
                                }
                                .testTag("export_mode_${mode.key.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedMode = mode
                                        exportResult = null
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0B3D2E))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = mode.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = if (isSelected) Color(0xFF0B3D2E) else Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            color = if (mode == LightweightBatchFilterMode.UNSYNCED && count > 0) Color(0xFFFFF3E0) else Color(0xFFE0F2F1),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "$count records",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (mode == LightweightBatchFilterMode.UNSYNCED && count > 0) Color(0xFFE65100) else Color(0xFF00695C),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = mode.description,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // File Size & Transfer Specs Banner
                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color(0xFFA5D6A7)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ESTIMATED CSV SIZE",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32)
                            )
                            Text(
                                text = "%.2f KB • $targetRecordCount Records".format(estimatedSizeKb),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF1B5E20)
                            )
                        }

                        Surface(
                            color = Color(0xFF0B3D2E),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "📶 Offline Bluetooth Ready",
                                color = Color(0xFFFFD54F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Monospace CSV Preview Box
                Text(
                    text = "CSV Output Preview:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    color = Color(0xFF1E1E1E),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    Text(
                        text = previewText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color(0xFF80CBC4),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Saved File Details Banner
                exportResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF2E7D32)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Batch History CSV Saved!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "File: ${result.exportFile.name} (${result.exportFile.length()} bytes)",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = Color.DarkGray
                            )
                            Text(
                                text = "Path: ${result.exportFile.absolutePath}",
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val prefix = "batch_history_${selectedMode.key.lowercase()}"
                            val res = RoomCsvExporter.saveCsvToLocalDirectory(context, csvContent, prefix)
                            if (res != null) {
                                exportResult = res
                                ttsHelper.speakText("Sharing lightweight batch CSV via Bluetooth or local transfer")
                                RoomCsvExporter.shareCsvFile(context, res.exportFile)
                            } else {
                                Toast.makeText(context, "Failed to prepare CSV file for sharing", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_lightweight_csv_bluetooth_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share via Bluetooth", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val prefix = "batch_history_${selectedMode.key.lowercase()}"
                            val res = RoomCsvExporter.saveCsvToLocalDirectory(context, csvContent, prefix)
                            if (res != null) {
                                exportResult = res
                                ttsHelper.speakText("Saved lightweight batch CSV to device storage")
                                Toast.makeText(context, "Saved CSV: ${res.exportFile.name}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to save CSV file", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_lightweight_csv_local_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save Local File", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
