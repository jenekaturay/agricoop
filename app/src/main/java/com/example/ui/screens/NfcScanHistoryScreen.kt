package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.material.icons.filled.BarChart
import com.example.ui.components.BatchWeight30DayYieldChart
import com.example.service.NfcScanLogEntry
import com.example.service.NfcScanLogService
import com.example.service.TextToSpeechHelper
import com.example.ui.components.SpeakIconButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NfcScanHistoryScreen(
    onBack: (() -> Unit)? = null,
    onOpenNfcScanner: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    val scanLogs by NfcScanLogService.scanLogs.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedScanModeFilter by remember { mutableStateOf("ALL") }
    var selectedTimeRange by remember { mutableStateOf("ALL") } // "ALL", "1H", "24H", "7D"
    var expandedFilterDropdown by remember { mutableStateOf(false) }

    val now = System.currentTimeMillis()
    val filteredLogs = remember(scanLogs, searchQuery, selectedScanModeFilter, selectedTimeRange) {
        scanLogs.filter { log ->
            val matchesQuery = searchQuery.isEmpty() ||
                    log.farmerId.contains(searchQuery, ignoreCase = true) ||
                    log.farmerName.contains(searchQuery, ignoreCase = true) ||
                    log.tagUid.contains(searchQuery, ignoreCase = true) ||
                    log.cooperativeName.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (selectedScanModeFilter) {
                "HARDWARE" -> log.scanMode.contains("HARDWARE", ignoreCase = true)
                "SIMULATED" -> log.scanMode.contains("SIMULATED", ignoreCase = true)
                "MANUAL" -> log.scanMode.contains("MANUAL", ignoreCase = true)
                else -> true
            }

            val timeDiffMs = now - log.timestamp
            val matchesTime = when (selectedTimeRange) {
                "1H" -> timeDiffMs <= 3600 * 1000L
                "24H" -> timeDiffMs <= 24 * 3600 * 1000L
                "7D" -> timeDiffMs <= 7 * 24 * 3600 * 1000L
                else -> true
            }

            matchesQuery && matchesFilter && matchesTime
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0D47A1),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Nfc,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "NFC Tag Scan History Log",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Contactless RFID Membership Card Tap Audit Events",
                                color = Color(0xFF80CBC4),
                                fontSize = 10.sp
                            )
                        }
                    }
                },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("nfc_history_back_button")
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                },
                actions = {
                    if (onOpenNfcScanner != null) {
                        Button(
                            onClick = onOpenNfcScanner,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFFFFD54F),
                                contentColor = Color(0xFF0B3D2E)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .height(34.dp)
                                .testTag("launch_nfc_tap_station_button")
                        ) {
                            Icon(Icons.Default.Contactless, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Tap Card", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF0B3D2E))
            )
        },
        containerColor = Color(0xFFF4F6F5)
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Header Summary Stats Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF07291F)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NFC CARD AUDIT LOG",
                                color = Color(0xFF80CBC4),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${scanLogs.size} Total Card Taps Logged",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (scanLogs.isNotEmpty()) "Latest scan: ${scanLogs.first().formattedTime}" else "No scans logged yet",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Surface(
                        color = Color(0xFF0D47A1),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("SCAN STATUS", color = Color(0xFF90CAF9), fontSize = 8.sp, fontWeight = FontWeight.Bold)
                            Text("100% OK", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 30-Day Batch Weights Collected Visual Yield Assessment Chart
            var show30DayChart by remember { mutableStateOf(true) }

            Surface(
                color = Color(0xFF04140F),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF2E7D32)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { show30DayChart = !show30DayChart }
                    .testTag("toggle_screen_30day_chart_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "30-Day Batch Weight Yield Assessment Chart",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFECB3)
                        )
                    }
                    Text(
                        text = if (show30DayChart) "Hide Graph ▲" else "Show Graph ▼",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF81C784)
                    )
                }
            }

            if (show30DayChart) {
                Spacer(modifier = Modifier.height(8.dp))
                BatchWeight30DayYieldChart()
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Search Bar & Scan Mode Filter Dropdown
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by Farmer ID, Name, or UID...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("nfc_history_search_input"),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Box {
                    OutlinedButton(
                        onClick = { expandedFilterDropdown = true },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("nfc_history_filter_button")
                    ) {
                        Icon(Icons.Default.FilterList, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (selectedScanModeFilter) {
                                "HARDWARE" -> "Hardware"
                                "SIMULATED" -> "Simulated"
                                "MANUAL" -> "Manual"
                                else -> "All Modes"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    DropdownMenu(
                        expanded = expandedFilterDropdown,
                        onDismissRequest = { expandedFilterDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("All Scan Modes") },
                            onClick = {
                                selectedScanModeFilter = "ALL"
                                expandedFilterDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Hardware Reader Taps") },
                            onClick = {
                                selectedScanModeFilter = "HARDWARE"
                                expandedFilterDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Simulated Card Taps") },
                            onClick = {
                                selectedScanModeFilter = "SIMULATED"
                                expandedFilterDropdown = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Manual Tag UID Lookups") },
                            onClick = {
                                selectedScanModeFilter = "MANUAL"
                                expandedFilterDropdown = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timestamp Range Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Time Range:",
                    color = Color(0xFF0B3D2E),
                    fontSize = 11.sp,
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
                        color = if (isSelected) Color(0xFF0B3D2E) else Color(0xFFE8F5E9),
                        contentColor = if (isSelected) Color.White else Color(0xFF0B3D2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF0B3D2E) else Color(0xFFA5D6A7)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { selectedTimeRange = key }
                            .testTag("nfc_history_time_filter_$key")
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Scanning Events (${filteredLogs.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF0B3D2E)
                )

                if (scanLogs.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { NfcScanLogService.clearHistory() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("clear_nfc_history_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear Logs", fontSize = 10.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (filteredLogs.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No NFC Scan Events Found",
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Tap a farmer membership card at the weighing station to generate log entries.",
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredLogs) { entry ->
                        NfcScanLogCard(
                            entry = entry,
                            onSpeak = {
                                ttsHelper.speakText(
                                    "NFC Scan Event. Farmer ID: %s. Farmer Name: %s. Tag UID: %s. Timestamp: %s.".format(
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

@Composable
fun NfcScanLogCard(
    entry: NfcScanLogEntry,
    onSpeak: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("nfc_scan_log_item_${entry.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Farmer ID Badge, Timestamp, Speak TTS Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0B3D2E),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "FARMER ID: ${entry.farmerId}",
                            color = Color(0xFFFFD54F),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        color = when {
                            entry.scanMode.contains("HARDWARE", ignoreCase = true) -> Color(0xFFE8F5E9)
                            entry.scanMode.contains("SIMULATED", ignoreCase = true) -> Color(0xFFE3F2FD)
                            else -> Color(0xFFFFF3E0)
                        },
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = entry.scanMode.replace("_", " "),
                            color = when {
                                entry.scanMode.contains("HARDWARE", ignoreCase = true) -> Color(0xFF2E7D32)
                                entry.scanMode.contains("SIMULATED", ignoreCase = true) -> Color(0xFF1565C0)
                                else -> Color(0xFFE65100)
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = entry.formattedTime,
                        fontSize = 10.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    SpeakIconButton(
                        onClick = onSpeak,
                        contentDescription = "Read scan log out loud",
                        tint = Color(0xFF0D47A1),
                        testTag = "speak_nfc_log_${entry.id}"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content: Farmer Name & Tag UID
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = entry.farmerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0B3D2E)
                    )
                    Text(
                        text = entry.cooperativeName,
                        fontSize = 11.sp,
                        color = Color.DarkGray
                    )
                }

                Surface(
                    color = Color(0xFFF1F8E9),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFAED581))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = null,
                            tint = Color(0xFF33691E),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "UID: ${entry.tagUid}",
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF33691E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom Status & Location Notes
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📍 ${entry.location} • ${entry.notes}",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = entry.status,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2E7D32)
                    )
                }
            }
        }
    }
}
