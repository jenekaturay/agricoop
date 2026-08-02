package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contactless
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SignalCellularConnectedNoInternet0Bar
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.service.TextToSpeechHelper
import kotlinx.coroutines.launch

data class QuickStartStep(
    val stepNumber: Int,
    val title: String,
    val subtitle: String,
    val description: String,
    val icon: ImageVector,
    val primaryColor: Color,
    val accentColor: Color,
    val simulationActionLabel: String,
    val simulationSuccessMsg: String,
    val tips: List<String>
)

/**
 * Offline-first interactive 'Quick Start' Carousel Dialog that trains new co-op staff
 * on NFC scanning, produce batch registration, MoMo payouts, and offline queuing.
 * Operates 100% offline without requiring cellular or internet connection.
 */
@Composable
fun QuickStartCarouselDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val steps = remember {
        listOf(
            QuickStartStep(
                stepNumber = 1,
                title = "NFC Farmer ID Scan",
                subtitle = "Step 1 of 4: Contactless Verification",
                description = "Hold the farmer's RFID/NFC smart card against the top rear sensor of the handheld terminal. The system retrieves their biometric profile and county registration ID instantly offline.",
                icon = Icons.Default.Nfc,
                primaryColor = Color(0xFF1B5E20),
                accentColor = Color(0xFF81C784),
                simulationActionLabel = "Tap to Simulate NFC Scan",
                simulationSuccessMsg = "NFC Card Verified! Farmer: Moses Tarkpah (ID: LBR-BF-8821)",
                tips = listOf(
                    "Keep card within 2 cm of terminal sensor",
                    "Works 100% offline without internet connection",
                    "Backup barcode/QR scan available if card is damaged"
                )
            ),
            QuickStartStep(
                stepNumber = 2,
                title = "Batch Weighing & Moisture",
                subtitle = "Step 2 of 4: Produce Scale Capture",
                description = "Place produce sacks (Cocoa, Coffee, or Palm Kernel) onto the Bluetooth scale. The terminal auto-reads gross weight, subtracts tare weight, and logs moisture content.",
                icon = Icons.Default.Scale,
                primaryColor = Color(0xFF3E2723),
                accentColor = Color(0xFFFFB74D),
                simulationActionLabel = "Tap to Capture Scale Sacks",
                simulationSuccessMsg = "Batch Captured! Gross: 68.2 kg | Moisture: 7.1% | Grade A",
                tips = listOf(
                    "Ensure scale zero calibration before first bag",
                    "Select produce grade (A/B/C) based on moisture reader",
                    "Weight value automatically locks upon stabilization"
                )
            ),
            QuickStartStep(
                stepNumber = 3,
                title = "Mobile Money Payout",
                subtitle = "Step 3 of 4: Instant MoMo Voucher",
                description = "Generates a cryptographic Mobile Money payment voucher (MTN / Orange MoMo). The transaction digest is signed with hardware Anti-Replay nonce.",
                icon = Icons.Default.Payments,
                primaryColor = Color(0xFF004D40),
                accentColor = Color(0xFF80CBC4),
                simulationActionLabel = "Tap to Generate MoMo Token",
                simulationSuccessMsg = "MoMo Token Issued! Payout: $148.50 USD (Digest: 0x8F9A1B)",
                tips = listOf(
                    "Prints offline thermal receipt or sends USSD code",
                    "Prevents double-payouts using idempotent nonces",
                    "Farmer receives SMS confirmation when signal restores"
                )
            ),
            QuickStartStep(
                stepNumber = 4,
                title = "Offline Room DB Queue",
                subtitle = "Step 4 of 4: Secure Data Storage",
                description = "All recorded weighings and payout vouchers are encrypted using 256-bit AES SQLCipher and queued in the local Room database until network signal is restored.",
                icon = Icons.Default.Storage,
                primaryColor = Color(0xFF0D47A1),
                accentColor = Color(0xFF90CAF9),
                simulationActionLabel = "Tap to Verify Local Queue",
                simulationSuccessMsg = "Verified! 1 Batch queued locally in encrypted Room SQLite DB.",
                tips = listOf(
                    "No data is lost even if battery drains completely",
                    "Auto-syncs via low-bandwidth compression on 2G signal",
                    "Status bar shows yellow Offline Mode indicator when disconnected"
                )
            )
        )
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { steps.size })
    var simulatedStepState by remember { mutableStateOf<Map<Int, Boolean>>(emptyMap()) }
    var isTtsSpeaking by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121814)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, Color(0xFF2E7D32)),
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
                    .testTag("quick_start_carousel_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = Color(0xFF2E7D32),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.School,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Co-op Quick Start",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = "100% Offline Interactive Staff Training",
                                    fontSize = 11.sp,
                                    color = Color(0xFFA5D6A7)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // TTS Audio Button
                            IconButton(
                                onClick = {
                                    val currentStep = steps[pagerState.currentPage]
                                    val textToRead = "${currentStep.title}. ${currentStep.description}"
                                    isTtsSpeaking = true
                                    TextToSpeechHelper.speak(context, textToRead)
                                },
                                modifier = Modifier.testTag("tts_audio_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VolumeUp,
                                    contentDescription = "Read Aloud Instructions",
                                    tint = Color(0xFF81C784)
                                )
                            }

                            // Close Button
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.testTag("close_carousel_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = Color.Gray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Progress Indicators / Step Dots
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        steps.forEachIndexed { index, step ->
                            val isSelected = pagerState.currentPage == index
                            val isCompleted = simulatedStepState[index] == true

                            val dotColor by animateColorAsState(
                                targetValue = when {
                                    isCompleted -> Color(0xFF00E676)
                                    isSelected -> step.accentColor
                                    else -> Color.DarkGray
                                },
                                animationSpec = tween(durationMillis = 300)
                            )

                            Surface(
                                color = dotColor,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(6.dp)
                                    .padding(horizontal = 3.dp)
                                    .clickable {
                                        scope.launch { pagerState.animateScrollToPage(index) }
                                    }
                            ) {}
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Main Horizontal Pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) { page ->
                        val step = steps[page]
                        val isStepSimulated = simulatedStepState[page] == true

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Visual Artwork Graphic Card
                            Surface(
                                color = step.primaryColor,
                                shape = RoundedCornerShape(18.dp),
                                border = BorderStroke(1.5.dp, step.accentColor),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        Icon(
                                            imageVector = step.icon,
                                            contentDescription = null,
                                            tint = step.accentColor,
                                            modifier = Modifier.size(56.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = step.subtitle,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = step.accentColor,
                                            fontFamily = FontFamily.Monospace
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = step.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = step.description,
                                fontSize = 12.sp,
                                color = Color(0xFFCFD8DC),
                                textAlign = TextAlign.Center,
                                lineHeight = 17.sp
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Interactive Practice Simulation Button
                            Button(
                                onClick = {
                                    simulatedStepState = simulatedStepState + (page to true)
                                    Toast.makeText(context, step.simulationSuccessMsg, Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isStepSimulated) Color(0xFF2E7D32) else step.accentColor,
                                    contentColor = if (isStepSimulated) Color.White else Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("simulate_step_${step.stepNumber}_button")
                            ) {
                                Icon(
                                    imageVector = if (isStepSimulated) Icons.Default.CheckCircle else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isStepSimulated) "Practice Completed!" else step.simulationActionLabel,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }

                            if (isStepSimulated) {
                                Surface(
                                    color = Color(0xFF1B5E20).copy(alpha = 0.4f),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(0.5.dp, Color(0xFF00E676)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp)
                                ) {
                                    Text(
                                        text = "✓ ${step.simulationSuccessMsg}",
                                        fontSize = 10.sp,
                                        color = Color(0xFFB9F6CA),
                                        modifier = Modifier.padding(8.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Field Tips
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                step.tips.forEach { tip ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = step.accentColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = tip, fontSize = 11.sp, color = Color(0xFFB0BEC5))
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Bottom Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous
                        if (pagerState.currentPage > 0) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                                },
                                border = BorderStroke(1.dp, Color.Gray),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Previous", color = Color.White)
                            }
                        } else {
                            Spacer(modifier = Modifier.width(1.dp))
                        }

                        // Next or Finish
                        if (pagerState.currentPage < steps.size - 1) {
                            Button(
                                onClick = {
                                    scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("carousel_next_button")
                            ) {
                                Text("Next Step", color = Color.White, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, tint = Color.White)
                            }
                        } else {
                            Button(
                                onClick = {
                                    onComplete()
                                    onDismiss()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.testTag("carousel_finish_button")
                            ) {
                                Icon(imageVector = Icons.Default.DoneAll, contentDescription = null, tint = Color.Black)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Complete Training", color = Color.Black, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }
            }
        }
    }
}
