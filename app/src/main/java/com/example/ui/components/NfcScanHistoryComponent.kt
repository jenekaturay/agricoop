package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.window.DialogProperties
import com.example.service.NfcScanLogEntry
import com.example.service.NfcScanLogService
import com.example.service.TextToSpeechHelper

/**
 * Dedicated Scan History UI Component that displays the last 10 NFC tag events with timestamps,
 * farmer IDs, and tag UIDs, allowing users to verify recent scans immediately.
 */
@Composable
fun NfcScanHistoryComponent(
    modifier: Modifier = Modifier,
    maxItems: Int = 10,
    onFarmerSelect: ((farmerId: String, farmerName: String, tagUid: String) -> Unit)? = null,
    onClearHistory: (() -> Unit)? = null,
    onClose: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    val allLogs by NfcScanLogService.scanLogs.collectAsState()
    val recentLogs = remember(allLogs, maxItems) {
        allLogs.take(maxItems)
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedTimeRange by remember { mutableStateOf("ALL") } // "ALL", "1H", "24H", "7D"

    val now = System.currentTimeMillis()
    val filteredLogs = remember(allLogs, recentLogs, searchQuery, selectedTimeRange, maxItems) {
        val baseLogs = if (selectedTimeRange == "ALL") recentLogs else allLogs
        baseLogs.filter { log ->
            val matchesSearch = searchQuery.isBlank() ||
                    log.farmerId.contains(searchQuery, ignoreCase = true) ||
                    log.farmerName.contains(searchQuery, ignoreCase = true) ||
                    log.tagUid.contains(searchQuery, ignoreCase = true) ||
                    log.cooperativeName.contains(searchQuery, ignoreCase = true)

            val timeDiffMs = now - log.timestamp
            val matchesTime = when (selectedTimeRange) {
                "1H" -> timeDiffMs <= 3600 * 1000L
                "24H" -> timeDiffMs <= 24 * 3600 * 1000L
                "7D" -> timeDiffMs <= 7 * 24 * 3600 * 1000L
                else -> true
            }

            matchesSearch && matchesTime
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF07291F)),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF81C784)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("nfc_scan_history_component")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Component Title, Badge, and Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0D47A1),
                        shape = CircleShape,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "NFC SCAN VERIFICATION HISTORY",
                                color = Color(0xFFFFD54F),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF81C784),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "LAST $maxItems SCANS",
                                    color = Color(0xFF0B3D2E),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = "Verify recent RFID card taps & farmer IDs",
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onClose != null) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("close_scan_history_component")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // View Mode Tab Selector (Scan Logs vs 30-Day Batch Weight Yield Chart)
            var selectedViewTab by remember { mutableStateOf(0) } // 0 = Scan Logs, 1 = 30-Day Batch Weight Yield Chart

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF04140F), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = if (selectedViewTab == 0) Color(0xFF1B5E20) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedViewTab = 0 }
                        .testTag("tab_scan_logs_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (selectedViewTab == 0) Color.White else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "NFC Scan Logs",
                            fontSize = 11.sp,
                            fontWeight = if (selectedViewTab == 0) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedViewTab == 0) Color.White else Color.Gray
                        )
                    }
                }

                Surface(
                    color = if (selectedViewTab == 1) Color(0xFF1B5E20) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedViewTab = 1 }
                        .testTag("tab_30day_yield_chart_button")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = if (selectedViewTab == 1) Color(0xFFFFD54F) else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "30-Day Weight Yield Chart",
                            fontSize = 11.sp,
                            fontWeight = if (selectedViewTab == 1) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedViewTab == 1) Color(0xFFFFD54F) else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedViewTab == 1) {
                BatchWeight30DayYieldChart()
            } else {
            // Search Bar & Clear Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter by Farmer ID, Name, Tag UID...", fontSize = 11.sp, color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(16.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("scan_history_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                if (recentLogs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            if (onClearHistory != null) onClearHistory() else NfcScanLogService.clearHistory()
                            ttsHelper.speakText("NFC scan history logs cleared")
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF9A9A)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("clear_nfc_history_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear", fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp Range Filter Selector Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Range:",
                    color = Color(0xFF80CBC4),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )

                val filterOptions = listOf(
                    "ALL" to "All Scans",
                    "1H" to "1 Hour",
                    "24H" to "Today (24h)",
                    "7D" to "Past 7 Days"
                )

                filterOptions.forEach { (key, label) ->
                    val isSelected = selectedTimeRange == key
                    Surface(
                        color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF0B3D2E),
                        contentColor = if (isSelected) Color(0xFF0B3D2E) else Color.White,
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (isSelected) Color(0xFFFFD54F) else Color(0xFF81C784)
                        ),
                        shape = RoundedCornerShape(6.dp),
                        modifier = Modifier
                            .clickable { selectedTimeRange = key }
                            .testTag("scan_history_time_filter_$key")
                    ) {
                        Text(
                            text = label,
                            fontSize = 9.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Subtitle status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Displaying ${filteredLogs.size} of ${recentLogs.size} Recent Taps:",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = "Status: ALL VERIFIED ✓",
                    color = Color(0xFF81C784),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // List of Recent Scan Log Entries
            if (filteredLogs.isEmpty()) {
                Surface(
                    color = Color(0xFF0B3D2E),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No Recent NFC Scans Logged",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "Tap a smallholder card at the scale station to record scan history.",
                            color = Color(0xFF80CBC4),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                ) {
                    itemsIndexed(filteredLogs) { index, entry ->
                        NfcScanHistoryItemRow(
                            indexNumber = index + 1,
                            entry = entry,
                            onFarmerSelect = onFarmerSelect,
                            onSpeak = {
                                ttsHelper.speakText(
                                    "Verified NFC tag scan %d. Farmer ID: %s. Name: %s. Tag UID: %s. Scanned at %s.".format(
                                        index + 1,
                                        entry.farmerId,
                                        entry.farmerName,
                                        entry.tagUid,
                                        entry.formattedTime
                                    )
                                )
                            }
                        )
                    }
                }
            }
            }
        }
    }
}

