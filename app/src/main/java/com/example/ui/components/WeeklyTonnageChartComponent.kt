package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.ProduceBatchEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyTonnageData(
    val dayLabel: String,         // e.g., "Mon", "Tue"
    val dateLabel: String,        // e.g., "Jul 26"
    val tonnageTons: Double,      // e.g., 6.4
    val isToday: Boolean,
    val timestamp: Long
)

data class CropTonnageData(
    val cropName: String,
    val tonnageTons: Double,
    val percentage: Float,
    val color: Color
)

@Composable
fun WeeklyTonnageChartComponent(
    batches: List<ProduceBatchEntity>,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = 7-Day Bar Chart, 1 = Crop Type Breakdown
    var animationTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationTrigger = true
    }

    // Process Room Database batches into weekly daily data (Last 7 days)
    val dailyDataList = remember(batches) {
        calculateWeeklyDailyTonnage(batches)
    }

    val totalWeeklyTonnageTons = remember(dailyDataList) {
        dailyDataList.sumOf { it.tonnageTons }
    }

    val dailyAverageTons = remember(dailyDataList) {
        if (dailyDataList.isNotEmpty()) totalWeeklyTonnageTons / dailyDataList.size else 0.0
    }

    val maxDailyTons = remember(dailyDataList) {
        dailyDataList.maxOfOrNull { it.tonnageTons }?.coerceAtLeast(1.0) ?: 10.0
    }

    val peakDay = remember(dailyDataList) {
        dailyDataList.maxByOrNull { it.tonnageTons }
    }

    // Crop Type Breakdown from Room batches
    val cropBreakdown = remember(batches) {
        calculateCropTypeBreakdown(batches)
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 1000),
        label = "chartAnimation"
    )

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("weekly_tonnage_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Title & Weekly Summary Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF0B3D2E),
                        shape = CircleShape,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Weekly Tuber Tonnage",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0B3D2E)
                        )
                        Text(
                            text = "Room SQLite Database Summary",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "%.1f Tons".format(totalWeeklyTonnageTons),
                            color = Color(0xFF2E7D32),
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Stat Summary Pill Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF4F7F5), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total 7-Day Volume", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "%.2f Tons".format(totalWeeklyTonnageTons),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0B3D2E)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color.LightGray)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Daily Average", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "%.1f Tons/day".format(dailyAverageTons),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF1B604A)
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(Color.LightGray)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Peak Intake Day", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        text = "${peakDay?.dayLabel ?: "N/A"} (%.1ft)".format(peakDay?.tonnageTons ?: 0.0),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFFE5A93C)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart View Mode Selector (Bar Chart vs Crop Type)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFEFEFEF), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Surface(
                    color = if (selectedTab == 0) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = if (selectedTab == 0) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 0 }
                        .testTag("tonnage_tab_bar_chart")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = if (selectedTab == 0) Color(0xFF0B3D2E) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "7-Day Daily Bar Chart",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) Color(0xFF0B3D2E) else Color.Gray
                        )
                    }
                }

                Surface(
                    color = if (selectedTab == 1) Color.White else Color.Transparent,
                    shape = RoundedCornerShape(8.dp),
                    shadowElevation = if (selectedTab == 1) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { selectedTab = 1 }
                        .testTag("tonnage_tab_crop_breakdown")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = if (selectedTab == 1) Color(0xFF0B3D2E) else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Crop Variety Split",
                            fontSize = 12.sp,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) Color(0xFF0B3D2E) else Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedTab == 0) {
                // Compose Canvas Bar Chart Component
                WeeklyBarChartCanvas(
                    dailyData = dailyDataList,
                    maxDailyTons = maxDailyTons,
                    animatedProgress = animatedProgress
                )
            } else {
                // Crop Variety Breakdown Component
                CropVarietyBreakdownList(
                    cropBreakdown = cropBreakdown,
                    animatedProgress = animatedProgress
                )
            }
        }
    }
}

