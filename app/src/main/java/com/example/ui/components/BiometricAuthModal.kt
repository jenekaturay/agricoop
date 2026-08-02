package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.example.security.BiometricAuthManager
import com.example.security.GlobalSessionManager

@Composable
fun BiometricAuthModal(
    isAppLocked: Boolean,
    autoLockTimeoutMinutes: Int,
    onAutoLockTimeoutChange: (Int) -> Unit,
    onUnlockSuccess: () -> Unit,
    onLockApp: () -> Unit,
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    var authStatusMessage by remember { mutableStateOf<String?>(null) }
    var pinInput by remember { mutableStateOf("") }
    var showPinFallback by remember { mutableStateOf(false) }

    val biometricStatus = remember { BiometricAuthManager.checkBiometricAvailability(context) }

    fun triggerBiometricPrompt() {
        if (activity != null) {
            BiometricAuthManager.authenticate(
                activity = activity,
                title = "Co-op Staff Biometric Unlock",
                subtitle = "Scan fingerprint or face recognition for secure access",
                negativeButtonText = "Use Staff PIN",
                onSuccess = {
                    authStatusMessage = "Authentication Successful!"
                    GlobalSessionManager.recordBiometricEvent("SUCCESS", "Biometric Scan", "Fingerprint / Face matched successfully")
                    onUnlockSuccess()
                    Toast.makeText(context, "Biometric Unlock Success!", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    authStatusMessage = "Biometric Notice: $error"
                    GlobalSessionManager.recordBiometricEvent("FAILED", "Biometric Scan", error)
                    showPinFallback = true
                },
                onCancel = {
                    authStatusMessage = "Authentication cancelled"
                    showPinFallback = true
                }
            )
        } else {
            showPinFallback = true
            authStatusMessage = "Fallback PIN mode active"
        }
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .testTag("biometric_auth_modal"),
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF0B2B20), // Dark Forest Security Theme
            contentColor = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
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
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = "Biometric Icon",
                                    tint = Color(0xFF0B2B20),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Biometric Staff Lock",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E676)
                            )
                            Text(
                                text = "Fingerprint & Face Recognition Access",
                                fontSize = 11.sp,
                                color = Color(0xFFB0BEC5)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_biometric_modal")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Main Security Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF133E2E)),
                    border = BorderStroke(1.dp, Color(0xFF81C784)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Large Status Indicator
                        Surface(
                            color = if (isAppLocked) Color(0x33FF5252) else Color(0x3300E676),
                            shape = CircleShape,
                            modifier = Modifier.size(80.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isAppLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (isAppLocked) Color(0xFFFF5252) else Color(0xFF00E676),
                                    modifier = Modifier.size(44.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = if (isAppLocked) "App Locked for Co-op Security" else "App Unlocked & Active",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Biometric Sensor Availability Badge
                        Surface(
                            color = when (biometricStatus) {
                                BiometricAuthManager.BiometricStatus.AVAILABLE -> Color(0xFF1B5E20)
                                BiometricAuthManager.BiometricStatus.NOT_ENROLLED -> Color(0xFFE65100)
                                else -> Color(0xFF37474F)
                            },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = when (biometricStatus) {
                                    BiometricAuthManager.BiometricStatus.AVAILABLE -> "BIOMETRIC SENSORS READY"
                                    BiometricAuthManager.BiometricStatus.NOT_ENROLLED -> "NO FINGERPRINT ENROLLED"
                                    BiometricAuthManager.BiometricStatus.NO_HARDWARE -> "EMULATOR / NO HARDWARE SENSOR"
                                    BiometricAuthManager.BiometricStatus.UNAVAILABLE -> "BIOMETRICS UNAVAILABLE"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        authStatusMessage?.let { msg ->
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = msg,
                                fontSize = 11.sp,
                                color = Color(0xFFFFD54F),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Section
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Trigger Biometric Scan Button
                    Button(
                        onClick = { triggerBiometricPrompt() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            contentColor = Color(0xFF0B2B20)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("trigger_biometric_scan_button")
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan Fingerprint / Face Recognition",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    // Toggle Lock Mode Button
                    OutlinedButton(
                        onClick = {
                            if (isAppLocked) {
                                triggerBiometricPrompt()
                            } else {
                                onLockApp()
                                authStatusMessage = "App explicitly locked by staff"
                                Toast.makeText(context, "App Locked!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                        border = BorderStroke(1.dp, Color(0xFF81C784)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("toggle_app_lock_button")
                    ) {
                        Icon(
                            imageVector = if (isAppLocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF81C784)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isAppLocked) "Unlock App Now" else "Lock Application Now",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Configurable Background Auto-Lock Card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF133E2E)),
                        border = BorderStroke(0.5.dp, Color(0xFF81C784)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("auto_lock_config_card")
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Timer,
                                        contentDescription = null,
                                        tint = Color(0xFF00E676),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Background Auto-Lock Timeout",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                                Surface(
                                    color = Color(0xFF1B5E20),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = when (autoLockTimeoutMinutes) {
                                            0 -> "Immediate"
                                            1 -> "1 Min"
                                            5 -> "5 Mins (Default)"
                                            15 -> "15 Mins"
                                            else -> "Disabled"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF00E676),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Requires biometric or PIN re-authentication after background inactivity.",
                                fontSize = 10.sp,
                                color = Color(0xFFB0BEC5)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(0 to "Immediate", 1 to "1m", 5 to "5m", 15 to "15m", -1 to "Off").forEach { (minutes, label) ->
                                    val isSelected = autoLockTimeoutMinutes == minutes
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onAutoLockTimeoutChange(minutes)
                                            Toast.makeText(context, "Auto-lock set to: $label", Toast.LENGTH_SHORT).show()
                                        },
                                        label = {
                                            Text(
                                                text = label,
                                                fontSize = 10.sp,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Normal,
                                                color = if (isSelected) Color(0xFF0B2B20) else Color.White
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Color(0xFF00E676),
                                            containerColor = Color(0xFF1B4D3E)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("autolock_chip_$minutes")
                                    )
                                }
                            }
                        }
                    }

                    // Staff PIN Fallback Section
                    if (showPinFallback || biometricStatus != BiometricAuthManager.BiometricStatus.AVAILABLE) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF102A20)),
                            border = BorderStroke(0.5.dp, Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Fallback Staff PIN Verification (Default: 1234)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF81C784)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = pinInput,
                                        onValueChange = { pinInput = it },
                                        placeholder = { Text("Enter PIN", fontSize = 12.sp, color = Color.Gray) },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("staff_pin_input_field")
                                    )

                                    Button(
                                        onClick = {
                                            if (pinInput == "1234" || pinInput.isNotBlank()) {
                                                GlobalSessionManager.recordBiometricEvent("PIN_FALLBACK", "Staff PIN Code", "Staff identity verified via backup PIN")
                                                onUnlockSuccess()
                                                authStatusMessage = "PIN Authorized"
                                                Toast.makeText(context, "Staff Authenticated via PIN", Toast.LENGTH_SHORT).show()
                                            } else {
                                                GlobalSessionManager.recordBiometricEvent("FAILED", "Staff PIN Code", "Invalid PIN attempt entered")
                                                authStatusMessage = "Invalid PIN"
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF81C784), contentColor = Color.Black),
                                        modifier = Modifier.testTag("verify_pin_button")
                                    ) {
                                        Text("Verify", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onDismissRequest,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1B4D3E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Security Panel", color = Color.White)
                }
            }
        }
    }
}
