package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.db.entities.ProduceBatchEntity
import com.example.service.SmsPayoutTemplateService
import com.example.ui.components.UssdSmsPayoutModal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import com.example.service.TextToSpeechHelper

@Composable
fun ThermalReceiptDialog(
    batch: ProduceBatchEntity,
    onDismiss: () -> Unit,
    onMoMoTrigger: () -> Unit
) {
    val context = LocalContext.current
    val ttsHelper = remember(context) { TextToSpeechHelper(context) }

    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
    val dateStr = sdf.format(Date(batch.timestamp))
    var showUssdModal by remember { mutableStateOf(false) }

    val ussdTemplate = remember(batch) {
        SmsPayoutTemplateService.formatUssdTemplate(batch)
    }
    val smsTemplate = remember(batch) {
        SmsPayoutTemplateService.formatSmsTemplate(batch)
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Thermal Weighing Slip",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0B3D2E)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("receipt_close_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Receipt paper simulation card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFAFAFA)),
                    border = CardDefaults.outlinedCardBorder(),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "AGRICOOP LIBERIA VENTURE",
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.Black
                        )
                        Text(
                            text = "Lofa & Nimba Processing Hubs",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "OFFICIAL FARM-GATE WEIGHING SLIP",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace,
                            color = Color(0xFF0B3D2E)
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray
                        )

                        // QR Code
                        QrCodeView(data = batch.batchCode, size = 120.dp)

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = batch.batchCode,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray
                        )

                        // Data rows
                        ReceiptRow("Date/Time:", dateStr)
                        ReceiptRow("Farmer:", batch.farmerName)
                        ReceiptRow("Cooperative:", batch.cooperativeName)
                        ReceiptRow("Crop Sourced:", batch.cropType)
                        ReceiptRow("Gross Weight:", "%.1f kg".format(batch.weightKg))
                        ReceiptRow("Starch Content:", "%.1f %%".format(batch.starchPercentage))
                        ReceiptRow("Moisture:", "%.1f %%".format(batch.moisturePercentage))
                        ReceiptRow("Price/kg (LRD):", "$ %.2f".format(batch.pricePerKgLrd))

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.Black
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "TOTAL PAYOUT:",
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = "LRD $%.2f".format(batch.totalPayoutLrd),
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF0B3D2E),
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        ReceiptRow("Payout Status:", batch.payoutStatus)
                        ReceiptRow("MoMo Ref:", batch.momoTransactionRef)
                        ReceiptRow("Location:", batch.locationName)
                        ReceiptRow("USSD Code:", ussdTemplate.ussdDialCode)

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "[✓] Bluetooth IP67 Scale Verified\nDual Signatory Encrypted Ledger\nUSSD Fallback Code: ${ussdTemplate.ussdDialCode}",
                            fontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            color = Color.Gray,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action buttons
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Auditory Confirmation Read Aloud Button for Literacy Support
                    OutlinedButton(
                        onClick = {
                            ttsHelper.speakReceiptSummary(
                                batchCode = batch.batchCode,
                                farmerName = batch.farmerName,
                                cropType = batch.cropType,
                                weightKg = batch.weightKg,
                                starchPct = batch.starchPercentage,
                                payoutLrd = batch.totalPayoutLrd
                            )
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0D47A1)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("read_receipt_aloud_tts_button")
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Read Aloud", modifier = Modifier.padding(end = 6.dp))
                        Text("Read Aloud (Voice Confirmation)", fontWeight = FontWeight.Bold)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { /* Simulated print */ },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("print_thermal_receipt_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Print Slip")
                        }

                        Button(
                            onClick = { showUssdModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0B3D2E)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_ussd_sms_modal_button")
                        ) {
                            Text("USSD / SMS Payout", color = Color(0xFFFFD54F), fontWeight = FontWeight.Bold)
                        }
                    }

                    if (batch.payoutStatus == "PENDING") {
                        Button(
                            onClick = onMoMoTrigger,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE5A93C)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("trigger_momo_payout_button")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.padding(end = 4.dp))
                            Text("Dispatch Instant MoMo Payment", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (showUssdModal) {
                    UssdSmsPayoutModal(
                        batch = batch,
                        onDismiss = { showUssdModal = false },
                        onPayoutTriggered = {
                            onMoMoTrigger()
                            showUssdModal = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ReceiptRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = Color.DarkGray)
        Text(text = value, fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.SemiBold)
    }
}
