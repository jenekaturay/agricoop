package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

/**
 * Security Info & Field Agent Guidelines Overlay.
 * Explains how staff can maintain biometric credential safety, app session locking,
 * and encrypted offline data protection while weighing produce in rural co-op hubs.
 */
@Composable
fun SecurityFieldGuideModal(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f)
                .testTag("security_field_guide_modal"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF09291E),
            border = BorderStroke(1.dp, Color(0xFF00E676))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Modal Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF1B5E20),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Field Staff Security Guide",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Biometric & Session Protection Best Practices",
                                fontSize = 11.sp,
                                color = Color(0xFF81C784)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_field_guide_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content Cards
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Tip 1: Biometric Credential Hygiene
                    GuideTopicCard(
                        icon = Icons.Default.Fingerprint,
                        title = "1. Biometric Credential Hygiene",
                        accentColor = Color(0xFF00E676),
                        bullets = listOf(
                            "Only enroll official co-op staff fingerprints into Android device security settings.",
                            "Keep the fingerprint sensor clean and dry, especially when weighing wet cassava or tubers.",
                            "If biometric scan fails due to dust or humidity, use your authorized 4-digit Staff PIN code as a secure fallback."
                        )
                    )

                    // Tip 2: Session & Lock Timeout Rules
                    GuideTopicCard(
                        icon = Icons.Default.Timer,
                        title = "2. Session & Auto-Lock Protection",
                        accentColor = Color(0xFF80D8FF),
                        bullets = listOf(
                            "The app automatically locks after background inactivity (default: 5 minutes).",
                            "Always tap 'Lock Session' before walking away from the scale platform or leaving the handheld unattended.",
                            "Do not share your Staff PIN code with farmers or unverified personnel."
                        )
                    )

                    // Tip 3: SQLCipher Encrypted Storage
                    GuideTopicCard(
                        icon = Icons.Default.Lock,
                        title = "3. SQLCipher Offline Data Vault",
                        accentColor = Color(0xFFFFD54F),
                        bullets = listOf(
                            "All farmer records, scale weights, and payout ledgers are encrypted with 256-bit AES-CBC.",
                            "Even if the physical device is lost or stole, local database files cannot be read without the hardware key.",
                            "Local records sync automatically to PostGIS Cloud once cellular coverage is restored."
                        )
                    )

                    // Tip 4: Physical & Compliance Security
                    GuideTopicCard(
                        icon = Icons.Default.Shield,
                        title = "4. Physical Hardware & Compliance",
                        accentColor = Color(0xFFB388FF),
                        bullets = listOf(
                            "Each handheld device is hardware-bound with a cryptographic device fingerprint.",
                            "Periodically export compliance logs for co-op manager audits using the 'Export Log' feature.",
                            "Report lost or damaged devices immediately to the regional co-op administrator."
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Understood / Close Button
                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color(0xFF07241A)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("understand_security_guide_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "I Understand Field Security Rules",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideTopicCard(
    icon: ImageVector,
    title: String,
    accentColor: Color,
    bullets: List<String>
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF113D2D)),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.5f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            bullets.forEach { bullet ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "• ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor
                    )
                    Text(
                        text = bullet,
                        fontSize = 11.sp,
                        color = Color(0xFFCFD8DC),
                        lineHeight = 15.sp
                    )
                }
            }
        }
    }
}