@Composable
private fun WeeklyBarChartCanvas(
    dailyData: List<DailyTonnageData>,
    maxDailyTons: Double,
    animatedProgress: Float
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(Color(0xFFFAFAFA), RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFFEAEAEA), RoundedCornerShape(12.dp))
                .padding(12.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height
                val barCount = dailyData.size.coerceAtLeast(1)
                val spacing = width / barCount
                val barWidth = (spacing * 0.55f).coerceAtMost(36.dp.toPx())

                // Draw target capacity dashed line at 5.0 Tons
                val targetTons = 5.0
                val targetY = height - ((targetTons / maxDailyTons) * (height * 0.75f)).toFloat()
                drawLine(
                    color = Color(0xFFFFB300),
                    start = Offset(0f, targetY),
                    end = Offset(width, targetY),
                    strokeWidth = 2.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                )

                dailyData.forEachIndexed { index, data ->
                    val x = index * spacing + (spacing / 2)
                    val barHeight = ((data.tonnageTons / maxDailyTons) * (height * 0.75f) * animatedProgress).toFloat().coerceAtLeast(6.dp.toPx())
                    val topY = height - barHeight - 24.dp.toPx()

                    val isPeak = data.tonnageTons == maxDailyTons && maxDailyTons > 0

                    val barBrush = if (data.isToday) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFFFFD54F), Color(0xFFE5A93C))
                        )
                    } else if (isPeak) {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF228B62), Color(0xFF0B3D2E))
                        )
                    } else {
                        Brush.verticalGradient(
                            colors = listOf(Color(0xFF81C784), Color(0xFF2E7D32))
                        )
                    }

                    // Draw Bar
                    drawRoundRect(
                        brush = barBrush,
                        topLeft = Offset(x - (barWidth / 2), topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx())
                    )
                }
            }

            // Overlay Compose Labels & Value Badges over Canvas
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyData.forEach { data ->
                    val isPeak = data.tonnageTons == maxDailyTons && maxDailyTons > 0

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        // Tonnage Value Label on Top of Bar
                        Surface(
                            color = if (data.isToday) Color(0xFFFFF3E0) else if (isPeak) Color(0xFFE8F5E9) else Color.Transparent,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "%.1ft".format(data.tonnageTons),
                                fontSize = 10.sp,
                                fontWeight = if (isPeak || data.isToday) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (data.isToday) Color(0xFFE65100) else if (isPeak) Color(0xFF0B3D2E) else Color.Gray,
                                modifier = Modifier.padding(horizontal = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Day & Date Label at Bottom
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = data.dayLabel,
                                fontSize = 11.sp,
                                fontWeight = if (data.isToday) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (data.isToday) Color(0xFFE65100) else Color(0xFF0B3D2E)
                            )
                            Text(
                                text = data.dateLabel,
                                fontSize = 9.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(Color(0xFFFFB300), CircleShape)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Dashed Line: Daily Target Capacity (5.0 Tons)", fontSize = 10.sp, color = Color.Gray)
            }

            Text("Source: Room SQLite Ledger", fontSize = 10.sp, color = Color(0xFF0B3D2E), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun CropVarietyBreakdownList(
    cropBreakdown: List<CropTonnageData>,
    animatedProgress: Float
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (cropBreakdown.isEmpty()) {
            Text(
                text = "No batch records available in local database.",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(12.dp)
            )
        } else {
            cropBreakdown.forEach { item ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(item.color, CircleShape)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = item.cropName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF0B3D2E)
                            )
                        }

                        Text(
                            text = "%.2f Tons (%.1f%%)".format(item.tonnageTons, item.percentage * 100),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.DarkGray
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { (item.percentage * animatedProgress).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(CircleShape),
                        color = item.color,
                        trackColor = Color(0xFFEEEEEE)
                    )
                }
            }
        }
    }
}

/**
 * Calculates 7-day daily tonnage breakdown from Room ProduceBatchEntity list.
 */
private fun calculateWeeklyDailyTonnage(batches: List<ProduceBatchEntity>): List<DailyTonnageData> {
    val calendar = Calendar.getInstance()
    val dayFormat = SimpleDateFormat("EEE", Locale.US)
    val dateFormat = SimpleDateFormat("MMM dd", Locale.US)

    val result = mutableListOf<DailyTonnageData>()

    // Generate last 7 days including today (from day -6 to today)
    for (i in 6 downTo 0) {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)

        val year = cal.get(Calendar.YEAR)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val dayBatches = batches.filter { batch ->
            val bCal = Calendar.getInstance().apply { timeInMillis = batch.timestamp }
            bCal.get(Calendar.YEAR) == year && bCal.get(Calendar.DAY_OF_YEAR) == dayOfYear
        }

        val totalKg = dayBatches.sumOf { it.weightKg }
        val tons = totalKg / 1000.0

        val isToday = i == 0
        val dayName = dayFormat.format(cal.time)
        val dateName = dateFormat.format(cal.time)

        result.add(
            DailyTonnageData(
                dayLabel = dayName,
                dateLabel = dateName,
                tonnageTons = tons,
                isToday = isToday,
                timestamp = cal.timeInMillis
            )
        )
    }

    return result
}

/**
 * Calculates crop variety breakdown from Room ProduceBatchEntity list.
 */
private fun calculateCropTypeBreakdown(batches: List<ProduceBatchEntity>): List<CropTonnageData> {
    if (batches.isEmpty()) return emptyList()

    val cropColors = listOf(
        Color(0xFF0B3D2E), // Bitter Cassava (Industrial HQCF)
        Color(0xFF228B62), // Sweet Cassava (Food Grade)
        Color(0xFFE5A93C), // Yellow Root Cassava (Biofortified Provitamin A)
        Color(0xFF8D5B4C), // Water Yam / Cocoyam
        Color(0xFF1565C0)  // Other Crops
    )

    val totalKg = batches.sumOf { it.weightKg }.coerceAtLeast(1.0)
    val grouped = batches.groupBy { it.cropType }

    return grouped.entries.mapIndexed { index, entry ->
        val cropKg = entry.value.sumOf { it.weightKg }
        val tons = cropKg / 1000.0
        val pct = (cropKg / totalKg).toFloat()
        val color = cropColors.getOrElse(index) { Color(0xFF616161) }

        CropTonnageData(
            cropName = entry.key,
            tonnageTons = tons,
            percentage = pct,
            color = color
        )
    }.sortedByDescending { it.tonnageTons }
}
