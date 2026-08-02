package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.security.BiometricAuthEvent
import com.example.security.DatabaseEncryptionManager
import com.example.security.GlobalSessionManager
import java.text.SimpleDateFormat
import java.util.*

/**
 * Security Status UI Dashboard Component.
 * Displays real-time security telemetry:
 * 1. Last successful login timestamp & auth provider.
 * 2. Database encryption status (SQLCipher 256-bit AES-CBC Room Database).
 * 3. Recent biometric authentication and PIN fallback audit logs.
 */
@Composable
fun SecurityStatusDashboardComponent(
    onTriggerBiometricModal: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lastLoginTimestamp by GlobalSessionManager.lastLoginTimestamp.collectAsStateWithLifecycle()
    val biometricEvents by GlobalSessionManager.biometricEvents.collectAsStateWithLifecycle()
    val autoLockTimeoutMinutes by GlobalSessionManager.autoLockTimeoutMinutes.collectAsStateWithLifecycle()

    var showAllEvents by remember { mutableStateOf(false) }
    var showExportModal by remember { mutableStateOf(false) }
    var showFieldGuideModal by remember { mutableStateOf(false) }

    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.US) }
    val timeAgoText = remember(lastLoginTimestamp) {
        val diffMillis = System.currentTimeMillis() - lastLoginTimestamp
        val mins = diffMillis / (1000 * 60)
        when {
            mins < 1 -> "Just now"
            mins < 60 -> "$mins mins ago"
            else -> "${mins / 60} hours ago"
        }
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF09291E)),
        border = BorderStroke(1.dp, Color(0xFF00E676)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = modifier.testTag("security_status_dashboard_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header Row: Security Status Title & Live Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFF1B5E20),
                        shape = CircleShape,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Security Shield",
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Security & Trust Telemetry",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Co-op Hardened Cryptographic Vault",
                            fontSize = 11.sp,
                            color = Color(0xFF81C784)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { showFieldGuideModal = true },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("security_info_help_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = "Security Info Guide",
                            tint = Color(0xFF80D8FF),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    Surface(
                        color = Color(0xFF00E676).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E676))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color(0xFF00E676), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ACTIVE VAULT",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF00E676)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Grid of Security Metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Metric 1: Last Successful Login Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF113D2D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_last_login_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                tint = Color(0xFF80D8FF),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Last Login",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB0BEC5)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = timeAgoText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Text(
                            text = dateFormat.format(Date(lastLoginTimestamp)),
                            fontSize = 9.sp,
                            color = Color(0xFF90A4AE)
                        )
                    }
                }

                // Metric 2: Database Encryption Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF113D2D)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("metric_db_encryption_card")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF00E676),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "DB Encryption",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB0BEC5)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (DatabaseEncryptionManager.isDatabaseEncrypted(context)) "SQLCipher 256-Bit" else "Encrypted",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF69F0AE)
                        )

                        Text(
                            text = "AES-CBC Key in Hardware Vault",
                            fontSize = 9.sp,
                            color = Color(0xFF90A4AE)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Database Encryption Deep Dive Info Banner
            Surface(
                color = Color(0xFF134835),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(0.5.dp, Color(0xFF81C784).copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFFFFD54F),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SQLCipher Room Database Status",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Farmer PII, weight scale logs, & MoMo payout ledgers are encrypted at rest with 256-bit AES entropy.",
                            fontSize = 10.sp,
                            color = Color(0xFFCFD8DC)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // SQLCipher Key Rotation Vault Card
            DatabaseKeyRotationCard()

            Spacer(modifier = Modifier.height(12.dp))

            // Co-op Manager FCM Remote Wipe Control Card
            RemoteWipeControlCard()

            Spacer(modifier = Modifier.height(16.dp))

            // Section Header: Biometric Authentication Events Log
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Recent Biometric & Auth Audit Events",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                TextButton(
                    onClick = { showAllEvents = !showAllEvents },
                    modifier = Modifier.testTag("toggle_events_button")
                ) {
                    Text(
                        text = if (showAllEvents) "Show Less" else "View All (${biometricEvents.size})",
                        fontSize = 11.sp,
                        color = Color(0xFF80D8FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Event Logs List
            val displayedEvents = if (showAllEvents) biometricEvents else biometricEvents.take(3)

            if (displayedEvents.isEmpty()) {
                Text(
                    text = "No biometric authentication events logged yet.",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    displayedEvents.forEach { event ->
                        BiometricEventRow(event = event, dateFormat = dateFormat)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Row: Lock Now, Test Biometric Scan & Export Compliance Report
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        GlobalSessionManager.lockSession()
                        onTriggerBiometricModal()
                    },
                    border = BorderStroke(1.dp, Color(0xFFFF5252)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFF8A80)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("security_lock_now_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lock", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showExportModal = true },
                    border = BorderStroke(1.dp, Color(0xFF80D8FF)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF80D8FF)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .testTag("security_export_log_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export Log", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onTriggerBiometricModal,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF00E676),
                        contentColor = Color(0xFF07241A)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("security_test_biometric_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showExportModal) {
        SecurityAuditExportModal(
            onDismissRequest = { showExportModal = false }
        )
    }

    if (showFieldGuideModal) {
        SecurityFieldGuideModal(
            onDismissRequest = { showFieldGuideModal = false }
        )
    }
}

@Composable
private fun BiometricEventRow(
    event: BiometricAuthEvent,
    dateFormat: SimpleDateFormat
) {
    val (statusColor, icon, badgeText) = when (event.status) {
        "SUCCESS" -> Triple(Color(0xFF00E676), Icons.Default.CheckCircle, "SUCCESS")
        "PIN_FALLBACK" -> Triple(Color(0xFFFFD54F), Icons.Default.VpnKey, "PIN VERIFIED")
        "AUTO_LOCKED" -> Triple(Color(0xFFB388FF), Icons.Default.Timer, "AUTO-LOCKED")
        else -> Triple(Color(0xFFFF5252), Icons.Default.Error, "FAILED")
    }

    Surface(
        color = Color(0xFF0D3326),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, statusColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = event.method,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateFormat.format(Date(event.timestamp)),
                            fontSize = 9.sp,
                            color = Color(0xFF80CBC4)
                        )
                    }
                    Text(
                        text = event.detail,
                        fontSize = 10.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }
            }

            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = badgeText,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
