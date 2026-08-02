package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ElectricCar
import androidx.compose.material.icons.filled.Factory
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.example.data.db.entities.HubOperationEntity

@Composable
fun ProcessingHubScreen(
    hubs: List<HubOperationEntity>
) {
    var rawInputTonsText by remember { mutableStateOf("10.0") }
    val rawTons = rawInputTonsText.toDoubleOrNull() ?: 10.0

    // Starch & Flour Conversion Yield Formulas
    // 1000 kg raw cassava yields ~250 kg HQCF (25%) or ~180 kg Industrial Starch (18%) + 200 kg animal feed peel pellets (20%)
    val calculatedHqcf = rawTons * 0.25
    val calculatedIndustrialStarch = rawTons * 0.18
    val calculatedFeedPellets = rawTons * 0.20

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9F7))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Solar Processing Hubs & Quality Control",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B3D2E)
        )

        // Interactive Yield Conversion Calculator Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3D2E)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("yield_calculator_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Science, contentDescription = null, tint = Color(0xFFFFD54F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Multi-Stream Yield Calculator",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                    Badge(containerColor = Color(0x33FFFFFF), contentColor = Color.White) {
                        Text("Import Substitution Engine", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = rawInputTonsText,
                    onValueChange = { rawInputTonsText = it },
                    label = { Text("Raw Tuber Input (Metric Tons)", color = Color.LightGray) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("raw_input_tons")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Stream Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    StreamResultBox("Consumer HQCF Flour", "%.2f Tons".format(calculatedHqcf), "Bakery Wheat Substitute", Color(0xFF228B62))
                    StreamResultBox("Industrial Starch", "%.2f Tons".format(calculatedIndustrialStarch), "Brewery & Adhesives", Color(0xFFE5A93C))
                    StreamResultBox("Animal Feed Pellets", "%.2f Tons".format(calculatedFeedPellets), "Pigs & Poultry Circular", Color(0xFF8D5B4C))
                }
            }
        }

        // Industrial Quality Assurance Protocol Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("qc_protocol_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.FilterAlt, contentDescription = null, tint = Color(0xFF0B3D2E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Industrial Starch QC Protocol",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0B3D2E)
                        )
                    }
                    Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                        Text("100-Mesh Certified", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                QcItemRow("Stainless Vibratory Sifter", "Passed 100-Mesh screen size (No grit/sand)", true)
                QcItemRow("Solar Flash Dryer Target", "Moisture <10.0% (Currently 8.8%)", true)
                QcItemRow("Specific Gravity Hydrometer", "Starch content >= 24% threshold", true)
                QcItemRow("Monrovia Bakery & Brewery QC", "Viscosity certificate generated for dispatch", true)
            }
        }

        // Hub Operations Details
        Text(
            text = "Active Off-Grid Processing Hubs",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B3D2E)
        )

        hubs.forEach { hub ->
            HubDetailCard(hub = hub)
        }

        // 15km Feeder Road Logistics Card
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
                        Icon(Icons.Default.LocalShipping, contentDescription = null, tint = Color(0xFF0B3D2E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "15km Radius Feeder Logistics",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0B3D2E)
                        )
                    }
                    Badge(containerColor = Color(0xFFFFF3E0), contentColor = Color(0xFFE65100)) {
                        Text("Rainy Season Spoke Active", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "To prevent 48-hour tuber rot during heavy rains, 4x4 trucks remain on paved highways while high-clearance motorized cargo trikes ('tuk-tuks') navigate mud feeder tracks.",
                    fontSize = 12.sp,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = Color(0xFFF0F4F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Motorized Cargo Trikes:", fontSize = 11.sp, color = Color.Gray)
                            Text("7 Active Units", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0B3D2E))
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Surface(
                        color = Color(0xFFF0F4F2),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Heavy 4x4 Corridor Trucks:", fontSize = 11.sp, color = Color.Gray)
                            Text("2 Highway Fleet", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF0B3D2E))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamResultBox(title: String, tons: String, subtitle: String, color: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color(0xFF07291F), RoundedCornerShape(10.dp))
            .padding(10.dp)
    ) {
        Text(text = title, fontSize = 10.sp, color = Color.LightGray)
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = tons, fontWeight = FontWeight.Black, fontSize = 14.sp, color = color)
        Text(text = subtitle, fontSize = 8.sp, color = Color.Gray)
    }
}

@Composable
private fun QcItemRow(title: String, desc: String, passed: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = if (passed) Color(0xFF2E7D32) else Color.Red,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0B3D2E))
            Text(text = desc, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun HubDetailCard(hub: HubOperationEntity) {
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
                    Icon(Icons.Default.Factory, contentDescription = null, tint = Color(0xFF0B3D2E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = hub.hubName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF0B3D2E))
                        Text(text = "${hub.county} County Corridor", fontSize = 11.sp, color = Color.Gray)
                    }
                }

                Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                    Text("Solar PV: ${hub.solarCapacityKw} kW", modifier = Modifier.padding(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "Daily Capacity: ${hub.dailyRawTons} Tons", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(text = "Flash Dryer Temp: 85°C", fontSize = 11.sp, color = Color(0xFF228B62), fontWeight = FontWeight.Bold)
            }
        }
    }
}
