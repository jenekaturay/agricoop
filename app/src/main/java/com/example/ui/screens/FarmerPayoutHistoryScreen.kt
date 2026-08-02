package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.ui.components.QrCodeView
import com.example.ui.components.ThermalReceiptDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.service.TextToSpeechHelper
import com.example.ui.components.SpeakIconButton

@Composable
fun FarmerPayoutHistoryScreen(
    farmers: List<FarmerEntity>,
    batches: List<ProduceBatchEntity>,
    initialMembershipId: String = "",
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }
    var membershipIdInput by remember { mutableStateOf(initialMembershipId) }
    var selectedFarmer by remember(farmers, membershipIdInput) {
        mutableStateOf<FarmerEntity?>(
            farmers.find {
                it.id.equals(membershipIdInput.trim(), ignoreCase = true) ||
                        it.nationalId.equals(membershipIdInput.trim(), ignoreCase = true) ||
                        it.fullName.contains(membershipIdInput.trim(), ignoreCase = true)
            } ?: farmers.firstOrNull()
        )
    }

    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, PAID, PENDING, PROCESSING
    var selectedBatchForReceipt by remember { mutableStateOf<ProduceBatchEntity?>(null) }
    var showMemberQrModal by remember { mutableStateOf(false) }
    var showLightweightCsvModal by remember { mutableStateOf(false) }

    // Find farmer's batches
    val farmerBatches = remember(selectedFarmer, batches, selectedStatusFilter) {
        if (selectedFarmer == null) emptyList()
        else {
            batches.filter { batch ->
                val isFarmerMatch = batch.farmerId.equals(selectedFarmer?.id, ignoreCase = true) ||
                        batch.farmerName.equals(selectedFarmer?.fullName, ignoreCase = true)
                val isStatusMatch = when (selectedStatusFilter) {
                    "PAID" -> batch.payoutStatus == "PAID"
                    "PENDING" -> batch.payoutStatus == "PENDING"
                    "PROCESSING" -> batch.payoutStatus == "PROCESSING"
                    else -> true
                }
                isFarmerMatch && isStatusMatch
            }.sortedByDescending { it.timestamp }
        }
    }

    val totalPaidLrd = farmerBatches.filter { it.payoutStatus == "PAID" }.sumOf { it.totalPayoutLrd }
    val totalPendingLrd = farmerBatches.filter { it.payoutStatus == "PENDING" || it.payoutStatus == "PROCESSING" }.sumOf { it.totalPayoutLrd }
    val totalTonnageKg = farmerBatches.sumOf { it.weightKg }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9F7))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (onBack != null) {
                        IconButton(onClick = onBack, modifier = Modifier.testTag("payout_history_back_button")) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color(0xFF0B3D2E))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Surface(
                        color = Color(0xFFE8F5E9),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = Color(0xFF0B3D2E),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Farmer Payout History",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color(0xFF0B3D2E)
                        )
                        Text(
                            text = "Membership ID Self-Service Portal",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }

                Badge(containerColor = Color(0xFF0B3D2E), contentColor = Color(0xFFFFD54F)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("MoMo Audit", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Membership ID Lookup Bar Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("membership_id_lookup_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Enter Member ID or National ID",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B3D2E)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = membershipIdInput,
                            onValueChange = { input ->
                                membershipIdInput = input
                                val found = farmers.find {
                                    it.id.equals(input.trim(), ignoreCase = true) ||
                                            it.nationalId.equals(input.trim(), ignoreCase = true) ||
                                            it.fullName.contains(input.trim(), ignoreCase = true)
                                }
                                if (found != null) selectedFarmer = found
                            },
                            placeholder = { Text("e.g., farm-001 or LR-NIM-88412", fontSize = 12.sp) },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null, tint = Color(0xFF0B3D2E)) },
                            trailingIcon = {
                                if (membershipIdInput.isNotEmpty()) {
                                    IconButton(onClick = { membershipIdInput = "" }) {
                                        Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("membership_id_input_field")
                        )

                        Button(
                            onClick = {
                                val found = farmers.find {
                                    it.id.equals(membershipIdInput.trim(), ignoreCase = true) ||
                                            it.nationalId.equals(membershipIdInput.trim(), ignoreCase = true) ||
                                            it.fullName.contains(membershipIdInput.trim(), ignoreCase = true)
                                }
                                if (found != null) selectedFarmer = found
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("lookup_payout_button")
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "Lookup", tint = Color(0xFFFFD54F))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Lookup", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Quick Select Member Account:",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(farmers.take(6)) { farmer ->
                            val isSelected = selectedFarmer?.id == farmer.id
                            Surface(
                                color = if (isSelected) Color(0xFF0B3D2E) else Color(0xFFF1F8E9),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) Color(0xFFFFD54F) else Color(0xFFC8E6C9)),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier
                                    .clickable {
                                        selectedFarmer = farmer
                                        membershipIdInput = farmer.id
                                    }
                                    .testTag("quick_member_chip_${farmer.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = if (isSelected) Color(0xFFFFD54F) else Color(0xFF2E7D32),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${farmer.fullName} (${farmer.id})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else Color(0xFF0B3D2E)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Farmer Profile Card
        selectedFarmer?.let { farmer ->
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3D2E)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("farmer_profile_summary_card")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFFFD54F),
                                    shape = CircleShape,
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = farmer.fullName.take(1),
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 20.sp,
                                            color = Color(0xFF0B3D2E)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = farmer.fullName,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = "${farmer.cooperativeName} • ID: ${farmer.id}",
                                        color = Color(0xFF80CBC4),
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            IconButton(
                                onClick = { showMemberQrModal = true },
                                modifier = Modifier.testTag("show_member_qr_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode,
                                    contentDescription = "Digital Membership QR",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0x33FFFFFF))
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("NATIONAL ID", fontSize = 9.sp, color = Color(0xFF80CBC4), letterSpacing = 1.sp)
                                Text(farmer.nationalId, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Column {
                                Text("MOMO WALLET", fontSize = 9.sp, color = Color(0xFF80CBC4), letterSpacing = 1.sp)
                                Text(farmer.momoNumber, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text("SEED ALLOCATION", fontSize = 9.sp, color = Color(0xFF80CBC4), letterSpacing = 1.sp)
                                Text("${farmer.seedCuttingsAllocated} Cuttings", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD54F))
                            }
                        }
                    }
                }
            }

            // Summary Payout Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PAID OUT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${"%,.0f".format(totalPaidLrd)} LRD",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF2E7D32)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.HourglassTop, contentDescription = null, tint = Color(0xFFD84315), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("PENDING/QUEUED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${"%,.0f".format(totalPendingLrd)} LRD",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFD84315)
                            )
                        }
                    }

                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Grass, contentDescription = null, tint = Color(0xFF0B3D2E), modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("TONNAGE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${"%,.0f".format(totalTonnageKg)} Kg",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0B3D2E)
                            )
                        }
                    }
                }
            }

            // Filter Tabs Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Payout Records (${farmerBatches.size})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0B3D2E)
                        )

                        OutlinedButton(
                            onClick = { showLightweightCsvModal = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0B3D2E)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF0B3D2E)),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("export_payout_history_csv_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Export CSV",
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Export CSV (Bluetooth)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Filter status:", fontSize = 11.sp, color = Color.Gray)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("ALL", "PAID", "PENDING", "PROCESSING").forEach { filter ->
                                Surface(
                                    color = if (selectedStatusFilter == filter) Color(0xFF0B3D2E) else Color(0xFFE8F5E9),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .clickable { selectedStatusFilter = filter }
                                        .testTag("payout_status_filter_$filter")
                                ) {
                                    Text(
                                        text = filter,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedStatusFilter == filter) Color.White else Color(0xFF2E7D32),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payout Batches List
            if (farmerBatches.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No payout records found matching filter '$selectedStatusFilter'.",
                                fontSize = 12.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(farmerBatches) { batch ->
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US) }
                    val dateStr = dateFormat.format(Date(batch.timestamp))

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("payout_batch_card_${batch.batchCode}")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = when (batch.payoutStatus) {
                                            "PAID" -> Color(0xFFE8F5E9)
                                            "PROCESSING" -> Color(0xFFFFF8E1)
                                            else -> Color(0xFFFBE9E7)
                                        },
                                        shape = CircleShape,
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = when (batch.payoutStatus) {
                                                    "PAID" -> Icons.Default.CheckCircle
                                                    "PROCESSING" -> Icons.Default.HourglassTop
                                                    else -> Icons.Default.AccountBalanceWallet
                                                },
                                                contentDescription = null,
                                                tint = when (batch.payoutStatus) {
                                                    "PAID" -> Color(0xFF2E7D32)
                                                    "PROCESSING" -> Color(0xFFF57F17)
                                                    else -> Color(0xFFD84315)
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = batch.batchCode,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = Color(0xFF0B3D2E)
                                        )
                                        Text(
                                            text = dateStr,
                                            fontSize = 10.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                Badge(
                                    containerColor = when (batch.payoutStatus) {
                                        "PAID" -> Color(0xFFE8F5E9)
                                        "PROCESSING" -> Color(0xFFFFF8E1)
                                        else -> Color(0xFFFBE9E7)
                                    },
                                    contentColor = when (batch.payoutStatus) {
                                        "PAID" -> Color(0xFF2E7D32)
                                        "PROCESSING" -> Color(0xFFF57F17)
                                        else -> Color(0xFFD84315)
                                    }
                                ) {
                                    Text(
                                        text = batch.payoutStatus,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Batch Weighing & Rate Details
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("CROP TYPE & WEIGHT", fontSize = 9.sp, color = Color.Gray)
                                    Text(
                                        text = "${batch.cropType} • ${batch.weightKg} Kg",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B3D2E)
                                    )
                                }

                                Column {
                                    Text("QUALITY & STARCH", fontSize = 9.sp, color = Color.Gray)
                                    Text(
                                        text = "${batch.starchPercentage}% Starch",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0B3D2E)
                                    )
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("NET PAYOUT", fontSize = 9.sp, color = Color.Gray)
                                    Text(
                                        text = "${"%,.0f".format(batch.totalPayoutLrd)} LRD",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Transaction Ref Footer
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (batch.momoTransactionRef.isNotEmpty()) "Ref: ${batch.momoTransactionRef}" else "Pending LoneStar MTN/Orange MoMo Sync",
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = Color.Gray
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    SpeakIconButton(
                                        onClick = {
                                            ttsHelper.speakReceiptSummary(
                                                batchCode = batch.batchCode,
                                                farmerName = batch.farmerName,
                                                cropType = batch.cropType,
                                                weightKg = batch.weightKg,
                                                starchPct = batch.starchPercentage,
                                                payoutLrd = batch.totalPayoutLrd
                                            )
                                        },
                                        contentDescription = "Read payout aloud",
                                        tint = Color(0xFF0D47A1),
                                        testTag = "payout_tts_speak_${batch.batchCode}"
                                    )

                                    OutlinedButton(
                                        onClick = { selectedBatchForReceipt = batch },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .height(32.dp)
                                            .testTag("print_batch_receipt_button_${batch.batchCode}")
                                    ) {
                                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(12.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Receipt", fontSize = 10.sp)
                                    }
                                }
                            }

                            if (batch.payoutStatus == "PENDING" || batch.payoutStatus == "PROCESSING") {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = Color(0xFFFFF3E0),
                                    shape = RoundedCornerShape(6.dp),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFFFFB74D)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("payout_pending_confirmation_alert_${batch.id}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.NotificationsActive,
                                                contentDescription = null,
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
                                            text = "Awaiting Gateway Ref",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFFD84315)
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

    // Modal Receipt View
    selectedBatchForReceipt?.let { batch ->
        ThermalReceiptDialog(
            batch = batch,
            onDismiss = { selectedBatchForReceipt = null },
            onMoMoTrigger = { }
        )
    }

    // Digital Member ID QR Modal
    if (showMemberQrModal && selectedFarmer != null) {
        val farmer = selectedFarmer!!
        androidx.compose.ui.window.Dialog(onDismissRequest = { showMemberQrModal = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("digital_member_id_qr_modal")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Digital Co-op Membership Pass",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B3D2E)
                    )
                    Text(
                        text = "Present at Scale Weighstation",
                        fontSize = 11.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    QrCodeView(
                        data = "AGRICOOP_MEMBER:${farmer.id}:${farmer.nationalId}:${farmer.fullName}",
                        modifier = Modifier.size(180.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = farmer.fullName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF0B3D2E)
                    )
                    Text(
                        text = "ID: ${farmer.id} • ${farmer.cooperativeName}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showMemberQrModal = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close Digital Pass")
                    }
                }
            }
        }

        if (showLightweightCsvModal) {
            com.example.ui.components.LightweightBatchExportModal(
                batches = if (farmerBatches.isNotEmpty()) farmerBatches else batches,
                onDismiss = { showLightweightCsvModal = false }
            )
        }
    }
}
