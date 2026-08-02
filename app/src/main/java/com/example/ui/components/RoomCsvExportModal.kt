package com.example.ui.components

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
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
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.service.CsvExportResult
import com.example.service.RoomCsvExporter

enum class CsvExportOption(val label: String, val description: String, val prefix: String) {
    MASTER_AUDIT("Full Master Audit CSV", "All Room tables (Batches, Farmers, Co-ops, Hubs, Floats)", "room_master_audit"),
    PRODUCE_BATCHES("Produce Batches Table", "Tuber weight records, payouts, and sync statuses", "room_produce_batches"),
    FARMERS_REGISTRY("Farmers Registry Table", "Smallholder profiles, MoMo numbers, seed allocations", "room_farmers_registry"),
    COOPERATIVES("Cooperatives Table", "District & county agri-cooperative directory", "room_cooperatives"),
    HUB_OPERATIONS("Hub Operations Table", "Tonnage capacity, storage temp, solar status", "room_hub_operations"),
    MOMO_FLOATS("MoMo Float Balances Table", "Agent float liquidity per processing hub", "room_momo_floats")
}

@Composable
fun RoomCsvExportModal(
    batches: List<ProduceBatchEntity>,
    farmers: List<FarmerEntity>,
    cooperatives: List<CooperativeEntity>,
    hubs: List<HubOperationEntity>,
    floats: List<MoMoFloatEntity>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedOption by remember { mutableStateOf(CsvExportOption.MASTER_AUDIT) }
    var exportResult by remember { mutableStateOf<CsvExportResult?>(null) }

    fun generateCsvString(option: CsvExportOption): String {
        return when (option) {
            CsvExportOption.MASTER_AUDIT -> RoomCsvExporter.exportFullMasterCsv(batches, farmers, cooperatives, hubs, floats)
            CsvExportOption.PRODUCE_BATCHES -> RoomCsvExporter.exportProduceBatchesToCsv(batches)
            CsvExportOption.FARMERS_REGISTRY -> RoomCsvExporter.exportFarmersToCsv(farmers)
            CsvExportOption.COOPERATIVES -> RoomCsvExporter.exportCooperativesToCsv(cooperatives)
            CsvExportOption.HUB_OPERATIONS -> RoomCsvExporter.exportHubOperationsToCsv(hubs)
            CsvExportOption.MOMO_FLOATS -> RoomCsvExporter.exportMoMoFloatsToCsv(floats)
        }
    }

    val currentPreviewText = remember(selectedOption, batches, farmers, cooperatives, hubs, floats) {
        val full = generateCsvString(selectedOption)
        full.lines().take(12).joinToString("\n")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("room_csv_export_modal")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Modal Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0B3D2E),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.TableChart,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Export Room Database CSV",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B3D2E)
                            )
                            Text(
                                text = "Local Storage Backup & Audit Trail",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Table Selector Options
                Text(
                    text = "Select Export Dataset:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = Color(0xFF0B3D2E)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    CsvExportOption.values().forEach { option ->
                        val isSelected = selectedOption == option
                        val count = when (option) {
                            CsvExportOption.MASTER_AUDIT -> batches.size + farmers.size + cooperatives.size + hubs.size + floats.size
                            CsvExportOption.PRODUCE_BATCHES -> batches.size
                            CsvExportOption.FARMERS_REGISTRY -> farmers.size
                            CsvExportOption.COOPERATIVES -> cooperatives.size
                            CsvExportOption.HUB_OPERATIONS -> hubs.size
                            CsvExportOption.MOMO_FLOATS -> floats.size
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFFE8F5E9) else Color(0xFFF9F9F9)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedOption = option
                                    exportResult = null
                                }
                                .testTag("export_option_${option.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        selectedOption = option
                                        exportResult = null
                                    },
                                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF0B3D2E))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${option.label} ($count records)",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (isSelected) Color(0xFF0B3D2E) else Color.Black
                                    )
                                    Text(
                                        text = option.description,
                                        fontSize = 10.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // CSV Data Preview Box
                Text(
                    text = "CSV Structure Preview:",
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
                        .height(85.dp)
                ) {
                    Text(
                        text = currentPreviewText,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 9.sp,
                        color = Color(0xFF80CBC4),
                        modifier = Modifier.padding(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Export Success Summary Card if exported
                exportResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2E7D32), RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "CSV Export Saved to Local Storage!",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "File: ${result.exportFile.name} (${result.exportFile.length() / 1024 + 1} KB)",
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

                // Action Buttons (Save CSV & Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val content = generateCsvString(selectedOption)
                            val res = RoomCsvExporter.saveCsvToLocalDirectory(context, content, selectedOption.prefix)
                            if (res != null) {
                                exportResult = res
                                RoomCsvExporter.shareCsvFile(context, res.exportFile)
                            } else {
                                Toast.makeText(context, "Failed to create share intent", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_csv_button")
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share CSV", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val content = generateCsvString(selectedOption)
                            val res = RoomCsvExporter.saveCsvToLocalDirectory(context, content, selectedOption.prefix)
                            if (res != null) {
                                exportResult = res
                                Toast.makeText(context, "Saved CSV to ${res.exportFile.name}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to save CSV to local storage", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_csv_local_button")
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save to Local Storage", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
