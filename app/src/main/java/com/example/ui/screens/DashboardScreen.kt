package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.BorderStroke
import com.example.ui.components.SpeakIconButton
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.service.AppLanguage
import com.example.service.HarvestPredictionResult
import androidx.compose.material.icons.filled.Translate
import com.example.ui.components.GeminiHarvestTrendCard
import com.example.ui.components.PendingUnsyncedSummaryCard
import com.example.ui.components.SecurityStatusDashboardComponent
import com.example.ui.components.WeeklyTonnageChartComponent

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.service.TextToSpeechHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    batches: List<ProduceBatchEntity>,
    farmers: List<FarmerEntity>,
    cooperatives: List<CooperativeEntity>,
    hubs: List<HubOperationEntity>,
    momoFloats: List<MoMoFloatEntity>,
    unsyncedCount: Int,
    isPredictingHarvestTrends: Boolean = false,
    harvestPredictionResult: HarvestPredictionResult? = null,
    predictionError: String? = null,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onOpenLanguageDialog: () -> Unit = {},
    onRunHarvestPrediction: () -> Unit = {},
    onNavigateTab: (Int) -> Unit,
    onSelectBatchForQr: (ProduceBatchEntity) -> Unit,
    onTriggerSync: () -> Unit,
    onOpenBiometricAuth: () -> Unit = {}
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    val totalWeightKg = batches.sumOf { it.weightKg }
    val totalPayoutLrd = batches.sumOf { it.totalPayoutLrd }
    val totalFemaleFarmers = farmers.count { it.gender.equals("FEMALE", ignoreCase = true) }
    val femalePercentage = if (farmers.isNotEmpty()) (totalFemaleFarmers * 100) / farmers.size else 55
    val totalYouthFarmers = farmers.count { it.isYouth }
    val youthPercentage = if (farmers.isNotEmpty()) (totalYouthFarmers * 100) / farmers.size else 35

    val now = System.currentTimeMillis()
    val twentyFourHoursMs = 24 * 60 * 60 * 1000L
    val overdueBatches = remember(batches) {
        batches.filter { it.payoutStatus == "PENDING" && (now - it.timestamp) >= twentyFourHoursMs }
    }

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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9F7))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF0B3D2E), Color(0xFF1B604A), Color(0xFF228B62))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "AGRICOOP LIBERIA",
                                    color = Color(0xFFFFD54F),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 1.sp
                                )
                                Text(
                                    text = "Ganta & Voinjama Hub Operations",
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Surface(
                                color = Color(0x33FFFFFF),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SolarPower,
                                        contentDescription = null,
                                        tint = Color(0xFFFFD54F),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Solar PV Active",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Stats Summary Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HeroStatItem(
                                label = "Total Sourced",
                                value = "%.1f Tons".format(totalWeightKg / 1000.0)
                            )
                            HeroStatItem(
                                label = "MoMo Dispatched",
                                value = "LRD $%.0f".format(totalPayoutLrd)
                            )
                            HeroStatItem(
                                label = "Active FBOs",
                                value = "${cooperatives.size} Co-ops"
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Sync Status Bar inside Hero
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0x22000000), RoundedCornerShape(10.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (unsyncedCount > 0) Icons.Default.Warning else Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = if (unsyncedCount > 0) Color(0xFFFFD54F) else Color(0xFF81C784),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (unsyncedCount > 0) "$unsyncedCount Batches Queued Offline" else "All Batches Synced to Cloud",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            if (unsyncedCount > 0) {
                                Button(
                                    onClick = onTriggerSync,
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C)),
                                    modifier = Modifier.testTag("hero_sync_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Auto-Sync", fontSize = 11.sp, color = Color.Black)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Summary Dashboard Element for Pending Unsynced Records
        item {
            PendingUnsyncedSummaryCard(
                batches = batches,
                onTriggerSync = onTriggerSync,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Security Status UI Dashboard Component
        item {
            SecurityStatusDashboardComponent(
                onTriggerBiometricModal = onOpenBiometricAuth,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Language & Local Liberian Dialects Accessibility Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("dashboard_language_dialect_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = CircleShape,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = currentLanguage.flagEmoji, fontSize = 20.sp)
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Language: ${currentLanguage.displayName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Color(0xFF0B3D2E)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                                    Text(currentLanguage.regionName, fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                }
                            }
                            Text(
                                text = "\"${currentLanguage.sampleGreeting}\"",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    Button(
                        onClick = onOpenLanguageDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("switch_dialect_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Switch Dialect",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Switch Dialect", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
        }

        // Overdue Pending Tuber Batches Alert Banner (>24 Hours Pending)
        if (overdueBatches.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("overdue_pending_batches_alert_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = Color(0xFFE65100),
                                    shape = CircleShape,
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Warning,
                                            contentDescription = "Overdue Alert",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "⚠️ Overdue Pending Tuber Batches",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFBF360C)
                                    )
                                    Text(
                                        text = "Pending processing for over 24 hours",
                                        fontSize = 10.sp,
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }

                            Surface(
                                color = Color(0xFFD84315),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${overdueBatches.size} OVERDUE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Text(
                            text = "Background notification system flagged these batches to ensure timely processing and prevent loss for smallholder farmers.",
                            fontSize = 11.sp,
                            color = Color(0xFF4E342E)
                        )

                        overdueBatches.forEach { overdue ->
                            val hrsPending = (now - overdue.timestamp) / (1000 * 60 * 60)
                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectBatchForQr(overdue) }
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
                                            text = "${overdue.batchCode} (${overdue.farmerName})",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = Color(0xFF0B3D2E)
                                        )
                                        Text(
                                            text = "Crop: ${overdue.cropType} • Weight: ${overdue.weightKg} kg • Payout: LRD $%.2f".format(overdue.totalPayoutLrd),
                                            fontSize = 10.sp,
                                            color = Color.DarkGray
                                        )
                                    }
                                    Surface(
                                        color = Color(0xFFFFEBEE),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${hrsPending}h Pending",
                                            color = Color(0xFFC62828),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Dashboard Summary: Weekly Tuber Tonnage Processed (Room SQLite DB) Chart
        item {
            WeeklyTonnageChartComponent(
                batches = batches,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Machine Learning Gemini AI Harvest Prediction Card
        item {
            GeminiHarvestTrendCard(
                isPredicting = isPredictingHarvestTrends,
                predictionResult = harvestPredictionResult,
                predictionError = predictionError,
                totalBatchesCount = batches.size,
                onRunPrediction = onRunHarvestPrediction,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Quick Actions Row
        item {
            Text(
                text = "Quick Field Operations",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0B3D2E)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    title = "Weigh & Batch",
                    subtitle = "Bluetooth Scale & QR",
                    icon = Icons.Default.Scale,
                    color = Color(0xFF0B3D2E),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_weigh_batch_button"),
                    onClick = { onNavigateTab(1) }
                )
                QuickActionTile(
                    title = "Onboard Farmer",
                    subtitle = "TME 419 Seed Kit",
                    icon = Icons.Default.People,
                    color = Color(0xFF1B604A),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_onboard_farmer_button"),
                    onClick = { onNavigateTab(2) }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                QuickActionTile(
                    title = "Ganta Solar Hub",
                    subtitle = "100-Mesh Sifter QC",
                    icon = Icons.Default.SolarPower,
                    color = Color(0xFF8D5B4C),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_hub_qc_button"),
                    onClick = { onNavigateTab(3) }
                )
                QuickActionTile(
                    title = "Sync & MoMo",
                    subtitle = "Low-Bandwidth Queue",
                    icon = Icons.Default.CloudUpload,
                    color = Color(0xFFE5A93C),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("action_sync_momo_button"),
                    onClick = { onNavigateTab(4) }
                )
            }
        }

        // Donor Inclusivity Metrics Card (55% Female, 35% Youth Target Tracker)
        item {
            OutlinedCard(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.outlinedCardColors(containerColor = Color.White),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("donor_metrics_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Donor Impact & Demographics (USADF / World Bank)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B3D2E)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Female Target
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Women Smallholders (Target 55%):", fontSize = 12.sp)
                        Text("$femalePercentage% ($totalFemaleFarmers / ${farmers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0B3D2E))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { femalePercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFF228B62),
                        trackColor = Color(0xFFE0E0E0)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Youth Target
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Youth Operators (Ages 18-35, Target 35%):", fontSize = 12.sp)
                        Text("$youthPercentage% ($totalYouthFarmers / ${farmers.size})", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFFE5A93C))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { youthPercentage / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = Color(0xFFE5A93C),
                        trackColor = Color(0xFFE0E0E0)
                    )
                }
            }
        }

        // Ganta Processing Hub Status Summary
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = Color(0xFF0B3D2E)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Ganta Processing Hub",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                        Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                            Text("100-Mesh Pass (<10% Moist)", modifier = Modifier.padding(4.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val gantaHub = hubs.find { it.hubName.contains("Ganta") }
                    if (gantaHub != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            HubStatBox("Solar PV", "${gantaHub.solarCapacityKw} kW", Icons.Default.SolarPower)
                            HubStatBox("HQCF Flour", "%.1f Tons".format(gantaHub.hqcfYieldTons), Icons.Default.Agriculture)
                            HubStatBox("Ind. Starch", "%.1f Tons".format(gantaHub.industrialStarchTons), Icons.Default.MonetizationOn)
                            HubStatBox("Feed Pellets", "%.1f Tons".format(gantaHub.animalFeedTons), Icons.Default.LocalShipping)
                        }
                    }
                }
            }
        }

        // Recent Harvest Batches Header & Sorted/Filtered Tracking List
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            text = "Batch Tracking List",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B3D2E)
                        )
                    }
                    Text(
                        text = "Total Batches (${batches.size})",
                        fontSize = 12.sp,
                        color = Color(0xFF228B62),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateTab(1) }
                    )
                }

                // Offline Farmer & Membership ID Search Bar
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
                        .testTag("batch_tracking_search_input")
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
                                text = "⚡ Offline Room Search: Showing ${sortedBatches.size} of ${batches.size} batches",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Clear filter",
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
                            modifier = Modifier.testTag("sort_date_desc")
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
                            modifier = Modifier.testTag("sort_date_asc")
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
                            modifier = Modifier.testTag("sort_farmer_asc")
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
                            modifier = Modifier.testTag("sort_farmer_desc")
                        )
                    }
                }
            }
        }

        if (sortedBatches.isEmpty() && batchSearchQuery.isNotEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "No Results",
                            tint = Color.Gray,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No batches found matching \"$batchSearchQuery\"",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF0B3D2E)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Try searching by farmer full name (e.g. Massaboi) or membership/national ID (e.g. LR-NIM...)",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }

        items(sortedBatches) { batch ->
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

@Composable
private fun HeroStatItem(label: String, value: String) {
    Column {
        Text(text = label, color = Color(0xFFB0BEC5), fontSize = 11.sp)
        Text(text = value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = modifier.clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    color = Color(0xFFE0E0E0),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun HubStatBox(label: String, value: String, icon: ImageVector) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFFF0F4F2), RoundedCornerShape(10.dp))
            .padding(8.dp)
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF0B3D2E), modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = value, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0B3D2E))
        Text(text = label, fontSize = 9.sp, color = Color.Gray)
    }
}
@Composable
fun ProduceBatchCard(
    batch: ProduceBatchEntity,
    onQrClick: () -> Unit,
    onSpeakClick: (() -> Unit)? = null
) {
    val now = System.currentTimeMillis()
    val isPendingConfirmation = batch.payoutStatus == "PENDING" || batch.payoutStatus == "PROCESSING"
    val isOverduePending = batch.payoutStatus == "PENDING" && (now - batch.timestamp) >= (24 * 60 * 60 * 1000L)
    val hoursPending = if (isOverduePending) (now - batch.timestamp) / (1000 * 60 * 60) else 0L

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onQrClick,
                        modifier = Modifier.testTag("batch_qr_${batch.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode,
                            contentDescription = "View QR Slip",
                            tint = Color(0xFF0B3D2E),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    if (onSpeakClick != null) {
                        SpeakIconButton(
                            onClick = onSpeakClick,
                            contentDescription = "Speak weight and payout aloud",
                            tint = Color(0xFF0D47A1),
                            testTag = "batch_tts_speak_${batch.id}"
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                    } else {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Column {
                        Text(
                            text = batch.batchCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0B3D2E)
                        )
                        Text(
                            text = "${batch.farmerName} • ${batch.cropType}",
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "${batch.locationName} (${batch.cooperativeName})",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                        val dateStr = remember(batch.timestamp) {
                            java.text.SimpleDateFormat("MMM dd, yyyy • HH:mm", java.util.Locale.getDefault()).format(java.util.Date(batch.timestamp))
                        }
                        Text(
                            text = "Recorded: $dateStr",
                            fontSize = 10.sp,
                            color = Color(0xFF228B62),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "%.1f kg".format(batch.weightKg),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "LRD $%.0f".format(batch.totalPayoutLrd),
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF228B62)
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Surface(
                        color = when {
                            batch.payoutStatus == "PAID" -> Color(0xFFE8F5E9)
                            isOverduePending -> Color(0xFFFFEBEE)
                            else -> Color(0xFFFFF8E1)
                        },
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = when {
                                batch.payoutStatus == "PAID" -> "PAID (MoMo)"
                                isOverduePending -> "⚠️ OVERDUE (${hoursPending}h)"
                                else -> "PENDING"
                            },
                            color = when {
                                batch.payoutStatus == "PAID" -> Color(0xFF2E7D32)
                                isOverduePending -> Color(0xFFC62828)
                                else -> Color(0xFFF57F17)
                            },
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (isPendingConfirmation) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = if (isOverduePending) Color(0xFFFFF0F0) else Color(0xFFFFF3E0),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        0.5.dp,
                        if (isOverduePending) Color(0xFFEF5350) else Color(0xFFFFB74D)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("momo_payout_pending_confirmation_badge_${batch.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                color = if (isOverduePending) Color(0xFFD32F2F) else Color(0xFFE65100),
                                shape = CircleShape,
                                modifier = Modifier.size(18.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsActive,
                                        contentDescription = "Pending MoMo Confirmation Alert",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOverduePending)
                                    "🚨 ALERT: MoMo payout status confirmation overdue (${hoursPending}h pending)"
                                else
                                    "🔔 Mobile Money payout status confirmation pending (${batch.payoutStatus})",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOverduePending) Color(0xFFC62828) else Color(0xFFE65100)
                            )
                        }

                        Surface(
                            color = if (isOverduePending) Color(0xFFFFCDD2) else Color(0xFFFFE082),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "Awaiting MoMo Gate",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isOverduePending) Color(0xFFB71C1C) else Color(0xFFBF360C),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
