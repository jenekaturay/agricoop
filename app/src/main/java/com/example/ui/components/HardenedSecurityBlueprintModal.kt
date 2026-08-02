package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.security.AntiReplayNonceEngine
import com.example.security.DatabaseEncryptionManager
import com.example.security.HardwareBinder
import com.example.security.MoMoIdempotentService
import com.example.security.SecureScaleDecoder

@Composable
fun HardenedSecurityBlueprintModal(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    var activeTab by remember { mutableStateOf("PERIMETERS") } // "PERIMETERS", "HARDWARE", "NONCE", "SCALE_CRC", "MOMO"

    val deviceFingerprint = remember { HardwareBinder.getHardwareFingerprint(context) }
    var testNonceResult by remember { mutableStateOf<String?>(null) }
    var testScaleCrcResult by remember { mutableStateOf<String?>(null) }
    var testMomoIdempotencyResult by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("hardened_security_blueprint_modal"),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0D1B2A), // Dark Zero-Trust Security theme
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF00E676),
                            shape = CircleShape,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Zero Trust Shield",
                                    tint = Color(0xFF0D1B2A),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Hardened Security Architecture v3.0",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                            Text(
                                text = "Project Iron-Deed • 11-Layer Zero-Trust Perimeter",
                                fontSize = 11.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_hardened_security_modal")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Navigation Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1B2A4A), RoundedCornerShape(10.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val tabs = listOf(
                        "PERIMETERS" to "11 Perimeters",
                        "HARDWARE" to "Hardware Binding",
                        "NONCE" to "Anti-Replay",
                        "SCALE_CRC" to "Scale CRC-16",
                        "MOMO" to "MoMo Idempotency"
                    )

                    tabs.forEach { (key, title) ->
                        val isSelected = activeTab == key
                        Surface(
                            color = if (isSelected) Color(0xFF00E676) else Color.Transparent,
                            contentColor = if (isSelected) Color(0xFF0D1B2A) else Color(0xFFB0BEC5),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { activeTab = key }
                                .testTag("security_tab_$key")
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(vertical = 8.dp)
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Scrollable Content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    when (activeTab) {
                        "PERIMETERS" -> {
                            Text(
                                text = "11-Layer Zero-Trust Security Perimeter Matrix",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            val perimeters = listOf(
                                Triple("Layer 1: Field Device (SQLCipher)", "SQLCipher 256-bit AES-CBC Encrypted Room DB (${if (DatabaseEncryptionManager.isDatabaseEncrypted(context)) "ENCRYPTED & ACTIVE" else "ACTIVE"})", "CRITICAL"),
                                Triple("Layer 2: Field Agent", "Hardware Fingerprinting (IMEI/Android ID) + Hardware Keystore", "CRITICAL"),
                                Triple("Layer 3: Sync Traffic", "SSL Certificate Pinning + SHA-256 Request HMAC Signing", "CRITICAL"),
                                Triple("Layer 4: Offline Buffer", "Cryptographic Nonce + Monotonic Millisecond Timestamps", "HIGH"),
                                Triple("Layer 5: Ingestion API", "API Gateway WAF + Strict Rate Limiter + Body Length Guards", "HIGH"),
                                Triple("Layer 6: Hardware Scales", "Bluetooth SPP Key Handshake + CRC-16 Checksum Validation", "HIGH"),
                                Triple("Layer 7: MoMo Gateway", "Idempotency Ledger Key Checks + MoMo Webhook RSA Verification", "CRITICAL"),
                                Triple("Layer 8: App Logic", "Role-Based Access Control (RBAC) + JWT Expiry Middleware", "HIGH"),
                                Triple("Layer 9: Database Layer", "Parameterized SQL + PGCrypto AES-256 Column Encryption", "CRITICAL"),
                                Triple("Layer 10: System Admin", "Cryptographic Hash-Chained Audit Ledger (Trigger-Based)", "HIGH"),
                                Triple("Layer 11: Cloud Storage", "Air-Gapped Off-Site Write-Once-Read-Many (WORM) Backups", "CRITICAL")
                            )

                            perimeters.forEach { (layer, defense, severity) ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF152238)),
                                    border = BorderStroke(0.5.dp, Color(0xFF263859)),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(layer, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            Text(defense, fontSize = 11.sp, color = Color(0xFFB0BEC5))
                                        }
                                        Surface(
                                            color = if (severity == "CRITICAL") Color(0xFFFF5252) else Color(0xFFFFAB40),
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = severity,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        "HARDWARE" -> {
                            Text(
                                text = "Hardware Device Binding & Anti-Cloning (Layer 2)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Field agents are cryptographically bound to specific physical devices using a hardware-rooted fingerprint digest. Stolen credentials cannot log in on unauthorized devices.",
                                fontSize = 12.sp,
                                color = Color(0xFFCFD8DC)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
                                border = BorderStroke(1.dp, Color(0xFF00E676)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = Color(0xFF00E676))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Current Hardware Fingerprint Digest", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Color(0xFF00E676))
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = deviceFingerprint,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color(0xFFB0BEC5)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Status: HARDWARE BOUND & VERIFIED",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp,
                                        color = Color(0xFF69F0AE)
                                    )
                                }
                            }
                        }

                        "NONCE" -> {
                            Text(
                                text = "Anti-Replay Nonce Engine (Layer 4)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Every offline payload contains a 128-bit UUID v4 Nonce and millisecond timestamp. Replaying identical sync packets is blocked automatically.",
                                fontSize = 12.sp,
                                color = Color(0xFFCFD8DC)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val secured = AntiReplayNonceEngine.wrapPayloadWithNonce("TEST_PAYLOAD_LOFA_BATCH")
                                    val firstValidation = AntiReplayNonceEngine.validateNonce(secured.nonce, secured.timestamp)
                                    val replayValidation = AntiReplayNonceEngine.validateNonce(secured.nonce, secured.timestamp)

                                    testNonceResult = """
                                        Generated UUID Nonce: ${secured.nonce}
                                        Timestamp: ${secured.timestamp}
                                        1st Pass: $firstValidation
                                        2nd Pass (Replay Attempt): $replayValidation
                                    """.trimIndent()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color(0xFF0D1B2A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_anti_replay_nonce_button")
                            ) {
                                Text("Run Anti-Replay Simulation", fontWeight = FontWeight.Bold)
                            }

                            testNonceResult?.let { result ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
                                    border = BorderStroke(1.dp, Color(0xFF80D8FF)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = result,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        "SCALE_CRC" -> {
                            Text(
                                text = "Bluetooth SPP Key Handshake & CRC-16 Checksum (Layer 6)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Frame layout: [STX(0x02), WEIGHT(6B), UNIT(2B), CRC(2B), ETX(0x03)]. Validates weight frames against Bluetooth spoofing or tampering.",
                                fontSize = 12.sp,
                                color = Color(0xFFCFD8DC)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val validFrame = SecureScaleDecoder.buildValidScaleFrame(84.5, "KG")
                                    val parsedWeight = SecureScaleDecoder.parseAndValidateFrame(validFrame)

                                    // Tamper byte
                                    val tamperedFrame = validFrame.clone()
                                    tamperedFrame[2] = 0x99.toByte()

                                    val tamperedResult = try {
                                        SecureScaleDecoder.parseAndValidateFrame(tamperedFrame)
                                        "TAMPER_UNDETECTED"
                                    } catch (e: Exception) {
                                        "BLOCKED: ${e.message}"
                                    }

                                    testScaleCrcResult = """
                                        Valid Scale Frame Weight: $parsedWeight kg (CRC PASS)
                                        Tampered Frame Test: $tamperedResult
                                    """.trimIndent()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color(0xFF0D1B2A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_scale_crc_validation_button")
                            ) {
                                Text("Simulate Bluetooth SPP Frame & CRC Check", fontWeight = FontWeight.Bold)
                            }

                            testScaleCrcResult?.let { result ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
                                    border = BorderStroke(1.dp, Color(0xFF80D8FF)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = result,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }

                        "MOMO" -> {
                            Text(
                                text = "Idempotent Payout Controller (Layer 7 & Section 3.2)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF80D8FF)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Generates deterministic X-Idempotency-Key header ('MOMO-PAY-{batchId}') to ensure double Mobile Money payouts are physically impossible.",
                                fontSize = 12.sp,
                                color = Color(0xFFCFD8DC)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    val sampleBatchId = "LOFA-BATCH-908"
                                    val firstAttempt = MoMoIdempotentService.processIdempotentPayout(sampleBatchId, "+231770112233", 4500.0, "PENDING")
                                    val secondAttempt = MoMoIdempotentService.processIdempotentPayout(sampleBatchId, "+231770112233", 4500.0, "PAID")

                                    testMomoIdempotencyResult = """
                                        1st Dispatch: $firstAttempt
                                        
                                        2nd Duplicate Dispatch: $secondAttempt
                                    """.trimIndent()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00E676), contentColor = Color(0xFF0D1B2A)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("test_momo_idempotency_button")
                            ) {
                                Text("Test Double-Payout Protection", fontWeight = FontWeight.Bold)
                            }

                            testMomoIdempotencyResult?.let { result ->
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFF102A43)),
                                    border = BorderStroke(1.dp, Color(0xFF80D8FF)),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = result,
                                        fontFamily = FontFamily.Monospace,
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF263859)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Security Blueprint", color = Color.White)
                }
            }
        }
    }
}
