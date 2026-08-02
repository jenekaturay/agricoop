package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * Specification for tuber variety typical yield expectations and thresholds
 * to catch data entry typos (e.g. entering 5000kg instead of 500kg or 3500kg instead of 350kg).
 */
data class TuberVarietyYieldSpec(
    val cropCode: String,                   // "CASSAVA", "YAM", "SWEET_POTATO"
    val displayName: String,                // "Cassava (TME 419 / White)"
    val typicalMinBatchKg: Double,          // e.g. 50.0 kg
    val typicalMaxBatchKg: Double,          // e.g. 450.0 kg
    val warningThresholdKg: Double,         // e.g. 500.0 kg (above this HIGHLIGHTS IN VIBRANT RED)
    val criticalLimitKg: Double,            // e.g. 800.0 kg
    val expectedStarchMinPct: Double,       // e.g. 20.0%
    val expectedStarchMaxPct: Double,       // e.g. 32.0%
    val yieldNotes: String                  // Explanation for field staff
)

object TuberYieldThresholds {
    val CASSAVA = TuberVarietyYieldSpec(
        cropCode = "CASSAVA",
        displayName = "Cassava (TME 419 / High Starch)",
        typicalMinBatchKg = 50.0,
        typicalMaxBatchKg = 450.0,
        warningThresholdKg = 500.0,
        criticalLimitKg = 800.0,
        expectedStarchMinPct = 20.0,
        expectedStarchMaxPct = 32.0,
        yieldNotes = "Standard single farmer delivery sack limit is 500.0 kg. Batches > 500 kg trigger RED visual error check."
    )

    val YAM = TuberVarietyYieldSpec(
        cropCode = "YAM",
        displayName = "Yam (Yellow / White Guinea)",
        typicalMinBatchKg = 30.0,
        typicalMaxBatchKg = 300.0,
        warningThresholdKg = 350.0,
        criticalLimitKg = 600.0,
        expectedStarchMinPct = 14.0,
        expectedStarchMaxPct = 22.0,
        yieldNotes = "Single farm crate limit is 350.0 kg. Entries > 350 kg exceed typical tuber weight distribution."
    )

    val SWEET_POTATO = TuberVarietyYieldSpec(
        cropCode = "SWEET_POTATO",
        displayName = "Sweet Potato (Orange Fleshed / OFSP)",
        typicalMinBatchKg = 20.0,
        typicalMaxBatchKg = 220.0,
        warningThresholdKg = 250.0,
        criticalLimitKg = 500.0,
        expectedStarchMinPct = 12.0,
        expectedStarchMaxPct = 20.0,
        yieldNotes = "Bag delivery limit is 250.0 kg. Entries > 250 kg indicate possible keypad typo."
    )

    val DEFAULT = TuberVarietyYieldSpec(
        cropCode = "OTHER",
        displayName = "General Tuber Variety",
        typicalMinBatchKg = 25.0,
        typicalMaxBatchKg = 280.0,
        warningThresholdKg = 300.0,
        criticalLimitKg = 600.0,
        expectedStarchMinPct = 15.0,
        expectedStarchMaxPct = 25.0,
        yieldNotes = "Default tuber batch threshold set at 300.0 kg."
    )

    fun getSpecForCrop(cropCode: String): TuberVarietyYieldSpec {
        return when (cropCode.uppercase()) {
            "CASSAVA" -> CASSAVA
            "YAM" -> YAM
            "SWEET_POTATO", "POTATO" -> SWEET_POTATO
            else -> DEFAULT
        }
    }
}

/**
 * Visual Threshold Indicator Component.
 * Highlights in vibrant RED if a batch weight entry exceeds typical yield expectations for specific tuber varieties,
 * preventing accidental key-press data entry errors during field weighing.
 */
