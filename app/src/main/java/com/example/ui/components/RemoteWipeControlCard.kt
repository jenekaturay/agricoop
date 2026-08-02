package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CellTower
import androidx.compose.material.icons.filled.PhonelinkErase
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.security.RemoteWipeManager
import kotlinx.coroutines.launch

/**
 * Co-op Manager Remote Wipe Control Panel Card integrated in the Security Dashboard.
 * Displays Firebase Cloud Messaging (FCM) registration state and provides an emergency trigger
 * for co-op managers to simulate or execute a remote wipe on stolen/lost devices.
 */
@Composable
fun RemoteWipeControlCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var fcmToken by remember { mutableStateOf("Fetching token...") }
    var managerIdInput by remember { mutableStateOf("MGR_THERESA_LIB") }
    var wipeReasonInput by remember { mutableStateOf("Scale terminal reported lost in Nimba District") }
    var isExecutingWipe by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        RemoteWipeManager.fetchFcmToken(context) { token ->
            fcmToken = token
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E0A0A)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFFFF5252).copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PhonelinkErase,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "FCM Remote Wipe Command",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Co-op Manager Lost/Stolen Device Clear",
                            fontSize = 10.sp,
                            color = Color(0xFFFFCDD2)
                        )
                    }
                }

                Surface(
                    color = Color(0xFFD32F2F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CellTower,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FCM READY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // FCM Token Display
            Surface(
                color = Color(0xFF0F0000),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(0.5.dp, Color(0xFFB71C1C))
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text(
                        text = "DEVICE FCM TOKEN:",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFEF9A9A)
                    )
                    Text(
                        text = fcmToken,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF80D8FF),
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inputs Form
            OutlinedTextField(
                value = managerIdInput,
                onValueChange = { managerIdInput = it },
                label = { Text("Manager ID", fontSize = 11.sp, color = Color(0xFFEF9A9A)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF5252),
                    unfocusedBorderColor = Color(0xFFB71C1C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fcm_wipe_manager_id")
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = wipeReasonInput,
                onValueChange = { wipeReasonInput = it },
                label = { Text("Reason for Loss/Theft Purge", fontSize = 11.sp, color = Color(0xFFEF9A9A)) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFFFF5252),
                    unfocusedBorderColor = Color(0xFFB71C1C),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("fcm_wipe_reason")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trigger Button
            Button(
                onClick = {
                    isExecutingWipe = true
                    scope.launch {
                        val res = RemoteWipeManager.executeRemoteWipe(
                            context = context,
                            managerId = managerIdInput,
                            reason = wipeReasonInput,
                            fcmMessageId = "FCM_TRIGGER_CONSOLE_${System.currentTimeMillis()}",
                            source = "FCM_COOP_MANAGER_CONSOLE"
                        )
                        isExecutingWipe = false
                        Toast.makeText(context, res.message, Toast.LENGTH_LONG).show()
                    }
                },
                enabled = !isExecutingWipe,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF2A2A),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trigger_remote_wipe_fcm_button")
            ) {
                if (isExecutingWipe) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Purging Database...", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "Trigger FCM Remote Wipe Command", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