/**
 * Individual Card Row item for a scanned NFC Tag Event in the Scan History component.
 */
@Composable
fun NfcScanHistoryItemRow(
    indexNumber: Int,
    entry: NfcScanLogEntry,
    onFarmerSelect: ((farmerId: String, farmerName: String, tagUid: String) -> Unit)?,
    onSpeak: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3D2E)),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1B5E20)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("scan_history_item_$indexNumber")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Top Line: Index Badge, Farmer ID, Timestamp, Voice Icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Index Badge e.g. #1, #2
                    Surface(
                        color = Color(0xFFFFD54F),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "#$indexNumber",
                            color = Color(0xFF0B3D2E),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Farmer ID Badge
                    Surface(
                        color = Color(0xFF0D47A1),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FARMER ID: ${entry.farmerId}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                            fontFamily = FontFamily.Monospace
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Scan Mode Badge
                    Surface(
                        color = Color(0xFF1B5E20),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = entry.scanMode.replace("_", " "),
                            color = Color(0xFF81C784),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // TTS Audio Verification Button
                    IconButton(
                        onClick = onSpeak,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("speak_scan_item_$indexNumber")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Read scan out loud",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Info: Farmer Name & Tag UID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = entry.farmerName,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "RFID Tag UID: ${entry.tagUid} • ${entry.cooperativeName}",
                        color = Color(0xFF80CBC4),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }

                if (onFarmerSelect != null) {
                    Button(
                        onClick = {
                            onFarmerSelect(entry.farmerId, entry.farmerName, entry.tagUid)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFFD54F),
                            contentColor = Color(0xFF0B3D2E)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("select_scanned_farmer_$indexNumber")
                    ) {
                        Text("Select", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Footer info: Timestamp & Status indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🕒 ${entry.formattedTime}",
                    color = Color.LightGray,
                    fontSize = 10.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "VERIFIED TAP",
                        color = Color(0xFF81C784),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Modal Popup Dialog Wrapper for the NfcScanHistoryComponent.
 */
@Composable
fun NfcScanHistoryModal(
    onDismiss: () -> Unit,
    onFarmerSelect: ((farmerId: String, farmerName: String, tagUid: String) -> Unit)? = null
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp),
            color = Color.Transparent
        ) {
            NfcScanHistoryComponent(
                maxItems = 10,
                onFarmerSelect = { id, name, uid ->
                    onFarmerSelect?.invoke(id, name, uid)
                    onDismiss()
                },
                onClose = onDismiss
            )
        }
    }
}