@Composable
fun TuberYieldThresholdIndicator(
    cropType: String,
    currentWeightKg: Double,
    onCapWeightToThreshold: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val spec = remember(cropType) { TuberYieldThresholds.getSpecForCrop(cropType) }

    val isExceedingThreshold = currentWeightKg > spec.warningThresholdKg
    val isCriticalExceeded = currentWeightKg > spec.criticalLimitKg
    val excessAmountKg = currentWeightKg - spec.warningThresholdKg

    var isOverrideAcknowledged by remember { mutableStateOf(false) }

    // Animated container background and border color
    val cardBgColor by animateColorAsState(
        targetValue = if (isExceedingThreshold) Color(0xFFFFEBEE) else Color(0xFFF1F8E9),
        animationSpec = tween(durationMillis = 300),
        label = "cardBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isExceedingThreshold) Color(0xFFD32F2F) else Color(0xFF388E3C),
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    val badgeBgColor by animateColorAsState(
        targetValue = if (isExceedingThreshold) Color(0xFFD32F2F) else Color(0xFF2E7D32),
        animationSpec = tween(durationMillis = 300),
        label = "badgeBgColor"
    )

    Surface(
        color = cardBgColor,
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("tuber_yield_threshold_indicator")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Tuber Variety Spec Title & Visual Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        color = if (isExceedingThreshold) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                        shape = CircleShape,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isExceedingThreshold) Icons.Default.Warning else Icons.Default.Agriculture,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = spec.displayName.uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isExceedingThreshold) Color(0xFFB71C1C) else Color(0xFF1B5E20),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Yield Expectation Limit: ${spec.warningThresholdKg.toInt()} kg",
                            fontSize = 10.sp,
                            color = if (isExceedingThreshold) Color(0xFFC62828) else Color(0xFF2E7D32)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Highlighted RED or GREEN Status Badge
                Surface(
                    color = badgeBgColor,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = if (isExceedingThreshold) 4.dp else 0.dp,
                    modifier = Modifier.testTag("yield_threshold_status_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isExceedingThreshold) Icons.Default.ReportProblem else Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isExceedingThreshold) "⚠️ RED ALERT: THRESHOLD EXCEEDED" else "✓ NORMAL YIELD RANGE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Threshold Linear Gauge Bar
            val fillRatio = (currentWeightKg / spec.criticalLimitKg).toFloat().coerceIn(0f, 1f)
            val thresholdRatio = (spec.warningThresholdKg / spec.criticalLimitKg).toFloat().coerceIn(0f, 1f)

            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Batch Weight Entry: ${String.format(Locale.US, "%.1f", currentWeightKg)} kg",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isExceedingThreshold) Color(0xFFB71C1C) else Color(0xFF1B5E20)
                    )

                    if (isExceedingThreshold) {
                        Text(
                            text = "+${String.format(Locale.US, "%.1f", excessAmountKg)} kg OVER THRESHOLD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFD32F2F)
                        )
                    } else {
                        Text(
                            text = "Max Normal: ${spec.typicalMaxBatchKg.toInt()} kg",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Custom Canvas Gauge Bar with Warning Threshold Divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp)
                        .background(Color.White, RoundedCornerShape(9.dp))
                        .border(1.dp, if (isExceedingThreshold) Color(0xFFEF9A9A) else Color(0xFFA5D6A7), RoundedCornerShape(9.dp))
                        .padding(2.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val barWidth = size.width
                        val barHeight = size.height

                        // Threshold Marker Line X coordinate
                        val thresholdX = barWidth * thresholdRatio

                        // Normal Zone Background Fill (Green)
                        drawRoundRect(
                            color = Color(0xFFC8E6C9),
                            topLeft = Offset(0f, 0f),
                            size = Size(thresholdX, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Red Warning Zone Fill Background
                        drawRoundRect(
                            color = Color(0xFFFFCDD2),
                            topLeft = Offset(thresholdX, 0f),
                            size = Size(barWidth - thresholdX, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Active Weight Bar Fill
                        val activeFillWidth = barWidth * fillRatio
                        val activeBrush = if (fillRatio > thresholdRatio) {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF4CAF50), Color(0xFFFF9800), Color(0xFFD32F2F))
                            )
                        } else {
                            Brush.horizontalGradient(
                                colors = listOf(Color(0xFF81C784), Color(0xFF388E3C))
                            )
                        }

                        drawRoundRect(
                            brush = activeBrush,
                            topLeft = Offset(0f, 0f),
                            size = Size(activeFillWidth, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )

                        // Threshold Marker Line (Dashed / Solid Line)
                        drawLine(
                            color = Color(0xFFB71C1C),
                            start = Offset(thresholdX, 0f),
                            end = Offset(thresholdX, barHeight),
                            strokeWidth = 3f
                        )
                    }
                }

                // Gauge Scale Tick Labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "0 kg", fontSize = 9.sp, color = Color.Gray)
                    Text(
                        text = "Threshold: ${spec.warningThresholdKg.toInt()} kg",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD32F2F)
                    )
                    Text(text = "${spec.criticalLimitKg.toInt()} kg", fontSize = 9.sp, color = Color.Gray)
                }
            }

            // Highlighting RED Warning Box if Threshold Exceeded
            AnimatedVisibility(
                visible = isExceedingThreshold,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Surface(
                        color = Color(0xFFB71C1C),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("red_threshold_warning_banner")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Data Entry Warning",
                                    tint = Color(0xFFFFD54F),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "DATA ENTRY EXCEEDANCE WARNING",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "Entry of ${String.format(Locale.US, "%.1f", currentWeightKg)} kg exceeds typical single-farmer harvest batch norm (${spec.warningThresholdKg.toInt()} kg) for ${spec.displayName}.",
                                fontSize = 11.sp,
                                color = Color(0xFFFFEBEE),
                                fontWeight = FontWeight.SemiBold
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = "• Verify scale readout to rule out extra '0' digit typos (e.g. 5000kg vs 500kg).\n• If this is a genuine heavy multi-bag consignment, tap Override.",
                                fontSize = 10.sp,
                                color = Color(0xFFFFCDD2)
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action Buttons: Cap Weight vs Override
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        onCapWeightToThreshold(spec.warningThresholdKg)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFFFFD54F),
                                        contentColor = Color.Black
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("cap_weight_to_threshold_button")
                                ) {
                                    Text(
                                        text = "Cap to ${spec.warningThresholdKg.toInt()} kg",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        isOverrideAcknowledged = !isOverrideAcknowledged
                                    },
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color.White
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("acknowledge_heavy_batch_button")
                                ) {
                                    Text(
                                        text = if (isOverrideAcknowledged) "✓ Override Active" else "Heavy Batch Override",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Guidance Note when within normal limits
            if (!isExceedingThreshold) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = spec.yieldNotes,
                        fontSize = 10.sp,
                        color = Color(0xFF33691E)
                    )
                }
            }
        }
    }
}
