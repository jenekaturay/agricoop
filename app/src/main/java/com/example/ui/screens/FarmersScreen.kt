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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.ProduceBatchEntity
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.ReceiptLong
import com.example.ui.components.OfflineFarmMapView
import com.example.ui.components.QrCodeScannerModal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmersScreen(
    farmers: List<FarmerEntity>,
    cooperatives: List<CooperativeEntity>,
    batches: List<ProduceBatchEntity> = emptyList(),
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onOnboardFarmer: (
        coopId: String,
        fullName: String,
        phone: String,
        momo: String,
        gender: String,
        yearOfBirth: Int,
        cuttings: Int
    ) -> Unit
) {
    var countyFilter by remember { mutableStateOf("ALL") } // ALL, NIMBA, LOFA
    var showOnboardDialog by remember { mutableStateOf(false) }
    var showQrScannerModal by remember { mutableStateOf(false) }
    var isOfflineMapView by remember { mutableStateOf(false) }
    var viewingPayoutFarmerId by remember { mutableStateOf<String?>(null) }

    val filteredFarmers = farmers.filter { farmer ->
        val matchesSearch = farmer.fullName.contains(searchQuery, ignoreCase = true) ||
                farmer.nationalId.contains(searchQuery, ignoreCase = true) ||
                farmer.cooperativeName.contains(searchQuery, ignoreCase = true)

        val matchesCounty = when (countyFilter) {
            "NIMBA" -> farmer.nationalId.contains("NIM", ignoreCase = true) || farmer.cooperativeName.contains("Ganta", ignoreCase = true) || farmer.cooperativeName.contains("Sanniquellie", ignoreCase = true)
            "LOFA" -> farmer.nationalId.contains("LOF", ignoreCase = true) || farmer.cooperativeName.contains("Voinjama", ignoreCase = true) || farmer.cooperativeName.contains("Zorzor", ignoreCase = true) || farmer.cooperativeName.contains("Foya", ignoreCase = true)
            else -> true
        }
        matchesSearch && matchesCounty
    }

    if (viewingPayoutFarmerId != null) {
        FarmerPayoutHistoryScreen(
            farmers = farmers,
            batches = batches,
            initialMembershipId = viewingPayoutFarmerId ?: "",
            onBack = { viewingPayoutFarmerId = null }
        )
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF6F9F7))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Partner Cooperatives & Farmers",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B3D2E)
                        )
                        Text(
                            text = "1,200 Smallholders • Lofa & Nimba Corridors",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF0B3D2E),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clickable { viewingPayoutFarmerId = farmers.firstOrNull()?.id ?: "" }
                                .testTag("open_payout_lookup_header_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ReceiptLong,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Payouts",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            color = if (isOfflineMapView) Color(0xFFE8F5E9) else Color(0xFF0B3D2E),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .clickable { isOfflineMapView = !isOfflineMapView }
                                .testTag("toggle_offline_map_view")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (isOfflineMapView) Icons.Default.List else Icons.Default.Map,
                                    contentDescription = null,
                                    tint = if (isOfflineMapView) Color(0xFF0B3D2E) else Color(0xFFFFD54F),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOfflineMapView) "List" else "Map",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineMapView) Color(0xFF0B3D2E) else Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = { showOnboardDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                            modifier = Modifier.testTag("onboard_farmer_fab")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(2.dp))
                            Text("Onboard", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Offline Map View Component
            if (isOfflineMapView) {
                item {
                    OfflineFarmMapView(
                        farmers = farmers,
                        cooperatives = cooperatives
                    )
                }
            }

            // Search & County Filters Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onSearchQueryChange,
                        placeholder = { Text("Search farmer, ID, Co-op...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onSearchQueryChange("") }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("farmer_search_input")
                    )

                    Button(
                        onClick = { showQrScannerModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("farmers_screen_scan_qr_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = "Scan QR",
                            tint = Color(0xFFFFD54F)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan QR", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = countyFilter == "ALL",
                        onClick = { countyFilter = "ALL" },
                        label = { Text("All Corridors (${farmers.size})") },
                        modifier = Modifier.testTag("filter_county_all")
                    )
                    FilterChip(
                        selected = countyFilter == "NIMBA",
                        onClick = { countyFilter = "NIMBA" },
                        label = { Text("Nimba (Ganta Hub)") },
                        modifier = Modifier.testTag("filter_county_nimba")
                    )
                    FilterChip(
                        selected = countyFilter == "LOFA",
                        onClick = { countyFilter = "LOFA" },
                        label = { Text("Lofa Corridor") },
                        modifier = Modifier.testTag("filter_county_lofa")
                    )
                }
            }

            // Cooperatives Summary Cards Carousel
            item {
                Text(
                    text = "Registered Farmer Based Organizations (FBOs)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B3D2E)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    cooperatives.forEach { coop ->
                        CooperativeCard(coop = coop)
                    }
                }
            }

            // Smallholders Header
            item {
                Text(
                    text = "Smallholder Directory (${filteredFarmers.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B3D2E)
                )
            }

            // Farmers List
            items(filteredFarmers) { farmer ->
                FarmerCard(
                    farmer = farmer,
                    onSelectPayoutHistory = { viewingPayoutFarmerId = farmer.id }
                )
            }
        }

        // Onboard Farmer Dialog
        if (showOnboardDialog) {
            OnboardFarmerDialog(
                cooperatives = cooperatives,
                onDismiss = { showOnboardDialog = false },
                onSubmit = { coopId, name, phone, momo, gender, yob, cuttings ->
                    onOnboardFarmer(coopId, name, phone, momo, gender, yob, cuttings)
                    showOnboardDialog = false
                }
            )
        }

        // QR Code Scanner Modal
        if (showQrScannerModal) {
            QrCodeScannerModal(
                farmers = farmers,
                onDismiss = { showQrScannerModal = false },
                onFarmerScanned = { scannedFarmer ->
                    onSearchQueryChange(scannedFarmer.fullName)
                    showQrScannerModal = false
                }
            )
        }
    }
}

