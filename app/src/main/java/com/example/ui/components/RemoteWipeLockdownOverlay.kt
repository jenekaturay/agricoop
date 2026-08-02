package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhonelinkErase
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.security.RemoteWipeManager
import com.example.security.WipeEventDetails
import kotlinx.coroutines.launch

/**
 * High-priority Lockdown Overlay displayed whenever a device has been remote wiped
 * by a Co-op Manager via Firebase Cloud Messaging due to loss or theft.
 * Prevents unauthorized access and provides a Staff PIN restore mechanism.
 */
@Composable
fun RemoteWipeLockdownOverlay(
    wipeDetails: WipeEventDetails,
    onRestored: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var showPinDialog by remember { mutableStateOf(false) }
    var pinInput by remember { mutableStateOf("") }
    var pinError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { /* Modal non-dismissable */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F0000))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF260505)),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFFF2A2A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remote_wipe_lockdown_card")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Alert Icon
                    Surface(
                        color = Color(0xFFFF2A2A),
                        shape = RoundedCornerShape(100.dp),
                        modifier = Modifier.size(64.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.PhonelinkErase,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "EMERGENCY LOCKDOWN",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF5252),
                        letterSpacing = 1.sp
                    )

                    Text(
                        text = "DEVICE REMOTE WIPED VIA FCM",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "This mobile scale terminal was wiped by Co-op Management following a loss/theft report.",
                        fontSize = 12.sp,
                        color = Color(0xFFFFCDD2),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wipe Details Container
                    Surface(
                        color = Color(0xFF140000),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFFB71C1C)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            DetailRow(label = "Trigger Source:", value = wipeDetails.source)
                            DetailRow(label = "Co-op Manager:", value = wipeDetails.managerId)
                            DetailRow(label = "Wipe Reason:", value = wipeDetails.reason)
                            DetailRow(label = "Timestamp:", value = wipeDetails.formattedTimestamp)
                            DetailRow(label = "FCM Msg ID:", value = wipeDetails.fcmMessageId.take(20))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Status Checklist
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        WipeStatusItem(text = "SQLCipher Room Database Tables Cleared")
                        WipeStatusItem(text = "Farmer Biometric Profiles Purged")
                        WipeStatusItem(text = "Produce Weighing Batches Purged")
                        WipeStatusItem(text = "MoMo Payout Credentials Purged")
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Restore Action Button
                    Button(
                        onClick = { showPinDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFD32F2F),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_restore_wipe_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Override & Restore Device",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // PIN Override Dialog
            if (showPinDialog) {
                AlertDialog(
                    onDismissRequest = { showPinDialog = false },
                    containerColor = Color(0xFF1A0505),
                    title = {
                        Text(
                            text = "Co-op Admin Unlock",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    },
                    text = {
                        Column {
                            Text(
                                text = "Enter Co-op Staff PIN (Default: 8841 or 9999) to clear the remote wipe lockdown.",
                                fontSize = 12.sp,
                                color = Color(0xFFFFCDD2)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            OutlinedTextField(
                                value = pinInput,
                                onValueChange = {
                                    pinInput = it
                                    pinError = false
                                },
                                label = { Text("Staff PIN", color = Color(0xFFEF9A9A)) },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = pinError,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFFFF5252),
                                    unfocusedBorderColor = Color(0xFFB71C1C),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_pin_input")
                            )
                            if (pinError) {
                                Text(
                                    text = "Invalid Admin PIN",
                                    color = Color(0xFFFF5252),
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    val success = RemoteWipeManager.restoreDeviceForAdmin(context, pinInput)
                                    if (success) {
                                        showPinDialog = false
                                        Toast.makeText(context, "Device Lockdown Cleared!", Toast.LENGTH_SHORT).show()
                                        onRestored()
                                    } else {
                                        pinError = true
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF5252))
                        ) {
                            Text("Unlock", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showPinDialog = false }) {
                            Text("Cancel", color = Color(0xFFFFCDD2))
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 10.sp, color = Color(0xFFEF9A9A), fontWeight = FontWeight.Bold)
        Text(text = value, fontSize = 10.sp, color = Color.White, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun WipeStatusItem(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = Color(0xFFFF5252),
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(text = text, fontSize = 11.sp, color = Color.White)
    }
}
