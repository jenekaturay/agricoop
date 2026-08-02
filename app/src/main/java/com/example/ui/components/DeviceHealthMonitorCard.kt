package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.BatteryStd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HardDrive
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SolarPower
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.DeviceHealthInfo
import com.example.service.DeviceHealthObserver
import com.example.service.TextToSpeechHelper
import java.util.Locale

@Composable
fun DeviceHealthMonitorCard(
    simulatedBatteryLevel: Int? = null,
    onSimulateBatteryChange: ((Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    val healthInfo = remember(simulatedBatteryLevel) {
        DeviceHealthObserver.getDeviceHealthInfo(context, simulatedBatteryLevel)
    }

    val isBatteryLow = healthInfo.batteryPercentage < 20
    val isStorageLow = healthInfo.isLowStorage

    // Container Colors
    val cardBgColor by animateColorAsState(
        targetValue = if (healthInfo.isCriticalState) Color(0xFFFFF3F0) else Color(0xFFF4F9F5),
        animationSpec = tween(durationMillis = 300),
        label = "cardBgColor"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            healthInfo.isCriticalState -> Color(0xFFD32F2F)
            isBatteryLow || isStorageLow -> Color(0xFFF57C00)
            else -> Color(0xFF2E7D32)
        },
        animationSpec = tween(durationMillis = 300),
        label = "borderColor"
    )

    Surface(
        color = cardBgColor,
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("device_health_monitor_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header: Title & Voice Speaker Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = when {
                            healthInfo.isCriticalState -> Color(0xFFD32F2F)
                            isBatteryLow || isStorageLow -> Color(0xFFF57C00)
                            else -> Color(0xFF2E7D32)
                        },
                        shape = CircleShape,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (healthInfo.isCriticalState) Icons.Default.Warning else Icons.Default.Memory,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Text(
                            text = "FIELD DEVICE HEALTH",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0B3D2E),
                            letterSpacing = 0.5.sp
                        )
                        Text(
                            text = "Off-Grid Battery & Storage Monitor",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }
                }

                SpeakIconButton(
                    onClick = {
                        val speechText = "Field Device Health Status. Battery level is ${healthInfo.batteryPercentage} percent. ${if (healthInfo.isCharging) "Solar charger connected." else ""} Storage availability is ${String.format(Locale.US, "%.1f", healthInfo.availableStorageGb)} gigabytes free out of ${String.format(Locale.US, "%.1f", healthInfo.totalStorageGb)} gigabytes total. ${healthInfo.fieldReadinessMessage}"
                        ttsHelper.speakText(speechText)
                    },
                    contentDescription = "Speak Device Health",
                    tint = Color(0xFF2E7D32),
                    testTag = "speak_device_health_button"
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 1. BATTERY HEALTH GAUGE SECTION
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when {
                                    healthInfo.isCharging -> Icons.Default.BatteryChargingFull
                                    isBatteryLow -> Icons.Default.BatteryAlert
                                    else -> Icons.Default.BatteryStd
                                },
                                contentDescription = null,
                                tint = if (isBatteryLow) Color(0xFFD32F2F) else Color(0xFF2E7D32),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Battery Power:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF222222)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (healthInfo.isCharging) {
                                Surface(
                                    color = Color(0xFFFFF8E1),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.padding(end = 6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.SolarPower,
                                            contentDescription = null,
                                            tint = Color(0xFFF57F17),
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "SOLAR CHARGING",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFF57F17)
                                        )
                                    }
                                }
                            }

                            Text(
                                text = "${healthInfo.batteryPercentage}%",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isBatteryLow) Color(0xFFD32F2F) else Color(0xFF2E7D32)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Battery Fill Canvas Bar
                    val battFillRatio = (healthInfo.batteryPercentage / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(7.dp))
                            .border(1.dp, if (isBatteryLow) Color(0xFFEF9A9A) else Color(0xFFA5D6A7), RoundedCornerShape(7.dp))
                            .padding(2.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = size.width * battFillRatio
                            val barHeight = size.height

                            val battBrush = if (healthInfo.batteryPercentage < 20) {
                                Brush.horizontalGradient(listOf(Color(0xFFEF5350), Color(0xFFD32F2F)))
                            } else if (healthInfo.batteryPercentage < 50) {
                                Brush.horizontalGradient(listOf(Color(0xFFFFCA28), Color(0xFFF57C00)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF81C784), Color(0xFF2E7D32)))
                            }

                            drawRoundRect(
                                brush = battBrush,
                                topLeft = Offset(0f, 0f),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(5f, 5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        val estHours = String.format(Locale.US, "%.1f", (healthInfo.batteryPercentage * 0.12))
                        Text(
                            text = "Est. Field Runtime: ~$estHours hrs",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = if (isBatteryLow) "⚠️ Solar Panel Required" else "✓ Normal Operating Power",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isBatteryLow) Color(0xFFD32F2F) else Color(0xFF388E3C)
                        )
                    }

                    // Interactive Battery Simulator Switch buttons
                    if (onSimulateBatteryChange != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSimulateBatteryChange(85) },
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .testTag("simulate_battery_full_button")
                            ) {
                                Text("Test 85% Full", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { onSimulateBatteryChange(12) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD32F2F)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEF9A9A)),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(30.dp)
                                    .testTag("simulate_battery_low_button")
                            ) {
                                Text("Test 12% Low", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. STORAGE AVAILABILITY GAUGE SECTION
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SdCard,
                                contentDescription = null,
                                tint = if (isStorageLow) Color(0xFFD32F2F) else Color(0xFF1565C0),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Storage Availability:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF222222)
                            )
                        }

                        Text(
                            text = "${String.format(Locale.US, "%.2f", healthInfo.availableStorageGb)} GB Free",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isStorageLow) Color(0xFFD32F2F) else Color(0xFF1565C0)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Storage Gauge Canvas Bar
                    val storageUsedRatio = (healthInfo.storageUsedPercentage / 100f).coerceIn(0f, 1f)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .background(Color(0xFFEEEEEE), RoundedCornerShape(7.dp))
                            .border(1.dp, if (isStorageLow) Color(0xFFEF9A9A) else Color(0xFF90CAF9), RoundedCornerShape(7.dp))
                            .padding(2.dp)
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val barWidth = size.width * storageUsedRatio
                            val barHeight = size.height

                            val storageBrush = if (healthInfo.storageUsedPercentage > 90) {
                                Brush.horizontalGradient(listOf(Color(0xFFEF5350), Color(0xFFD32F2F)))
                            } else {
                                Brush.horizontalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1565C0)))
                            }

                            drawRoundRect(
                                brush = storageBrush,
                                topLeft = Offset(0f, 0f),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(5f, 5f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Used: ${healthInfo.storageUsedPercentage}% (${String.format(Locale.US, "%.1f", healthInfo.totalStorageGb)} GB Total)",
                            fontSize = 10.sp,
                            color = Color.Gray
                        )

                        Text(
                            text = "SQLite DB: ~${healthInfo.databaseSizeKb} KB",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0B3D2E)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. READINESS ASSESSMENT BANNER
            Surface(
                color = when {
                    healthInfo.isCriticalState -> Color(0xFFD32F2F)
                    isBatteryLow || isStorageLow -> Color(0xFFF57C00)
                    else -> Color(0xFF1B5E20)
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("off_grid_readiness_banner")
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (healthInfo.isCriticalState) Icons.Default.ReportProblem else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = healthInfo.fieldReadinessMessage,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        lineHeight = 14.sp
                    )
                }
            }
        }
    }
}
