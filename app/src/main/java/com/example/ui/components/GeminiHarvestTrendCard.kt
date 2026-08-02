package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BatchPrediction
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.service.HarvestPredictionResult

@Composable
fun GeminiHarvestTrendCard(
    isPredicting: Boolean,
    predictionResult: HarvestPredictionResult?,
    predictionError: String?,
    totalBatchesCount: Int,
    onRunPrediction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier
            .fillMaxWidth()
            .border(1.5.dp, Color(0xFF6A1B9A).copy(alpha = 0.3f), RoundedCornerShape(20.dp))
            .testTag("gemini_harvest_trend_card")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Banner
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF3E5F5),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Gemini AI",
                                tint = Color(0xFF7B1FA2),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Gemini AI ML Harvest Predictor",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF4A148C)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                color = Color(0xFFE1BEE7),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "3.5 FLASH",
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF4A148C),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Predicts 30-day tuber tonnage using $totalBatchesCount Room DB records",
                            fontSize = 11.sp,
                            color = Color(0xFF6A1B9A)
                        )
                    }
                }

                Button(
                    onClick = onRunPrediction,
                    enabled = !isPredicting,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF7B1FA2),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFE1BEE7)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("run_gemini_ml_prediction_button")
                ) {
                    if (isPredicting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Analyzing...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = if (predictionResult != null) Icons.Default.Refresh else Icons.Default.Psychology,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (predictionResult == null) "Run Forecast" else "Re-Run",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isPredicting) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF3E5F5), RoundedCornerShape(12.dp))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF7B1FA2), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "🤖 Querying Gemini 3.5 Flash ML Model...",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF4A148C)
                        )
                        Text(
                            text = "Processing Room SQLite historical batch weights, starch content & farmer yield trends",
                            fontSize = 11.sp,
                            color = Color(0xFF7B1FA2)
                        )
                    }
                }
            } else if (predictionResult != null) {
                // Forecast Results Dashboard
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Summary Metric Banner
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF3E5F5),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "30-DAY FORECASTED TONNAGE",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A),
                                    letterSpacing = 0.5.sp
                                )
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "%.1f Tons".format(predictionResult.totalProjectedVolumeKg / 1000.0),
                                        fontSize = 22.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF4A148C)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "(%.0f kg)".format(predictionResult.totalProjectedVolumeKg),
                                        fontSize = 12.sp,
                                        color = Color(0xFF7B1FA2)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Surface(
                                    color = when (predictionResult.trendDirection) {
                                        "INCREASING" -> Color(0xFFE8F5E9)
                                        "DECREASING" -> Color(0xFFFFEBEE)
                                        else -> Color(0xFFE3F2FD)
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = when (predictionResult.trendDirection) {
                                                "INCREASING" -> Icons.Default.TrendingUp
                                                "DECREASING" -> Icons.Default.TrendingDown
                                                else -> Icons.Default.ShowChart
                                            },
                                            contentDescription = null,
                                            tint = when (predictionResult.trendDirection) {
                                                "INCREASING" -> Color(0xFF2E7D32)
                                                "DECREASING" -> Color(0xFFC62828)
                                                else -> Color(0xFF1565C0)
                                            },
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = predictionResult.trendDirection,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = when (predictionResult.trendDirection) {
                                                "INCREASING" -> Color(0xFF2E7D32)
                                                "DECREASING" -> Color(0xFFC62828)
                                                else -> Color(0xFF1565C0)
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = "AI Confidence: ${predictionResult.confidenceScore}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6A1B9A)
                                )
                            }
                        }
                    }

                    // Crop Breakdown Section
                    Text(
                        text = "Projected Tuber Crop Yield Breakdown:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = Color(0xFF4A148C)
                    )

                    predictionResult.cropBreakdown.forEach { crop ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${crop.cropType} (${crop.starchYieldPotential})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                                Text(
                                    text = "%.0f kg (+%.1f%%)".format(crop.projectedWeightKg, crop.expectedGrowthPercentage),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            val ratio = if (predictionResult.totalProjectedVolumeKg > 0) {
                                (crop.projectedWeightKg / predictionResult.totalProjectedVolumeKg).toFloat()
                            } else 0.33f
                            LinearProgressIndicator(
                                progress = { ratio.coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(CircleShape),
                                color = when (crop.cropType) {
                                    "CASSAVA" -> Color(0xFF2E7D32)
                                    "YAM" -> Color(0xFFE5A93C)
                                    else -> Color(0xFF8D5B4C)
                                },
                                trackColor = Color(0xFFE0E0E0)
                            )
                        }
                    }

                    // Key Insights List
                    if (predictionResult.keyInsights.isNotEmpty()) {
                        OutlinedCard(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFAFAFA)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = Color(0xFFE5A93C),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Gemini ML Operational Insights:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF4A148C)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                predictionResult.keyInsights.forEach { insight ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "• ", fontSize = 10.sp, color = Color(0xFF7B1FA2), fontWeight = FontWeight.Bold)
                                        Text(text = insight, fontSize = 10.sp, color = Color.DarkGray, lineHeight = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Operational Recommendations List
                    if (predictionResult.operationalRecommendations.isNotEmpty()) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF2E7D32),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Recommended Coop Actions:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1B5E20)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                predictionResult.operationalRecommendations.forEach { rec ->
                                    Row(
                                        modifier = Modifier.padding(vertical = 2.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Text(text = "✓ ", fontSize = 10.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                                        Text(text = rec, fontSize = 10.sp, color = Color(0xFF1B5E20), lineHeight = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Model Badge footer
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (predictionResult.isGeminiPowered) "Powered by Gemini 3.5 Flash API" else "Powered by Local SQLite ML Regression Engine",
                            fontSize = 9.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                        val formattedDate = remember(predictionResult.timestamp) {
                            java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(predictionResult.timestamp))
                        }
                        Text(
                            text = "Last calculated: $formattedDate",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                // Initial prompt before user runs prediction
                OutlinedCard(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.outlinedCardColors(containerColor = Color(0xFFFAFAFA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatchPrediction,
                            contentDescription = "Run Prediction",
                            tint = Color(0xFF7B1FA2),
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Generate Tuber Harvest Volume Forecast",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF4A148C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Run Forecast' above to send Room database batch histories, starch ratios, and cooperative delivery speeds to Gemini AI for predictive yield analysis.",
                            fontSize = 11.sp,
                            color = Color.Gray,
                            lineHeight = 14.sp
                        )
                    }
                }
            }

            if (predictionError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFFFFEBEE),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Note: $predictionError",
                        fontSize = 10.sp,
                        color = Color(0xFFC62828),
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}
