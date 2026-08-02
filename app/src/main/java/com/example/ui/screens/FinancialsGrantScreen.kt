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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FinancialsGrantScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF6F9F7))
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Financials & Donor Grant Blueprint",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B3D2E)
        )

        // Setup Budget & Matching Grant Breakdown Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0B3D2E)),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("grant_capex_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccountBalance, contentDescription = null, tint = Color(0xFFFFD54F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "CapEx & Matching Grant Ratio",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Badge(containerColor = Color(0x33FFFFFF), contentColor = Color.White) {
                        Text("$85,000 USD Total", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CapExBudgetItem("Requested Grant (USADF/World Bank)", "$65,000 (76.5%)", Color(0xFFFFD54F))
                    CapExBudgetItem("Co-op In-Kind Contribution", "$20,000 (23.5%)", Color(0xFF81C784))
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color(0x33FFFFFF))
                Spacer(modifier = Modifier.height(10.dp))

                Text("CapEx Expense Allocations:", color = Color.LightGray, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                CapExRow("Solar Flash Dryers & Raspers", "$28,000")
                CapExRow("Off-Grid 15kW Solar PV & Boreholes", "$14,500")
                CapExRow("Logistics (2 Trucks + 4 Tuk-Tuk Trikes)", "$24,000")
                CapExRow("Farmer TME 419 Seed Nursery Kits", "$12,000")
                CapExRow("Offline Mobile & Spatial Backend Customization", "$6,500")
            }
        }

        // 3-Year Pro-Forma Income Statement
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("income_statement_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Assessment, contentDescription = null, tint = Color(0xFF0B3D2E))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "3-Year Income Statement (Pro-Forma)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF0B3D2E)
                        )
                    }

                    Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                        Text("Breakeven Month 14", modifier = Modifier.padding(4.dp))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                ProFormaHeaderRow()
                HorizontalDivider(color = Color.LightGray)
                ProFormaDataRow("Target Farmers", "1,200", "4,500", "12,000")
                ProFormaDataRow("HQCF & Food Flour", "$65,000", "$280,000", "$820,000")
                ProFormaDataRow("Industrial Starch", "$42,000", "$195,000", "$610,000")
                ProFormaDataRow("Gross Revenues", "$107,000", "$475,000", "$1,430,000", isBold = true)
                ProFormaDataRow("OpEx & Raw Tubers", "$88,500", "$285,000", "$766,000")
                HorizontalDivider(color = Color.Black)
                ProFormaDataRow("Net Operating Profit", "$18,500", "$190,000", "$664,000", isBold = true, highlightColor = Color(0xFF2E7D32))
            }
        }

        // Donor MEL Results Logframe Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Assignment, contentDescription = null, tint = Color(0xFF0B3D2E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Monitoring & Evaluation (MEL) Logframe",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0B3D2E)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                MelItemRow("Impact Target", "25% net household income increase for 1,200 smallholders by Month 18")
                MelItemRow("Outcome Target", "Post-harvest rot loss reduced from 45% down to < 5%")
                MelItemRow("Output 1", "50+ metric tons HQCF and Industrial Starch delivered to B2B off-takers in Year 1")
                MelItemRow("Output 2", "1,200 farmers trained in TME 419 disease-resistant agronomy with GPS logs")
            }
        }

        // Structural Risk & Capital Rollback Matrix
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("risk_matrix_card")
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF0B3D2E))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Risk Governance & 12-Mo Capital Rollback",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFF0B3D2E)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                RiskRow("Cassava Mosaic Disease (CMD)", "Distribute certified TME 419 resistant cuttings via 5 co-op nurseries")
                RiskRow("24-Hour Root Spoilage", "Enforce 15km Radius Rule for same-day flash drying near farms")
                RiskRow("Rainy Season Feeder Roads", "Deploy all-terrain motorized cargo trikes ('tuk-tuks') to highway trucks")
                RiskRow("Mobile Money Cash Float Shortage", "Corporate Bulk-Payout Agreement with Orange/MTN regional nodes")

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color(0xFFFFF8E1),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text("12-Month Pilot Failure Rollback Protocol:", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color(0xFFF57F17))
                        Text(
                            text = "1) Halt cloud node & SMS server contracts.\n2) Liquidate physical solar arrays & flash dryers (recover 40-50% CapEx).\n3) Pivot & license offline PostGIS logistics IP to B2B transport firms across West Africa.",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CapExBudgetItem(label: String, amount: String, color: Color) {
    Column {
        Text(text = label, fontSize = 11.sp, color = Color.LightGray)
        Text(text = amount, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
    }
}

@Composable
private fun CapExRow(item: String, cost: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = item, fontSize = 11.sp, color = Color.LightGray)
        Text(text = cost, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ProFormaHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = "FINANCIAL PROFILE", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(2f))
        Text(text = "YEAR 1", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(text = "YEAR 2", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
        Text(text = "YEAR 3", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ProFormaDataRow(
    metric: String,
    y1: String,
    y2: String,
    y3: String,
    isBold: Boolean = false,
    highlightColor: Color = Color.Black
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = metric,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = highlightColor,
            modifier = Modifier.weight(2f)
        )
        Text(
            text = y1,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = highlightColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = y2,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = highlightColor,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = y3,
            fontSize = 11.sp,
            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
            color = highlightColor,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MelItemRow(title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF228B62), modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0B3D2E))
            Text(text = desc, fontSize = 11.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun RiskRow(risk: String, fix: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = "• $risk", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color(0xFF0B3D2E))
        Text(text = "   Mitigation: $fix", fontSize = 11.sp, color = Color.DarkGray)
    }
}
