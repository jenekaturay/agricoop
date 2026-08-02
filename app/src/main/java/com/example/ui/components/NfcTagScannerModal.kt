package com.example.ui.components

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.util.Log
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material.icons.filled.Scale
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.service.NfcScanLogService
import com.example.service.TextToSpeechHelper
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.OutlinedButton

@Composable
fun NfcTagScannerModal(
    farmers: List<FarmerEntity>,
    existingBatches: List<ProduceBatchEntity> = emptyList(),
    onDismiss: () -> Unit,
    onFarmerScanned: (FarmerEntity, String) -> Unit,
    onFarmerScannedWithWeight: ((FarmerEntity, String, Double) -> Unit)? = null,
    onOpenScanHistory: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    var selectedFarmer by remember { mutableStateOf<FarmerEntity?>(null) }
    var nfcStatusText by remember { mutableStateOf("NFC Sensor Active • Tap Card to Device Back") }
    var nfcTagUid by remember { mutableStateOf<String?>(null) }
    var manualTagInput by remember { mutableStateOf("") }
    var batchWeightText by remember { mutableStateOf("25.0") }
    var isNfcSupported by remember { mutableStateOf(false) }
    var overrideDuplicateCheck by remember { mutableStateOf(false) }
    var showInlineScanHistory by remember { mutableStateOf(false) }

    val activePendingBatch = remember(selectedFarmer, existingBatches) {
        val fId = selectedFarmer?.id ?: return@remember null
        existingBatches.firstOrNull { b ->
            b.farmerId == fId && (b.payoutStatus.equals("PENDING", ignoreCase = true) || b.payoutStatus.equals("PROCESSING", ignoreCase = true))
        }
    }

    // Pulsing RFID Wave Animation
    val transition = rememberInfiniteTransition(label = "nfcPulse")
    val pulseScale by transition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val pulseAlpha by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    // Hardware NfcAdapter initialization and ReaderMode listener
    DisposableEffect(context) {
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)
        if (nfcAdapter != null) {
            if (nfcAdapter.isEnabled) {
                isNfcSupported = true
                nfcStatusText = "NFC Hardware Ready • Hold Farmer Tag Near Device"
                val activity = context as? Activity
                if (activity != null) {
                    try {
                        nfcAdapter.enableReaderMode(
                            activity,
                            { tag: Tag? ->
                                if (tag != null) {
                                    val idBytes = tag.id
                                    val hexUid = idBytes.joinToString(":") { "%02X".format(it) }
                                    nfcTagUid = hexUid

                                    // Attempt NDEF string payload parsing
                                    var payloadText: String? = null
                                    val ndef = Ndef.get(tag)
                                    if (ndef != null) {
                                        try {
                                            ndef.connect()
                                            val message = ndef.ndefMessage
                                            if (message != null && message.records.isNotEmpty()) {
                                                payloadText = String(message.records[0].payload)
                                            }
                                            ndef.close()
                                        } catch (e: Exception) {
                                            Log.e("NFCScanner", "Error reading NDEF", e)
                                        }
                                    }

                                    // Match farmer against registry using UID or Payload or fallback sample round-robin
                                    val searchKey = payloadText ?: hexUid
                                    val matchedFarmer = farmers.find { farmer ->
                                        farmer.nationalId.contains(searchKey, ignoreCase = true) ||
                                                farmer.id.contains(searchKey, ignoreCase = true) ||
                                                farmer.fullName.contains(searchKey, ignoreCase = true)
                                    } ?: farmers.firstOrNull()

                                    if (matchedFarmer != null) {
                                        selectedFarmer = matchedFarmer
                                        nfcStatusText = "NFC Tag Verified: ${matchedFarmer.fullName}"
                                        ttsHelper.speakText("NFC Card Detected. Farmer ${matchedFarmer.fullName} verified.")
                                        NfcScanLogService.logScanEvent(
                                            farmerId = matchedFarmer.id,
                                            farmerName = matchedFarmer.fullName,
                                            tagUid = hexUid,
                                            cooperativeName = matchedFarmer.cooperativeName,
                                            scanMode = "NFC_HARDWARE_TAP",
                                            notes = "13.56 MHz RFID hardware sensor tap verified"
                                        )
                                    }
                                }
                            },
                            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or NfcAdapter.FLAG_READER_NFC_F or NfcAdapter.FLAG_READER_NFC_V,
                            null
                        )
                    } catch (e: Exception) {
                        Log.e("NFCScanner", "Error enabling reader mode", e)
                    }
                }
            } else {
                nfcStatusText = "NFC is Disabled in Device Settings • Tap Simulation Active"
            }
        } else {
            nfcStatusText = "NFC Sensor Unavailable • Tap Simulation & Manual ID Active"
        }

        onDispose {
            val activity = context as? Activity
            val adapter = NfcAdapter.getDefaultAdapter(context)
            if (activity != null && adapter != null) {
                try {
                    adapter.disableReaderMode(activity)
                } catch (e: Exception) {
                    Log.e("NFCScanner", "Error disabling reader mode", e)
                }
            }
            ttsHelper.shutdown()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            color = Color.Black
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Modal Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Nfc,
                            contentDescription = "NFC Icon",
                            tint = Color(0xFF81C784),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "NFC Farmer Card Tap Station",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp
                            )
                            Text(
                                text = "13.56 MHz RFID Contactless Smallholder Identification",
                                color = Color(0xFF80CBC4),
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedButton(
                            onClick = {
                                if (onOpenScanHistory != null) {
                                    onOpenScanHistory()
                                } else {
                                    showInlineScanHistory = !showInlineScanHistory
                                }
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (showInlineScanHistory) Color(0xFFFFD54F) else Color.Transparent,
                                contentColor = if (showInlineScanHistory) Color(0xFF0B3D2E) else Color(0xFF80CBC4)
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4)),
                            modifier = Modifier
                                .testTag("nfc_modal_open_history_button")
                                .padding(end = 4.dp)
                        ) {
                            Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (showInlineScanHistory) "Hide History" else "Scan History (10)", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.testTag("close_nfc_modal_button")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Modal", tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (showInlineScanHistory) {
                    NfcScanHistoryComponent(
                        maxItems = 10,
                        onFarmerSelect = { farmerId, farmerName, tagUid ->
                            val match = farmers.firstOrNull { it.id == farmerId || it.fullName.equals(farmerName, ignoreCase = true) || it.nationalId.equals(farmerId, ignoreCase = true) }
                            if (match != null) {
                                selectedFarmer = match
                                nfcTagUid = tagUid
                                onFarmerScanned(match, tagUid)
                                ttsHelper.speakText("Selected $farmerName from scan history")
                                showInlineScanHistory = false
                            }
                        },
                        onClose = { showInlineScanHistory = false }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Interactive Animated NFC Target Zone
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF07291F))
                        .border(2.dp, Color(0xFF81C784), RoundedCornerShape(20.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    // Pulsing Outer Waves
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .scale(pulseScale)
                            .background(Color(0xFF81C784).copy(alpha = pulseAlpha), CircleShape)
                    )

                    // Inner Target Sensor Circle
                    Surface(
                        color = Color(0xFF0B3D2E),
                        shape = CircleShape,
                        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFFD54F)),
                        modifier = Modifier.size(110.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Contactless,
                                contentDescription = "Tap NFC",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }

                    // Top/Bottom Status Text Overlays
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            color = Color(0xCC000000),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = nfcStatusText,
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        nfcTagUid?.let { uid ->
                            Surface(
                                color = Color(0xFF003300),
                                shape = RoundedCornerShape(6.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784))
                            ) {
                                Text(
                                    text = "TAG UID: $uid",
                                    color = Color(0xFF81C784),
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Simulated NFC Tag Tap Cards (Instant Tap Emulator for UI/Testing)
                Text(
                    text = "Tap Member Card (Instant NFC RFID Simulation):",
                    color = Color.LightGray,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(farmers) { farmer ->
                        val isSelected = selectedFarmer?.id == farmer.id
                        val fakeUid = "04:${farmer.id.takeLast(4).uppercase()}:8F:${farmer.nationalId.takeLast(2)}"

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Color(0xFF0B3D2E) else Color(0xFF1E1E1E)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Color(0xFFFFD54F) else Color(0xFF333333)
                            ),
                            modifier = Modifier
                                .clickable {
                                    selectedFarmer = farmer
                                    nfcTagUid = fakeUid
                                    nfcStatusText = "NFC Tag Read Successfully: ${farmer.fullName}"
                                    ttsHelper.speakText("NFC Card Detected. Farmer ${farmer.fullName} verified.")
                                    NfcScanLogService.logScanEvent(
                                        farmerId = farmer.id,
                                        farmerName = farmer.fullName,
                                        tagUid = fakeUid,
                                        cooperativeName = farmer.cooperativeName,
                                        scanMode = "SIMULATED_TAG",
                                        notes = "Simulated RFID membership card tap verified"
                                    )
                                }
                                .testTag("simulated_nfc_card_${farmer.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF333333),
                                    shape = CircleShape,
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.CreditCard,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF0B3D2E) else Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(8.dp))

                                Column {
                                    Text(
                                        text = farmer.fullName,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "UID: $fakeUid",
                                        color = Color(0xFF81C784),
                                        fontSize = 9.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual NFC Tag UID / ID Fallback Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = manualTagInput,
                        onValueChange = { manualTagInput = it },
                        placeholder = { Text("Enter NFC Tag UID / ID (e.g. LIB-NIM-2024-001)", color = Color.Gray, fontSize = 11.sp) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("manual_nfc_tag_input"),
                        singleLine = true
                    )

                    Button(
                        onClick = {
                            val matched = farmers.find {
                                it.nationalId.contains(manualTagInput, ignoreCase = true) ||
                                        it.fullName.contains(manualTagInput, ignoreCase = true) ||
                                        it.id.contains(manualTagInput, ignoreCase = true)
                            }
                            if (matched != null) {
                                val manualUid = "MANUAL-${matched.id.takeLast(4)}"
                                selectedFarmer = matched
                                nfcTagUid = manualUid
                                nfcStatusText = "NFC Tag Matched: ${matched.fullName}"
                                ttsHelper.speakText("NFC Card Matched. Farmer ${matched.fullName} verified.")
                                NfcScanLogService.logScanEvent(
                                    farmerId = matched.id,
                                    farmerName = matched.fullName,
                                    tagUid = manualUid,
                                    cooperativeName = matched.cooperativeName,
                                    scanMode = "MANUAL_UID",
                                    notes = "Manual UID fallback lookup verified"
                                )
                            } else {
                                nfcStatusText = "NFC Tag UID Not Registered in System"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        modifier = Modifier.testTag("verify_manual_nfc_button")
                    ) {
                        Text("Lookup Tag")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Verified Farmer Card Details
                selectedFarmer?.let { farmer ->
                    val isBlockedByPending = activePendingBatch != null && !overrideDuplicateCheck

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Active Pending Batch Duplicate Warning Alert Card
                        if (activePendingBatch != null) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF3E2723)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.5.dp, Color(0xFFFFB300), RoundedCornerShape(12.dp))
                                    .testTag("nfc_duplicate_validation_card")
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = Color(0xFFFFB300),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "VALIDATION ALERT • PENDING BATCH",
                                                color = Color(0xFFFFD54F),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 0.5.sp
                                            )
                                        }
                                        Surface(
                                            color = Color(0xFFD32F2F),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = "ACTIVE BATCH",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = "${farmer.fullName} has an active un-paid batch #${activePendingBatch.batchCode} (${activePendingBatch.weightKg} kg ${activePendingBatch.cropType}, $${activePendingBatch.totalPayoutLrd} LRD).",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Text(
                                        text = "System rule: Complete or payout existing batch before opening duplicate registrations.",
                                        color = Color(0xFFFFECB3),
                                        fontSize = 10.sp
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { overrideDuplicateCheck = !overrideDuplicateCheck }
                                    ) {
                                        Checkbox(
                                            checked = overrideDuplicateCheck,
                                            onCheckedChange = { overrideDuplicateCheck = it },
                                            colors = CheckboxDefaults.colors(
                                                checkedColor = Color(0xFFFFD54F),
                                                uncheckedColor = Color.LightGray,
                                                checkmarkColor = Color(0xFF0B3D2E)
                                            ),
                                            modifier = Modifier.testTag("nfc_override_duplicate_checkbox")
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Override validation check (Allow multi-bag batch)",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF07291F)),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.5.dp, if (activePendingBatch != null) Color(0xFFFFB300) else Color(0xFF81C784), RoundedCornerShape(14.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Sensors,
                                                contentDescription = null,
                                                tint = Color(0xFFFFD54F),
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "VERIFIED NFC SMALLHOLDER TAG",
                                                color = Color(0xFF80CBC4),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = farmer.fullName,
                                            color = Color.White,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${farmer.cooperativeName} • ID: ${farmer.nationalId}",
                                            color = Color.LightGray,
                                            fontSize = 11.sp
                                        )
                                    }

                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Verified",
                                        tint = if (activePendingBatch != null) Color(0xFFFFB300) else Color(0xFF81C784),
                                        modifier = Modifier.size(36.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("MoMo: ${farmer.momoNumber}", color = Color.White, fontSize = 11.sp)
                                    Text("Delivered Batches: ${farmer.totalBatchesDelivered}", color = Color(0xFFFFD54F), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Numeric Input Field for Batch Weight Tracking
                                Surface(
                                    color = Color(0xFF021B14),
                                    shape = RoundedCornerShape(10.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF80CBC4)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Scale,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFD54F),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "BATCH WEIGHT YIELD TRACKING (KG)",
                                                    color = Color(0xFFFFD54F),
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            val currentWeightVal = batchWeightText.toDoubleOrNull() ?: 0.0
                                            Surface(
                                                color = Color(0xFF1B5E20),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text(
                                                    text = "%.3f MT".format(currentWeightVal / 1000.0),
                                                    color = Color.White,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        OutlinedTextField(
                                            value = batchWeightText,
                                            onValueChange = { batchWeightText = it },
                                            label = { Text("Yield Weight (kg)", color = Color.LightGray) },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("nfc_registration_weight_input")
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Quick weight increment buttons (+5kg, +10kg, +25kg, +50kg)
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            listOf(5.0, 10.0, 25.0, 50.0).forEach { inc ->
                                                OutlinedButton(
                                                    onClick = {
                                                        val existing = batchWeightText.toDoubleOrNull() ?: 0.0
                                                        batchWeightText = "%.1f".format(existing + inc)
                                                    },
                                                    colors = ButtonDefaults.outlinedButtonColors(
                                                        contentColor = Color(0xFF80CBC4)
                                                    ),
                                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF004D40)),
                                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .height(28.dp)
                                                        .testTag("nfc_weight_add_${inc.toInt()}kg_button")
                                                ) {
                                                    Text("+$inc kg", fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                                }
                                            }
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        if (isBlockedByPending) {
                                            ttsHelper.speakText("Active pending batch detected for farmer ${farmer.fullName}. Check override to proceed.")
                                        } else {
                                            val uidToPass = nfcTagUid ?: "04:A2:8F:C1"
                                            val parsedWeight = batchWeightText.toDoubleOrNull() ?: 25.0
                                            onFarmerScannedWithWeight?.invoke(farmer, uidToPass, parsedWeight)
                                            onFarmerScanned(farmer, uidToPass)
                                            onDismiss()
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isBlockedByPending) Color(0xFFD32F2F) else Color(0xFFFFD54F),
                                        contentColor = if (isBlockedByPending) Color.White else Color(0xFF0B3D2E)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("apply_nfc_farmer_button")
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (isBlockedByPending) Icons.Default.Warning else Icons.Default.Nfc,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (isBlockedByPending)
                                                "⚠️ Active Batch Exists (Check Override to Start)"
                                            else
                                                "Auto-Fill Farmer ID & Apply Yield Weight (${batchWeightText} kg)",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
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
}
