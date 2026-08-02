package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.ProduceBatchEntity
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.TextToSpeechHelper
import com.example.ui.components.NfcTagScannerModal
import com.example.ui.components.QrCodeScannerModal
import com.example.ui.components.SpeakIconButton
import com.example.ui.components.TtsAudioConfirmationBanner
import com.example.ui.components.TuberYieldThresholdIndicator
import com.example.ui.components.TuberYieldThresholds
import com.example.ui.components.VoiceBatchLoggingModal

data class NfcScanToastData(
    val title: String,
    val farmerName: String,
    val details: String,
    val tagUid: String? = null,
    val registeredBatch: ProduceBatchEntity? = null,
    val previousFarmer: FarmerEntity? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeighBatchScreen(
    farmers: List<FarmerEntity>,
    batches: List<ProduceBatchEntity> = emptyList(),
    isScaleConnected: Boolean,
    scaleWeightKg: Double,
    scaleStatusText: String,
    onToggleScale: () -> Unit,
    onCalibrateScale: () -> Unit,
    onSelectBatchForQr: (ProduceBatchEntity) -> Unit = {},
    onSubmitBatch: (
        farmerId: String,
        cropType: String,
        weightKg: Double,
        starchPct: Double,
        moisturePct: Double,
        pricePerKgLrd: Double,
        locationName: String
    ) -> Unit,
    onUndoBatch: ((ProduceBatchEntity) -> Unit)? = null
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }
    val isTtsSpeaking by ttsHelper.isSpeaking.collectAsState()
    var isTtsEnabled by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    var selectedFarmer by remember { mutableStateOf(farmers.firstOrNull()) }
    var expandedFarmerDropdown by remember { mutableStateOf(false) }
    var showQrScannerModal by remember { mutableStateOf(false) }
    var showNfcReaderModal by remember { mutableStateOf(false) }
    var showNfcHistoryModal by remember { mutableStateOf(false) }
    var showVoiceModal by remember { mutableStateOf(false) }
    var showLightweightCsvModal by remember { mutableStateOf(false) }
    var lastScannedNfcUid by remember { mutableStateOf<String?>(null) }
    var isNfcAutoFilledBannerVisible by remember { mutableStateOf(false) }
    var nfcToastData by remember { mutableStateOf<NfcScanToastData?>(null) }

    var batchSearchQuery by remember { mutableStateOf("") }
    var batchSortOption by remember { mutableStateOf("DATE_DESC") }

    val filteredBatches = remember(batches, farmers, batchSearchQuery) {
        if (batchSearchQuery.isBlank()) {
            batches
        } else {
            val q = batchSearchQuery.trim()
            val farmerMap = farmers.associateBy { it.id }
            batches.filter { batch ->
                val farmer = farmerMap[batch.farmerId]
                val nationalId = farmer?.nationalId ?: ""
                val farmerPhone = farmer?.phoneNumber ?: ""
                batch.farmerName.contains(q, ignoreCase = true) ||
                        batch.farmerId.contains(q, ignoreCase = true) ||
                        nationalId.contains(q, ignoreCase = true) ||
                        batch.batchCode.contains(q, ignoreCase = true) ||
                        farmerPhone.contains(q, ignoreCase = true)
            }
        }
    }

    val sortedBatches = remember(filteredBatches, batchSortOption) {
        when (batchSortOption) {
            "DATE_DESC" -> filteredBatches.sortedByDescending { it.timestamp }
            "DATE_ASC" -> filteredBatches.sortedBy { it.timestamp }
            "FARMER_ASC" -> filteredBatches.sortedBy { it.farmerName.lowercase() }
            "FARMER_DESC" -> filteredBatches.sortedByDescending { it.farmerName.lowercase() }
            else -> filteredBatches
        }
    }

    var selectedCrop by remember { mutableStateOf("CASSAVA") } // CASSAVA, YAM, SWEET_POTATO
    var customWeightText by remember { mutableStateOf("%.1f".format(scaleWeightKg)) }
    var starchPctText by remember { mutableStateOf("25.0") }
    var moisturePctText by remember { mutableStateOf("60.0") }
    var pricePerKgText by remember { mutableStateOf("85.0") }

    val locations = listOf(
        "Ganta Collection Spoke 1 (Nimba)",
        "Ganta Collection Spoke 2 (Nimba)",
        "Voinjama Regional Hub (Lofa)",
        "Zorzor Feeder Depot (Lofa)",
        "Sanniquellie Depot (Nimba)",
        "Foya Spoke 3 (Lofa)"
    )
    var selectedLocation by remember { mutableStateOf(locations[0]) }
    var expandedLocationDropdown by remember { mutableStateOf(false) }

    val currentWeight = customWeightText.toDoubleOrNull() ?: scaleWeightKg
    val currentPrice = pricePerKgText.toDoubleOrNull() ?: 85.0
    val totalCalculatedPayout = currentWeight * currentPrice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9F7))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Field Collection & Scale Weighing",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B3D2E)
        )

        // Auditory Voice Confirmation (TTS) Control Banner
        TtsAudioConfirmationBanner(
            isTtsEnabled = isTtsEnabled,
            isSpeaking = isTtsSpeaking,
            onToggleTts = { isTtsEnabled = !isTtsEnabled },
            onTestSpeak = {
                val farmerName = selectedFarmer?.fullName ?: "Farmer"
                ttsHelper.speakWeightConfirmation(farmerName, selectedCrop, currentWeight, totalCalculatedPayout, selectedLocation)
            }
        )

        // Bluetooth Digital Scale Status Gauge Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3D2E)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("bluetooth_scale_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Bluetooth,
                            contentDescription = null,
                            tint = if (isScaleConnected) Color(0xFF81C784) else Color(0xFFFF8A80),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isScaleConnected) "IP67 Digital Scale Paired" else "Manual Scale Mode",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    OutlinedButton(
                        onClick = onCalibrateScale,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFFD54F)),
                        modifier = Modifier.testTag("zero_scale_button")
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Zero/Tare", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Big Digital Weight Display
                val activeCropSpec = remember(selectedCrop) { TuberYieldThresholds.getSpecForCrop(selectedCrop) }
                val isWeightExceedingThreshold = currentWeight > activeCropSpec.warningThresholdKg

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isWeightExceedingThreshold) Color(0xFF3E0A0A) else Color(0xFF07291F),
                            RoundedCornerShape(12.dp)
                        )
                        .border(
                            1.5.dp,
                            if (isWeightExceedingThreshold) Color(0xFFD32F2F) else Color(0xFF1B5E20),
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "LIVE SCALE READING",
                                color = if (isWeightExceedingThreshold) Color(0xFFFF8A80) else Color(0xFF80CBC4),
                                fontSize = 10.sp,
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            SpeakIconButton(
                                onClick = {
                                    val farmerName = selectedFarmer?.fullName ?: "Farmer Partner"
                                    if (isWeightExceedingThreshold) {
                                        ttsHelper.speakText("Warning! Live reading of %.1f kilograms exceeds the %.1f kilogram threshold for %s.".format(currentWeight, activeCropSpec.warningThresholdKg, selectedCrop))
                                    } else {
                                        ttsHelper.speakText("Live scale reading: %.1f kilograms of %s for %s.".format(currentWeight, selectedCrop, farmerName))
                                    }
                                },
                                contentDescription = "Speak Live Weight",
                                tint = if (isWeightExceedingThreshold) Color(0xFFFF5252) else Color(0xFFFFD54F),
                                testTag = "speak_live_scale_weight_button"
                            )
                        }
                        Text(
                            text = "%.1f KG".format(currentWeight),
                            color = if (isWeightExceedingThreshold) Color(0xFFFF5252) else Color(0xFFFFD54F),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            fontFamily = FontFamily.Monospace
                        )
                        if (isWeightExceedingThreshold) {
                            Surface(
                                color = Color(0xFFD32F2F),
                                shape = RoundedCornerShape(4.dp),
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "⚠️ THRESHOLD EXCEEDED (> ${activeCropSpec.warningThresholdKg.toInt()} kg)",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        } else {
                            Text(
                                text = scaleStatusText,
                                color = Color.LightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Weight simulation adjustment chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    WeightAdjustChip("+50kg") {
                        val w = (currentWeight + 50.0)
                        customWeightText = "%.1f".format(w)
                    }
                    WeightAdjustChip("+100kg") {
                        val w = (currentWeight + 100.0)
                        customWeightText = "%.1f".format(w)
                    }
                    WeightAdjustChip("+250kg") {
                        val w = (currentWeight + 250.0)
                        customWeightText = "%.1f".format(w)
                    }
                    WeightAdjustChip("Reset 420kg") {
                        customWeightText = "420.0"
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Microphone Voice-to-Text Input Accessibility Button
                Surface(
                    color = Color(0x33FFFFFF),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFD54F)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showVoiceModal = true }
                        .testTag("open_voice_input_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = Color(0xFFFFD54F),
                                shape = CircleShape,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Voice Confirmation Mode (Literacy Access)",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "Verbally state tuber weight & crop type into microphone",
                                    color = Color(0xFF80CBC4),
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Button(
                            onClick = { showVoiceModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("speak_weight_button")
                        ) {
                            Text("Speak Weight", fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Farmer & Crop Selection Form Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // NFC Scan Success Toast with UNDO Action
                if (nfcToastData != null) {
                    Surface(
                        color = Color(0xFF07291F),
                        shape = RoundedCornerShape(14.dp),
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 14.dp)
                            .border(1.5.dp, Color(0xFFFFD54F), RoundedCornerShape(14.dp))
                            .testTag("nfc_scan_success_toast")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = Color(0xFF0D47A1),
                                    shape = CircleShape,
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Nfc,
                                            contentDescription = "NFC Success",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = nfcToastData!!.title,
                                            color = Color(0xFFFFD54F),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color(0xFF81C784),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    Text(
                                        text = nfcToastData!!.farmerName,
                                        color = Color.White,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = nfcToastData!!.details,
                                        color = Color(0xFF80CBC4),
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            // UNDO BUTTON
                            Button(
                                onClick = {
                                    val toastToUndo = nfcToastData
                                    toastToUndo?.registeredBatch?.let { batchToDelete ->
                                        val matchInList = batches.firstOrNull { b ->
                                            b.farmerId == batchToDelete.farmerId && kotlin.math.abs(b.weightKg - batchToDelete.weightKg) < 0.01
                                        }
                                        onUndoBatch?.invoke(matchInList ?: batchToDelete)
                                    } ?: run {
                                        if (batches.isNotEmpty()) {
                                            val newest = batches.maxByOrNull { it.timestamp }
                                            if (newest != null && newest.farmerId == selectedFarmer?.id) {
                                                onUndoBatch?.invoke(newest)
                                            }
                                        }
                                    }

                                    if (toastToUndo?.previousFarmer != null) {
                                        selectedFarmer = toastToUndo.previousFarmer
                                    }
                                    lastScannedNfcUid = null
                                    isNfcAutoFilledBannerVisible = false

                                    ttsHelper.speakText("NFC scan batch registration undone")
                                    nfcToastData = null
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFD32F2F),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("nfc_scan_toast_undo_button")
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Undo",
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "UNDO",
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { nfcToastData = null },
                                modifier = Modifier
                                    .size(24.dp)
                                    .padding(start = 2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Dismiss Toast",
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Text(
                    text = "Farmer & Batch Information",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color(0xFF0B3D2E)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Active NFC Auto-Filled Link Banner
                if (isNfcAutoFilledBannerVisible && selectedFarmer != null) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF07291F)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                            .testTag("nfc_autofill_active_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = Color(0xFF0D47A1),
                                        shape = CircleShape,
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(Icons.Default.Nfc, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "LINKED NFC TAG DATA • AUTO-FILLED",
                                        color = Color(0xFFFFD54F),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }

                                IconButton(
                                    onClick = { isNfcAutoFilledBannerVisible = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Dismiss", tint = Color.LightGray, modifier = Modifier.size(16.dp))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Farmer ID: ${selectedFarmer?.nationalId} (${selectedFarmer?.fullName})",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = "Co-op: ${selectedFarmer?.cooperativeName} • Tag UID: ${lastScannedNfcUid ?: "04:A2:8F:C1"}",
                                color = Color(0xFF80CBC4),
                                fontSize = 11.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "⚡ Quick Auto-Fill Tuber Crop Presets:",
                                color = Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        selectedCrop = "CASSAVA"
                                        starchPctText = "26.5"
                                        moisturePctText = "58.0"
                                        pricePerKgText = "85.0"
                                        ttsHelper.speakText("Cassava Tuber batch parameters auto-filled for farmer ${selectedFarmer?.fullName}")
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF81C784)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("nfc_preset_cassava")
                                ) {
                                    Text("Cassava (26.5% Starch)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        selectedCrop = "YAM"
                                        starchPctText = "18.0"
                                        moisturePctText = "64.0"
                                        pricePerKgText = "120.0"
                                        ttsHelper.speakText("Yam Tuber batch parameters auto-filled for farmer ${selectedFarmer?.fullName}")
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF90CAF9)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF90CAF9)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("nfc_preset_yam")
                                ) {
                                    Text("Yam (18% Starch)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Select Smallholder Farmer Header + Scan QR Card Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Select Smallholder Farmer:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { showNfcReaderModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0D47A1),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("tap_nfc_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Nfc,
                                contentDescription = "Tap NFC Card",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("NFC Tap", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showNfcHistoryModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF07291F),
                                contentColor = Color(0xFFFFD54F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("view_nfc_scan_history_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = "Scan History",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("History", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
                            onClick = { showQrScannerModal = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0B3D2E),
                                contentColor = Color(0xFFFFD54F)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("scan_qr_card_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode,
                                contentDescription = "Scan QR",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Scan QR", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedFarmerDropdown,
                    onExpandedChange = { expandedFarmerDropdown = !expandedFarmerDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedFarmer?.let { "${it.fullName} (${it.cooperativeName})" } ?: "Select Farmer...",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFarmerDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("select_farmer_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedFarmerDropdown,
                        onDismissRequest = { expandedFarmerDropdown = false }
                    ) {
                        farmers.forEach { farmer ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(text = farmer.fullName, fontWeight = FontWeight.Bold)
                                        Text(text = "${farmer.cooperativeName} • MoMo: ${farmer.momoNumber}", fontSize = 11.sp, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    selectedFarmer = farmer
                                    expandedFarmerDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Crop Type Selector Chips
                Text(text = "Crop Type Sourced:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CropChip("Cassava (TME 419)", "CASSAVA", selectedCrop) { selectedCrop = "CASSAVA" }
                    CropChip("Yam", "YAM", selectedCrop) { selectedCrop = "YAM" }
                    CropChip("Sweet Potato", "SWEET_POTATO", selectedCrop) { selectedCrop = "SWEET_POTATO" }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hydrometer Starch & Moisture Test Inputs
                Text(text = "Hydrometer Starch & Quality Tests:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = starchPctText,
                        onValueChange = { starchPctText = it },
                        label = { Text("Starch Content %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_starch_pct")
                    )

                    OutlinedTextField(
                        value = moisturePctText,
                        onValueChange = { moisturePctText = it },
                        label = { Text("Moisture %") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_moisture_pct")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Price per kg & Collection Depot
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = pricePerKgText,
                        onValueChange = { pricePerKgText = it },
                        label = { Text("Price/kg (LRD)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("input_price_per_kg")
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        val cropSpec = remember(selectedCrop) { TuberYieldThresholds.getSpecForCrop(selectedCrop) }
                        val isWeightExceeding = currentWeight > cropSpec.warningThresholdKg

                        OutlinedTextField(
                            value = customWeightText,
                            onValueChange = { customWeightText = it },
                            label = {
                                Text(
                                    if (isWeightExceeding) "Weight (kg) • EXCEEDED" else "Weight (kg)",
                                    color = if (isWeightExceeding) Color(0xFFD32F2F) else Color.Unspecified
                                )
                            },
                            isError = isWeightExceeding,
                            colors = if (isWeightExceeding) {
                                OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFD32F2F),
                                    unfocusedBorderColor = Color(0xFFD32F2F),
                                    focusedLabelColor = Color(0xFFD32F2F),
                                    unfocusedLabelColor = Color(0xFFD32F2F),
                                    errorBorderColor = Color(0xFFD32F2F),
                                    errorLabelColor = Color(0xFFD32F2F),
                                    focusedContainerColor = Color(0xFFFFEBEE),
                                    unfocusedContainerColor = Color(0xFFFFEBEE)
                                )
                            } else {
                                OutlinedTextFieldDefaults.colors()
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_batch_weight_numeric")
                                .testTag("input_weight_kg")
                        )

                        if (isWeightExceeding) {
                            Text(
                                text = "⚠️ Exceeds ${cropSpec.warningThresholdKg.toInt()} kg expectation!",
                                color = Color(0xFFD32F2F),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 2.dp, start = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Quick-add numeric weight tracking buttons
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(1.0, 5.0, 10.0, 25.0, 50.0).forEach { inc ->
                                OutlinedButton(
                                    onClick = {
                                        val cur = customWeightText.toDoubleOrNull() ?: 0.0
                                        val newW = cur + inc
                                        customWeightText = "%.1f".format(newW)
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B3D2E)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4)),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 1.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(26.dp)
                                        .testTag("weight_add_${inc.toInt()}kg_button")
                                ) {
                                    Text("+$inc", fontSize = 8.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tuber Variety Yield Expectations Threshold Indicator
                TuberYieldThresholdIndicator(
                    cropType = selectedCrop,
                    currentWeightKg = currentWeight,
                    onCapWeightToThreshold = { thresholdKg ->
                        customWeightText = "%.1f".format(thresholdKg)
                        ttsHelper.speakText("Weight entry capped to ${thresholdKg.toInt()} kilograms threshold for $selectedCrop.")
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Collection Location Dropdown
                Text(text = "Farm-Gate Collection Hub Location:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(4.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedLocationDropdown,
                    onExpandedChange = { expandedLocationDropdown = !expandedLocationDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedLocation,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLocationDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("select_location_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedLocationDropdown,
                        onDismissRequest = { expandedLocationDropdown = false }
                    ) {
                        locations.forEach { loc ->
                            DropdownMenuItem(
                                text = { Text(loc) },
                                onClick = {
                                    selectedLocation = loc
                                    expandedLocationDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Total Payout Preview Box
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp),
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
                            Text(text = "TOTAL FARMER PAYOUT:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF2E7D32))
                            Text(
                                text = "LRD $%.2f".format(totalCalculatedPayout),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0B3D2E)
                            )
                            Text(text = "Orange / MTN MoMo Instant Payout", fontSize = 10.sp, color = Color.Gray)
                        }

                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Submit Button
                Button(
                    onClick = {
                        val farmerId = selectedFarmer?.id ?: "farm-001"
                        val farmerName = selectedFarmer?.fullName ?: "Farmer Partner"
                        val starch = starchPctText.toDoubleOrNull() ?: 25.0
                        val moisture = moisturePctText.toDoubleOrNull() ?: 60.0
                        val price = pricePerKgText.toDoubleOrNull() ?: 85.0

                        if (isTtsEnabled) {
                            ttsHelper.speakWeightConfirmation(
                                farmerName = farmerName,
                                cropType = selectedCrop,
                                weightKg = currentWeight,
                                payoutLrd = totalCalculatedPayout,
                                locationName = selectedLocation
                            )
                        }

                        val createdBatch = ProduceBatchEntity(
                            id = java.util.UUID.randomUUID().toString(),
                            batchCode = "BATCH-${if (selectedLocation.contains("Lofa", ignoreCase = true)) "LOF" else "NIM"}-2026-${(100..999).random()}",
                            farmerId = farmerId,
                            farmerName = farmerName,
                            cooperativeName = selectedFarmer?.cooperativeName ?: "Ganta Co-op",
                            cropType = selectedCrop,
                            weightKg = currentWeight,
                            starchPercentage = starch,
                            moisturePercentage = moisture,
                            pricePerKgLrd = price,
                            totalPayoutLrd = totalCalculatedPayout,
                            latitude = 7.3622,
                            longitude = -8.9811,
                            locationName = selectedLocation,
                            payoutStatus = "PENDING",
                            momoTransactionRef = "PENDING_DISPATCH",
                            isSynced = false,
                            timestamp = System.currentTimeMillis()
                        )

                        onSubmitBatch(
                            farmerId,
                            selectedCrop,
                            currentWeight,
                            starch,
                            moisture,
                            price,
                            selectedLocation
                        )

                        nfcToastData = NfcScanToastData(
                            title = if (lastScannedNfcUid != null) "⚡ NFC BATCH REGISTERED SUCCESSFULLY" else "✓ TUBER BATCH REGISTERED",
                            farmerName = farmerName,
                            details = "$selectedCrop • ${currentWeight} kg • LRD ${"%.2f".format(totalCalculatedPayout)}",
                            tagUid = lastScannedNfcUid ?: "04:A2:8F:C1",
                            registeredBatch = createdBatch,
                            previousFarmer = selectedFarmer
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("submit_weigh_batch_button")
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Generate QR Batch & Print Thermal Slip",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Processed Tuber Batches Tracking List with Offline Search & Sorting
        if (batches.isNotEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weigh_batch_tracking_list_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort Batches",
                                tint = Color(0xFF0B3D2E),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Batch Tracking List (${sortedBatches.size}/${batches.size})",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0B3D2E)
                            )
                        }

                        OutlinedButton(
                            onClick = { showLightweightCsvModal = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B3D2E)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0B3D2E)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("export_batch_history_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export CSV",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Export CSV (Offline)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Offline Search Bar
                    OutlinedTextField(
                        value = batchSearchQuery,
                        onValueChange = { batchSearchQuery = it },
                        placeholder = {
                            Text(
                                text = "Search farmer name or membership ID (e.g. LR-NIM...)",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Offline Search",
                                tint = Color(0xFF0B3D2E)
                            )
                        },
                        trailingIcon = {
                            if (batchSearchQuery.isNotEmpty()) {
                                IconButton(onClick = { batchSearchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = Color.Gray
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF0B3D2E),
                            unfocusedBorderColor = Color(0xFFC8E6C9),
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weigh_batch_tracking_search_input")
                    )

                    if (batchSearchQuery.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color(0xFFE8F5E9),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "⚡ Offline Search: ${sortedBatches.size} matches",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }

                            Text(
                                text = "Clear",
                                fontSize = 11.sp,
                                color = Color(0xFFD32F2F),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { batchSearchQuery = "" }
                            )
                        }
                    }

                    Text(
                        text = "Sort batches by date or farmer name:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    // Sorting Controls
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        item {
                            FilterChip(
                                selected = batchSortOption == "DATE_DESC",
                                onClick = { batchSortOption = "DATE_DESC" },
                                label = { Text("Date (Newest First)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0B3D2E),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("weigh_sort_date_desc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = batchSortOption == "DATE_ASC",
                                onClick = { batchSortOption = "DATE_ASC" },
                                label = { Text("Date (Oldest First)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0B3D2E),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("weigh_sort_date_asc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = batchSortOption == "FARMER_ASC",
                                onClick = { batchSortOption = "FARMER_ASC" },
                                label = { Text("Farmer (A to Z)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0B3D2E),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("weigh_sort_farmer_asc")
                            )
                        }
                        item {
                            FilterChip(
                                selected = batchSortOption == "FARMER_DESC",
                                onClick = { batchSortOption = "FARMER_DESC" },
                                label = { Text("Farmer (Z to A)", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0B3D2E),
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag("weigh_sort_farmer_desc")
                            )
                        }
                    }

                    if (sortedBatches.isEmpty() && batchSearchQuery.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "No Results",
                                tint = Color.Gray,
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No batches found for \"$batchSearchQuery\"",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF0B3D2E)
                            )
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            sortedBatches.forEach { batch ->
                                ProduceBatchCard(
                                    batch = batch,
                                    onQrClick = { onSelectBatchForQr(batch) },
                                    onSpeakClick = {
                                        ttsHelper.speakReceiptSummary(
                                            batchCode = batch.batchCode,
                                            farmerName = batch.farmerName,
                                            cropType = batch.cropType,
                                            weightKg = batch.weightKg,
                                            starchPct = batch.starchPercentage,
                                            payoutLrd = batch.totalPayoutLrd
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (showQrScannerModal) {
            QrCodeScannerModal(
                farmers = farmers,
                onDismiss = { showQrScannerModal = false },
                onFarmerScanned = { scanned ->
                    selectedFarmer = scanned
                    showQrScannerModal = false
                },
                onSwitchToNfc = {
                    showQrScannerModal = false
                    showNfcReaderModal = true
                }
            )
        }

        if (showNfcReaderModal) {
            NfcTagScannerModal(
                farmers = farmers,
                onDismiss = { showNfcReaderModal = false },
                onFarmerScannedWithWeight = { scanned, tagUid, yieldWeight ->
                    customWeightText = "%.1f".format(yieldWeight)
                },
                onFarmerScanned = { scanned, tagUid ->
                    val prevFarmer = selectedFarmer
                    selectedFarmer = scanned
                    lastScannedNfcUid = tagUid
                    isNfcAutoFilledBannerVisible = true
                    showNfcReaderModal = false
                    nfcToastData = NfcScanToastData(
                        title = "⚡ NFC TAG LINKED & AUTO-FILLED",
                        farmerName = scanned.fullName,
                        details = "National ID: ${scanned.nationalId} • Tag UID: $tagUid • Yield: ${customWeightText} kg",
                        tagUid = tagUid,
                        registeredBatch = null,
                        previousFarmer = prevFarmer
                    )
                    ttsHelper.speakText("NFC scan success. Linked farmer ${scanned.fullName} with ${customWeightText} kg yield")
                },
                onOpenScanHistory = {
                    showNfcReaderModal = false
                    showNfcHistoryModal = true
                }
            )
        }

        if (showNfcHistoryModal) {
            com.example.ui.components.NfcScanHistoryModal(
                onDismiss = { showNfcHistoryModal = false },
                onFarmerSelect = { farmerId, farmerName, tagUid ->
                    val match = farmers.firstOrNull { it.id == farmerId || it.fullName.equals(farmerName, ignoreCase = true) || it.nationalId.equals(farmerId, ignoreCase = true) }
                    val selected = match ?: farmers.firstOrNull() ?: com.example.data.db.entities.FarmerEntity(
                        id = farmerId,
                        cooperativeId = "coop-001",
                        cooperativeName = "Ganta District Farmers Co-op",
                        nationalId = farmerId,
                        fullName = farmerName,
                        phoneNumber = "0770001122",
                        momoNumber = "0770001122",
                        gender = "FEMALE",
                        yearOfBirth = 1988,
                        isYouth = false,
                        seedCuttingsAllocated = 500,
                        totalBatchesDelivered = 12,
                        totalEarningsLrd = 45000.0
                    )
                    val prevFarmer = selectedFarmer
                    selectedFarmer = selected
                    lastScannedNfcUid = tagUid
                    isNfcAutoFilledBannerVisible = true
                    nfcToastData = NfcScanToastData(
                        title = "⚡ RECENT SCAN VERIFIED & LINKED",
                        farmerName = selected.fullName,
                        details = "Farmer ID: ${selected.nationalId} • Tag UID: $tagUid",
                        tagUid = tagUid,
                        registeredBatch = null,
                        previousFarmer = prevFarmer
                    )
                    ttsHelper.speakText("Selected farmer ${selected.fullName} from recent scan verification history.")
                    showNfcHistoryModal = false
                }
            )
        }

        if (showVoiceModal) {
            VoiceBatchLoggingModal(
                farmers = farmers,
                onApplyVoiceInputs = { farmer, crop, weight, starch ->
                    if (farmer != null) {
                        selectedFarmer = farmer
                    }
                    selectedCrop = crop
                    customWeightText = "%.1f".format(weight)
                    starchPctText = "%.1f".format(starch)
                    showVoiceModal = false
                },
                onDismiss = { showVoiceModal = false }
            )
        }

        if (showLightweightCsvModal) {
            com.example.ui.components.LightweightBatchExportModal(
                batches = batches,
                onDismiss = { showLightweightCsvModal = false }
            )
        }
    }
}

@Composable
private fun WeightAdjustChip(label: String, onClick: () -> Unit) {
    Surface(
        color = Color(0x22FFFFFF),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun CropChip(label: String, cropKey: String, selectedCrop: String, onSelect: () -> Unit) {
    val isSelected = selectedCrop == cropKey
    Surface(
        color = if (isSelected) Color(0xFF0B3D2E) else Color(0xFFF0F4F2),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onSelect() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFF0B3D2E),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}
