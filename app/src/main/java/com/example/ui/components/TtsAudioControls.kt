package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TtsAudioConfirmationBanner(
    isTtsEnabled: Boolean,
    isSpeaking: Boolean,
    onToggleTts: () -> Unit,
    onTestSpeak: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isTtsEnabled) Color(0xFF0D47A1) else Color(0xFF424242),
        contentColor = Color.White,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("tts_audio_confirmation_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = if (isSpeaking) Color(0xFFFFD54F) else Color(0x33FFFFFF),
                    shape = CircleShape,
                    modifier = Modifier.size(32.dp)
                ) {
                    IconButton(
                        onClick = onToggleTts,
                        modifier = Modifier.testTag("toggle_tts_button")
                    ) {
                        Icon(
                            imageVector = if (isTtsEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Toggle Text To Speech",
                            tint = if (isSpeaking) Color(0xFF0B3D2E) else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Auditory Voice Confirmation (TTS)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Surface(
                            color = if (isTtsEnabled) Color(0xFF81C784) else Color(0xFFFF8A80),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = if (isTtsEnabled) "ACTIVE" else "MUTED",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isTtsEnabled) "Spoken weight & payout confirmation enabled for non-literate support" else "Voice readout disabled - Tap speaker to enable",
                        fontSize = 10.sp,
                        color = Color(0xFFE0E0E0),
                        maxLines = 1
                    )
                }
            }

            if (isTtsEnabled) {
                Surface(
                    color = Color(0x33FFFFFF),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .clickable { onTestSpeak() }
                        .testTag("test_voice_readout_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Test Voice",
                            tint = Color(0xFFFFD54F),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Test Voice",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SpeakIconButton(
    onClick: () -> Unit,
    contentDescription: String = "Speak aloud",
    tint: Color = Color(0xFF0B3D2E),
    modifier: Modifier = Modifier,
    testTag: String = "speak_icon_button"
) {
    Surface(
        color = tint.copy(alpha = 0.12f),
        shape = CircleShape,
        modifier = modifier
            .size(34.dp)
            .testTag(testTag)
            .clickable { onClick() }
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
