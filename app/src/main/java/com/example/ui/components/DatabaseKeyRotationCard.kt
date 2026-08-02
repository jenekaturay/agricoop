package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Shield
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
import com.example.security.DatabaseKeyRotationService
import com.example.security.KeyRotationStatus
import kotlinx.coroutines.launch

/**
 * UI Component for displaying SQLCipher database key rotation status,
 * schedule policy, and triggering on-demand database re-encryption without data loss.
 */
@Composable
fun DatabaseKeyRotationCard(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rotationStatus by remember { mutableStateOf(DatabaseKeyRotationService.getRotationStatus(context)) }
    var isRotating by remember { mutableStateOf(false) }

    fun refreshStatus() {
        rotationStatus = DatabaseKeyRotationService.getRotationStatus(context)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF113D2D)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF80D8FF).copy(alpha = 0.5f)),
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
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = Color(0xFF80D8FF),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SQLCipher Key Rotation Vault",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "256-bit AES Database Re-Encryption",
                            fontSize = 10.sp,
                            color = Color(0xFF80CBC4)
                        )
                    }
                }

                Surface(
                    color = Color(0xFF00838F),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Key Version ${rotationStatus.currentVersion}",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "LAST ROTATION",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB0BEC5)
                    )
                    Text(
                        text = rotationStatus.formattedLastRotationDate,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "SCHEDULE POLICY",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB0BEC5)
                    )
                    Text(
                        text = "${rotationStatus.rotationIntervalDays} Days (${rotationStatus.daysRemaining} remaining)",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (rotationStatus.isDueForRotation) Color(0xFFFF8A80) else Color(0xFF00E676)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rotate Key Action Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${rotationStatus.totalRotationsPerformed} Total Re-keyings",
                        fontSize = 10.sp,
                        color = Color(0xFFCFD8DC)
                    )
                }

                Button(
                    onClick = {
                        isRotating = true
                        scope.launch {
                            val res = DatabaseKeyRotationService.rotateDatabaseKey(context)
                            isRotating = false
                            refreshStatus()
                            val msg = if (res.isSuccess) {
                                "Key rotated successfully to v${res.newVersion}!"
                            } else {
                                "Key rotation failed: ${res.message}"
                            }
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    enabled = !isRotating,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF80D8FF),
                        contentColor = Color(0xFF003847)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("rotate_db_key_now_button")
                ) {
                    if (isRotating) {
                        CircularProgressIndicator(
                            color = Color(0xFF003847),
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Re-encrypting...", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Autorenew,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "Rotate Key Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
