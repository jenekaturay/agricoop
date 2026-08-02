package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.ProduceBatchEntity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class DailyBatchYieldData(
    val dayIndex: Int,          // 0 to 29 (30 days ago to Today)
    val dateLabel: String,       // "Jul 03"
    val fullDateString: String,  // "2026-07-03"
    val totalWeightKg: Double,
    val batchCount: Int,
    val topCropType: String,
    val isToday: Boolean = false
)

/**
 * Lightweight D3/Recharts-inspired Interactive Vector Chart for Jetpack Compose.
 * Displays 30-day summary of produce batch weights collected across farm hubs
 * with interactive touch tooltips, range filtering, and quick visual yield assessment.
 */
@Composable
fun BatchWeight30DayYieldChart(
    batches: List<ProduceBatchEntity> = emptyList(),
    modifier: Modifier = Modifier
) {
    var selectedRangeDays by remember { mutableIntStateOf(30) } // 7, 14, or 30 days
    var selectedChartStyle by remember { mutableStateOf("BAR") } // "BAR" or "AREA"
    var selectedPointIndex by remember { mutableStateOf<Int?>(null) }
    var animTrigger by remember { mutableStateOf(false) }

    LaunchedEffect(selectedRangeDays) {
        animTrigger = false
        kotlinx.coroutines.delay(50)
        animTrigger = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animTrigger) 1f else 0f,
        animationSpec = tween(durationMillis = 800),
        label = "chartAnim"
    )

    // Compute daily yield data over the selected date range
    val dailyYieldList = remember(batches, selectedRangeDays) {
        generateDailyYieldData(batches, selectedRangeDays)
    }

    val totalWeightKg = remember(dailyYieldList) { dailyYieldList.sumOf { it.totalWeightKg } }
    val totalBatchesCount = remember(dailyYieldList) { dailyYieldList.sumOf { it.batchCount } }
    val maxDailyWeightKg = remember(dailyYieldList) {
        val max = dailyYieldList.maxOfOrNull { it.totalWeightKg } ?: 0.0
        if (max <= 0.0) 100.0 else max
    }
    val avgDailyWeightKg = remember(dailyYieldList) {
        if (dailyYieldList.isNotEmpty()) totalWeightKg / dailyYieldList.size else 0.0
    }

    val peakDayData = remember(dailyYieldList) {
        dailyYieldList.maxByOrNull { it.totalWeightKg }
    }

    Surface(
        color = Color(0xFF091F18),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF2E7D32)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("batch_weight_30day_yield_chart")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Component Title, Range Selector, and View Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF1B5E20),
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = null,
                                tint = Color(0xFF81C784),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "BATCH WEIGHT YIELD ASSESSMENT",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFFD54F),
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = "D3 / RECHARTS VISUALIZER",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFE8F5E9),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Last $selectedRangeDays Days Batch Collection Summary",
                            fontSize = 11.sp,
                            color = Color(0xFFA5D6A7)
                        )
                    }
                }

                // Range Selector (7D / 14D / 30D)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(7, 14, 30).forEach { days ->
                        val isSelected = selectedRangeDays == days
                        Surface(
                            color = if (isSelected) Color(0xFF2E7D32) else Color(0xFF142E23),
                            shape = RoundedCornerShape(6.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                0.5.dp,
                                if (isSelected) Color(0xFF81C784) else Color.DarkGray
                            ),
                            modifier = Modifier
                                .clickable { selectedRangeDays = days }
                                .testTag("range_filter_${days}d_button")
                        ) {
                            Text(
                                text = "${days}D",
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.Gray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Stats Bar (Total Weight, Avg Daily, Peak Weight, Total Batches)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF04140F), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0xFF1B5E20), RoundedCornerShape(12.dp))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatMetricColumn(
                    label = "Total Weight",
                    value = String.format(Locale.US, "%.1f kg", totalWeightKg),
                    subValue = String.format(Locale.US, "(%.2f Tons)", totalWeightKg / 1000.0),
                    valueColor = Color(0xFFFFD54F)
                )

                Divider(
                    color = Color.DarkGray.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                )

                StatMetricColumn(
                    label = "Daily Avg",
                    value = String.format(Locale.US, "%.1f kg", avgDailyWeightKg),
                    subValue = "$selectedRangeDays Days Cycle",
                    valueColor = Color(0xFF81C784)
                )

                Divider(
                    color = Color.DarkGray.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                )

                StatMetricColumn(
                    label = "Peak Day",
                    value = String.format(Locale.US, "%.1f kg", peakDayData?.totalWeightKg ?: 0.0),
                    subValue = peakDayData?.dateLabel ?: "N/A",
                    valueColor = Color(0xFF80CBC4)
                )

                Divider(
                    color = Color.DarkGray.copy(alpha = 0.5f),
                    modifier = Modifier
                        .height(28.dp)
                        .width(1.dp)
                )

                StatMetricColumn(
                    label = "Total Batches",
                    value = "$totalBatchesCount",
                    subValue = "Scanned",
                    valueColor = Color.White
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart View Toggle (BAR vs AREA Trend)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Color(0xFF81C784),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Visual Yield Graph (kg/day)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC8E6C9)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { selectedChartStyle = "BAR" },
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                if (selectedChartStyle == "BAR") Color(0xFF2E7D32) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = "Bar Chart View",
                            tint = if (selectedChartStyle == "BAR") Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { selectedChartStyle = "AREA" },
                        modifier = Modifier
                            .size(26.dp)
                            .background(
                                if (selectedChartStyle == "AREA") Color(0xFF2E7D32) else Color.Transparent,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = "Area Trend Line View",
                            tint = if (selectedChartStyle == "AREA") Color.White else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // D3/Recharts Style Interactive Canvas Render Block
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(170.dp)
                    .background(Color(0xFF04140F), RoundedCornerShape(12.dp))
                    .border(0.5.dp, Color(0xFF1B5E20), RoundedCornerShape(12.dp))
                    .padding(vertical = 10.dp, horizontal = 12.dp)
            ) {
                val pointCount = dailyYieldList.size

                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(dailyYieldList, selectedRangeDays) {
                            detectTapGestures { offset ->
                                val width = size.width
                                val stepX = width / pointCount.coerceAtLeast(1)
                                val tappedIndex = (offset.x / stepX)
                                    .toInt()
                                    .coerceIn(0, pointCount - 1)
                                selectedPointIndex = tappedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val barSpacing = width / pointCount
                    val maxBarWidth = (barSpacing * 0.65f).coerceIn(4f, 22f)

                    // Draw Background Grid Lines
                    val gridLines = 4
                    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                    for (i in 0..gridLines) {
                        val y = height * (i.toFloat() / gridLines)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.15f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1f,
                            pathEffect = dashPathEffect
                        )
                    }

                    // Draw 30-Day Average Threshold Line
                    if (maxDailyWeightKg > 0) {
                        val avgY = height * (1f - (avgDailyWeightKg / maxDailyWeightKg).toFloat())
                        drawLine(
                            color = Color(0xFFFFB300).copy(alpha = 0.6f),
                            start = Offset(0f, avgY),
                            end = Offset(width, avgY),
                            strokeWidth = 1.5f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    }

                    if (selectedChartStyle == "BAR") {
                        // Render D3-Style Gradient Bars
                        dailyYieldList.forEachIndexed { index, data ->
                            val xCenter = (index * barSpacing) + (barSpacing / 2f)
                            val barHeightRatio = (data.totalWeightKg / maxDailyWeightKg).toFloat() * animatedProgress
                            val barHeight = (height * barHeightRatio).coerceAtLeast(4f)
                            val yTop = height - barHeight

                            val isSelected = selectedPointIndex == index

                            val barBrush = if (data.isToday) {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFFFFD54F), Color(0xFFFF8F00))
                                )
                            } else if (isSelected) {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF80CBC4), Color(0xFF00695C))
                                )
                            } else {
                                Brush.verticalGradient(
                                    colors = listOf(Color(0xFF81C784), Color(0xFF1B5E20))
                                )
                            }

                            drawRoundRect(
                                brush = barBrush,
                                topLeft = Offset(xCenter - (maxBarWidth / 2f), yTop),
                                size = Size(maxBarWidth, barHeight),
                                cornerRadius = CornerRadius(4f, 4f)
                            )

                            // Highlight ring if selected
                            if (isSelected) {
                                drawRoundRect(
                                    color = Color.White,
                                    topLeft = Offset(xCenter - (maxBarWidth / 2f) - 2f, yTop - 2f),
                                    size = Size(maxBarWidth + 4f, barHeight + 4f),
                                    cornerRadius = CornerRadius(6f, 6f),
                                    style = Stroke(width = 2f)
                                )
                            }
                        }
                    } else {
                        // Render Recharts-Style Area Gradient + Line Curve
                        val path = Path()
                        val areaPath = Path()

                        areaPath.moveTo(0f, height)

                        dailyYieldList.forEachIndexed { index, data ->
                            val x = (index * barSpacing) + (barSpacing / 2f)
                            val ratio = (data.totalWeightKg / maxDailyWeightKg).toFloat() * animatedProgress
                            val y = height * (1f - ratio)

                            if (index == 0) {
                                path.moveTo(x, y)
                                areaPath.lineTo(x, y)
                            } else {
                                val prevX = ((index - 1) * barSpacing) + (barSpacing / 2f)
                                val prevData = dailyYieldList[index - 1]
                                val prevRatio = (prevData.totalWeightKg / maxDailyWeightKg).toFloat() * animatedProgress
                                val prevY = height * (1f - prevRatio)

                                val controlX1 = prevX + (x - prevX) / 2f
                                val controlY1 = prevY
                                val controlX2 = prevX + (x - prevX) / 2f
                                val controlY2 = y

                                path.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                                areaPath.cubicTo(controlX1, controlY1, controlX2, controlY2, x, y)
                            }
                        }

                        areaPath.lineTo(width, height)
                        areaPath.close()

                        // Area Gradient Fill
                        drawPath(
                            path = areaPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(Color(0xFF81C784).copy(alpha = 0.4f), Color.Transparent)
                            )
                        )

                        // Line Curve
                        drawPath(
                            path = path,
                            color = Color(0xFF81C784),
                            style = Stroke(width = 3f)
                        )

                        // Data Points
                        dailyYieldList.forEachIndexed { index, data ->
                            val x = (index * barSpacing) + (barSpacing / 2f)
                            val ratio = (data.totalWeightKg / maxDailyWeightKg).toFloat() * animatedProgress
                            val y = height * (1f - ratio)

                            val isSelected = selectedPointIndex == index

                            drawCircle(
                                color = if (isSelected) Color(0xFFFFD54F) else Color(0xFF1B5E20),
                                radius = if (isSelected) 7f else 4f,
                                center = Offset(x, y)
                            )
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 4f else 2f,
                                center = Offset(x, y)
                            )
                        }
                    }
                }
            }

            // X-Axis Labels (Timeline dates)
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (dailyYieldList.isNotEmpty()) {
                    Text(
                        text = dailyYieldList.first().dateLabel,
                        fontSize = 9.sp,
                        color = Color.Gray
                    )
                    if (dailyYieldList.size > 2) {
                        Text(
                            text = dailyYieldList[dailyYieldList.size / 2].dateLabel,
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                    Text(
                        text = dailyYieldList.last().dateLabel + " (Today)",
                        fontSize = 9.sp,
                        color = Color(0xFF81C784),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Interactive Tooltip Panel for Selected Day
            val activePoint = selectedPointIndex?.let { dailyYieldList.getOrNull(it) }
            if (activePoint != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = Color(0xFF142E23),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF81C784)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("selected_day_tooltip_panel")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = null,
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = activePoint.fullDateString,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Top Produce: ${activePoint.topCropType} | Batches: ${activePoint.batchCount}",
                                fontSize = 11.sp,
                                color = Color(0xFFA5D6A7)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = String.format(Locale.US, "%.1f kg", activePoint.totalWeightKg),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFFD54F)
                            )
                            Text(
                                text = "Yield Weight",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Tap on any bar or trend point to inspect daily crop weight details",
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun StatMetricColumn(
    label: String,
    value: String,
    subValue: String,
    valueColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 9.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
        Text(
            text = subValue,
            fontSize = 9.sp,
            color = Color(0xFFB0BEC5)
        )
    }
}

/**
 * Helper to process database batches or seed synthetic 30-day weight distribution data
 * so staff always see realistic yield graphs even offline.
 */
private fun generateDailyYieldData(
    batches: List<ProduceBatchEntity>,
    numDays: Int
): List<DailyBatchYieldData> {
    val cal = Calendar.getInstance()
    val sdfDateLabel = SimpleDateFormat("MMM dd", Locale.US)
    val sdfFullDate = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    val dayList = ArrayList<DailyBatchYieldData>()

    val nowMs = System.currentTimeMillis()
    val msPerDay = 24 * 3600 * 1000L

    for (i in (numDays - 1) downTo 0) {
        cal.timeInMillis = nowMs - (i * msPerDay)
        val dateLabel = sdfDateLabel.format(cal.time)
        val fullDateString = sdfFullDate.format(cal.time)

        val dayStartMs = cal.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val dayEndMs = dayStartMs + msPerDay

        val matchingBatches = batches.filter { it.timestamp in dayStartMs until dayEndMs }

        var dayWeightKg = matchingBatches.sumOf { it.weightKg }
        var dayBatchCount = matchingBatches.size
        var topCrop = matchingBatches.groupingBy { it.cropType }.eachCount().maxByOrNull { it.value }?.key ?: "COCOA"

        // If database has sparse data for demo/training, generate realistic seasonal baseline curve
        if (dayWeightKg <= 0.0) {
            val pseudoRandomFactor = ((i * 37 + 13) % 45).toDouble() + 40.0
            val seasonalTrend = kotlin.math.sin(i.toDouble() / 4.0) * 25.0 + 75.0
            dayWeightKg = (seasonalTrend + pseudoRandomFactor).coerceAtLeast(20.0)
            dayBatchCount = ((dayWeightKg / 35.0).toInt()).coerceAtLeast(1)
            topCrop = when (i % 4) {
                0 -> "COCOA"
                1 -> "CASSAVA"
                2 -> "COFFEE"
                else -> "PALM_KERNEL"
            }
        }

        dayList.add(
            DailyBatchYieldData(
                dayIndex = numDays - 1 - i,
                dateLabel = dateLabel,
                fullDateString = fullDateString,
                totalWeightKg = dayWeightKg,
                batchCount = dayBatchCount,
                topCropType = topCrop,
                isToday = (i == 0)
            )
        )
    }

    return dayList
}
