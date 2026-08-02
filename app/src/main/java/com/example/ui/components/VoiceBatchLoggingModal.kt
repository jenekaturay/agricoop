package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.FarmerEntity
import com.example.service.AppLanguage
import com.example.service.ParsedVoiceBatch
import com.example.service.VoiceBatchParserService
import com.example.ui.components.TuberYieldThresholdIndicator
import kotlinx.coroutines.delay

@Composable
fun VoiceBatchLoggingModal(
    farmers: List<FarmerEntity>,
    currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    onApplyVoiceInputs: (
        farmer: FarmerEntity?,
        cropType: String,
        weightKg: Double,
        starchPct: Double
    ) -> Unit,
    onDismiss: () -> Unit
) {
    var isListening by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var isAudioPlaying by remember { mutableStateOf(false) }

    var currentTranscript by remember {
        mutableStateOf("Tap microphone below and verbally state farmer name, tuber weight, and crop type...")
    }

    var parsedResult by remember { mutableStateOf<ParsedVoiceBatch?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    // Simulated Speech Recording Trigger
    fun startListeningWithPrompt(prompt: String) {
        isListening = true
        isProcessing = false
        currentTranscript = "Listening... Speak clearly into microphone"
        parsedResult = null
    }

    fun finishListeningWithTranscript(prompt: String) {
        isListening = false
        isProcessing = true
        currentTranscript = "\"$prompt\""

        val parsed = VoiceBatchParserService.parseVoiceTranscript(prompt)
        parsedResult = parsed
        isProcessing = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("voice_batch_modal")
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Modal Top Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.RecordVoiceOver,
                                    contentDescription = null,
                                    tint = Color(0xFF0B3D2E),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Voice Batch Logging",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color(0xFF0B3D2E)
                            )
                            Text(
                                text = "Speech-to-Text Tuber Confirmation",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_voice_modal_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pulsing Mic Circle Button
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.padding(vertical = 12.dp)
                ) {
                    if (isListening) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(pulseScale)
                                .background(Color(0x3381C784), CircleShape)
                        )
                    }

                    Surface(
                        color = if (isListening) Color(0xFFD32F2F) else Color(0xFF0B3D2E),
                        shape = CircleShape,
                        shadowElevation = 6.dp,
                        modifier = Modifier
                            .size(76.dp)
                            .clickable {
                                if (isListening) {
                                    finishListeningWithTranscript(VoiceBatchParserService.sampleVoicePrompts[0])
                                } else {
                                    startListeningWithPrompt("Tap sample phrase or speak")
                                }
                            }
                            .testTag("microphone_record_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                                contentDescription = "Record Voice",
                                tint = Color(0xFFFFD54F),
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }
                }

                Text(
                    text = if (isListening) "Listening... Tap to stop" else if (isProcessing) "Processing voice..." else "Tap Microphone to Speak Confirmation",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isListening) Color(0xFFD32F2F) else Color(0xFF0B3D2E)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Live Speech Transcript Box
                Surface(
                    color = Color(0xFFF6F9F7),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SPEECH TRANSCRIPT",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Gray,
                                letterSpacing = 1.sp
                            )
                            if (parsedResult != null) {
                                Badge(containerColor = Color(0xFFE8F5E9), contentColor = Color(0xFF2E7D32)) {
                                    Text("Confidence 96%", fontSize = 9.sp, modifier = Modifier.padding(2.dp))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = currentTranscript,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF1B5E20)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Sample Spoken Voice Phrases Quick Taps
                Text(
                    text = "Try verbal phrases in Liberian English / Dialect:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0B3D2E),
                    modifier = Modifier.align(Alignment.Start)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    VoiceBatchParserService.sampleVoicePrompts.take(3).forEach { prompt ->
                        Surface(
                            color = Color(0xFFFAFAFA),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { finishListeningWithTranscript(prompt) }
                                .testTag("voice_sample_${prompt.take(10)}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MicNone,
                                    contentDescription = null,
                                    tint = Color(0xFF2E7D32),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "\"$prompt\"",
                                    fontSize = 11.sp,
                                    color = Color(0xFF333333)
                                )
                            }
                        }
                    }
                }

                // Extracted Fields Breakdown Card
                parsedResult?.let { parsed ->
                    Spacer(modifier = Modifier.height(14.dp))

                    val matchedFarmer = remember(parsed.extractedFarmerName, farmers) {
                        if (parsed.extractedFarmerName != null) {
                            farmers.find { it.fullName.contains(parsed.extractedFarmerName, ignoreCase = true) }
                        } else farmers.firstOrNull()
                    }

                    val weight = parsed.extractedWeightKg ?: 420.0
                    val crop = parsed.extractedCropType ?: "CASSAVA"
                    val calculatedPayoutLrd = weight * 85.0

                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32), modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Verbal Confirmation Verified", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                                }

                                // Audio Playback Audio Confirmation Button
                                Surface(
                                    color = if (isAudioPlaying) Color(0xFFFFD54F) else Color(0xFF0B3D2E),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { isAudioPlaying = !isAudioPlaying }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Read Aloud",
                                            tint = if (isAudioPlaying) Color.Black else Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isAudioPlaying) "Playing Audio..." else "Read Aloud",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isAudioPlaying) Color.Black else Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Farmer Member:", fontSize = 10.sp, color = Color.Gray)
                                    Text(matchedFarmer?.fullName ?: "Flomo Kpelle", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Crop & Weight:", fontSize = 10.sp, color = Color.Gray)
                                    Text("$crop • ${weight} Kg", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Starch Content:", fontSize = 10.sp, color = Color.Gray)
                                    Text("${parsed.extractedStarchPct ?: 25.0}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0B3D2E))
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("Calculated Payout:", fontSize = 10.sp, color = Color.Gray)
                                    Text("${"%,.0f".format(calculatedPayoutLrd)} LRD", fontSize = 13.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    var spokenWeightKg by remember(parsed) { mutableDoubleStateOf(weight) }

                    TuberYieldThresholdIndicator(
                        cropType = crop,
                        currentWeightKg = spokenWeightKg,
                        onCapWeightToThreshold = { cappedKg ->
                            spokenWeightKg = cappedKg
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Button(
                        onClick = {
                            onApplyVoiceInputs(
                                matchedFarmer,
                                crop,
                                spokenWeightKg,
                                parsed.extractedStarchPct ?: 25.0
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("apply_voice_inputs_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFFFD54F))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Apply Spoken Values to Batch Form",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}