@Composable
private fun CooperativeCard(coop: CooperativeEntity) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = CircleShape,
                    modifier = Modifier.size(38.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Group, contentDescription = null, tint = Color(0xFF0B3D2E))
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = coop.name, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0B3D2E))
                    Text(text = "${coop.district}, ${coop.county} • Lead: ${coop.leadPerson}", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Badge(containerColor = Color(0xFFF0F4F2), contentColor = Color(0xFF0B3D2E)) {
                Text("${coop.memberCount} Members", modifier = Modifier.padding(4.dp))
            }
        }
    }
}

@Composable
private fun FarmerCard(
    farmer: FarmerEntity,
    onSelectPayoutHistory: (String) -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (farmer.gender == "FEMALE") Color(0xFFFCE4EC) else Color(0xFFE3F2FD),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = if (farmer.gender == "FEMALE") Color(0xFFC2185B) else Color(0xFF1976D2)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = farmer.fullName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0B3D2E))
                        Text(text = "ID: ${farmer.id} • ${farmer.cooperativeName}", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                if (farmer.isYouth) {
                    Surface(
                        color = Color(0xFFFFF3E0),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "Youth (18-35)",
                            color = Color(0xFFE65100),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "MoMo: ${farmer.momoNumber}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { onSelectPayoutHistory(farmer.id) }
                        .testTag("view_farmer_payout_btn_${farmer.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = Color(0xFF0B3D2E), modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Payout History", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OnboardFarmerDialog(
    cooperatives: List<CooperativeEntity>,
    onDismiss: () -> Unit,
    onSubmit: (
        coopId: String,
        fullName: String,
        phone: String,
        momo: String,
        gender: String,
        yearOfBirth: Int,
        cuttings: Int
    ) -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("+231") }
    var momo by remember { mutableStateOf("+231") }
    var gender by remember { mutableStateOf("FEMALE") }
    var yearOfBirthText by remember { mutableStateOf("1998") }
    var cuttingsText by remember { mutableStateOf("500") }

    var selectedCoop by remember { mutableStateOf(cooperatives.firstOrNull()) }
    var expandedCoopDropdown by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Onboard Smallholder Farmer",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B3D2E)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboard_name_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboard_phone_input")
                    )

                    OutlinedTextField(
                        value = momo,
                        onValueChange = { momo = it },
                        label = { Text("MoMo Wallet") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboard_momo_input")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Cooperative Selector Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedCoopDropdown,
                    onExpandedChange = { expandedCoopDropdown = !expandedCoopDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedCoop?.name ?: "Select Cooperative",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCoopDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                            .testTag("onboard_coop_dropdown")
                    )

                    ExposedDropdownMenu(
                        expanded = expandedCoopDropdown,
                        onDismissRequest = { expandedCoopDropdown = false }
                    ) {
                        cooperatives.forEach { coop ->
                            DropdownMenuItem(
                                text = { Text("${coop.name} (${coop.county})") },
                                onClick = {
                                    selectedCoop = coop
                                    expandedCoopDropdown = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Gender:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = gender == "FEMALE", onClick = { gender = "FEMALE" })
                        Text("Female", fontSize = 11.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = gender == "MALE", onClick = { gender = "MALE" })
                        Text("Male", fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = yearOfBirthText,
                        onValueChange = { yearOfBirthText = it },
                        label = { Text("Birth Year (Youth check)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboard_yob_input")
                    )

                    OutlinedTextField(
                        value = cuttingsText,
                        onValueChange = { cuttingsText = it },
                        label = { Text("TME 419 Seed Kit") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("onboard_cuttings_input")
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Cancel")
                    }
                    Button(
                        onClick = {
                            val coopId = selectedCoop?.id ?: "coop-ganta-01"
                            val yob = yearOfBirthText.toIntOrNull() ?: 1998
                            val cuttings = cuttingsText.toIntOrNull() ?: 500
                            onSubmit(coopId, fullName, phone, momo, gender, yob, cuttings)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        modifier = Modifier.testTag("submit_onboard_farmer_button")
                    ) {
                        Text("Save & Issue Seed Kit")
                    }
                }
            }
        }
    }
}
