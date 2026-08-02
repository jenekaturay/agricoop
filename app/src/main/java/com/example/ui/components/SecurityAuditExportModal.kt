package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.db.entities.AuditLogEntity
import com.example.security.GlobalSessionManager
import com.example.security.SecureAuditLogger
import com.example.security.SecurityAuditExportResult
import com.example.security.SecurityAuditLogExporter
import java.io.File

/**
 * Co-op Manager Security Audit & Compliance Log Export Modal.
 * Enables exporting hardware-bound, AES-256 encrypted & SHA-256 signed
 * security status logs to local storage and offline sharing channels.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityAuditExportModal(
    onDismissRequest: () -> Unit
) {
    val context = LocalContext.current
    val lastLoginTimestamp by GlobalSessionManager.lastLoginTimestamp.collectAsStateWithLifecycle()
    val biometricEvents by GlobalSessionManager.biometricEvents.collectAsStateWithLifecycle()
    val autoLockTimeoutMinutes by GlobalSessionManager.autoLockTimeoutMinutes.collectAsStateWithLifecycle()

    val auditLogsFlow = remember(context) { SecureAuditLogger.getAllAuditLogsFlow(context) }
    val auditLogs by auditLogsFlow.collectAsStateWithLifecycle(initialValue = emptyList())

    var activeTab by remember { mutableStateOf(0) } // 0: Encrypted Audit Table, 1: Export Report
    var exportResult by remember { mutableStateOf<SecurityAuditExportResult?>(null) }
    var selectedFileFormat by remember { mutableStateOf("PLAIN_SIGNED") } // "PLAIN_SIGNED" vs "AES_ENCRYPTED"
    var isExporting by remember { mutableStateOf(false) }

    val previewText = remember(lastLoginTimestamp, autoLockTimeoutMinutes, biometricEvents) {
        SecurityAuditLogExporter.buildComplianceReportText(
            context,
            lastLoginTimestamp,
            autoLockTimeoutMinutes,
            biometricEvents
        )
    }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .testTag("security_audit_export_modal"),
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF09291E),
            border = BorderStroke(1.dp, Color(0xFF00E676))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFF1B5E20),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Export Security Compliance Log",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "Local-Only Encrypted Audit Telemetry",
                                fontSize = 11.sp,
                                color = Color(0xFF81C784)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.testTag("close_export_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Modal",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Switcher Bar
                TabRow(
                    selectedTabIndex = activeTab,
                    containerColor = Color(0xFF113D2D),
                    contentColor = Color(0xFF00E676),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = activeTab == 0,
                        onClick = { activeTab = 0 },
                        text = {
                            Text(
                                "🔒 Encrypted DB Table (${auditLogs.size})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == 0) Color(0xFF00E676) else Color.White
                            )
                        },
                        modifier = Modifier.testTag("audit_tab_table")
                    )
                    Tab(
                        selected = activeTab == 1,
                        onClick = { activeTab = 1 },
                        text = {
                            Text(
                                "📄 Export Audit File",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (activeTab == 1) Color(0xFF00E676) else Color.White
                            )
                        },
                        modifier = Modifier.testTag("audit_tab_export")
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeTab == 0) {
                    EncryptedAuditLogTableViewer(
                        auditLogs = auditLogs,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Top
                    ) {
                        // Format Selector Card
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF113D2D)),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF00E676).copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "1. Select Compliance Export Format",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedFileFormat == "PLAIN_SIGNED",
                                onClick = { selectedFileFormat = "PLAIN_SIGNED" },
                                label = {
                                    Text(
                                        text = "📄 Signed Report (.txt)",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedFileFormat == "PLAIN_SIGNED") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedFileFormat == "PLAIN_SIGNED") Color(0xFF0B2B20) else Color.White
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E676),
                                    containerColor = Color(0xFF1B4D3E)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("format_plain_chip")
                            )

                            FilterChip(
                                selected = selectedFileFormat == "AES_ENCRYPTED",
                                onClick = { selectedFileFormat = "AES_ENCRYPTED" },
                                label = {
                                    Text(
                                        text = "🔒 Encrypted Log (.enc)",
                                        fontSize = 11.sp,
                                        fontWeight = if (selectedFileFormat == "AES_ENCRYPTED") FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedFileFormat == "AES_ENCRYPTED") Color(0xFF0B2B20) else Color.White
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF00E676),
                                    containerColor = Color(0xFF1B4D3E)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("format_encrypted_chip")
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (selectedFileFormat == "PLAIN_SIGNED")
                                "Includes full human-readable audit logs signed with a SHA-256 HMAC digital seal."
                            else
                                "Encrypted locally with AES-256-CBC using the co-op hardware vault passphrase.",
                            fontSize = 10.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Report Preview Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "2. Compliance Report Preview",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Security Audit Log", previewText))
                            Toast.makeText(context, "Report text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("copy_report_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy Report Text",
                            tint = Color(0xFF80D8FF),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Scrollable Monospaced Log Preview Container
                Surface(
                    color = Color(0xFF051B13),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1B5E20)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    Box(modifier = Modifier.padding(12.dp)) {
                        SelectionContainer {
                            Text(
                                text = previewText,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color(0xFF69F0AE),
                                lineHeight = 14.sp,
                                modifier = Modifier.verticalScroll(rememberScrollState())
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Export Results Confirmation Box (if exported)
                exportResult?.let { result ->
                    Surface(
                        color = Color(0xFF134835),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color(0xFF00E676)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF00E676),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Log Export Saved Successfully!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "File: ${if (selectedFileFormat == "AES_ENCRYPTED") result.encryptedReportFile.name else result.plainReportFile.name}",
                                fontSize = 10.sp,
                                color = Color(0xFFB0BEC5)
                            )
                            Text(
                                text = "Path: ${result.plainReportFile.parent}",
                                fontSize = 9.sp,
                                color = Color(0xFF80CBC4)
                            )
                            Text(
                                text = "SHA-256 Digest: ${result.sha256Checksum.take(24)}...",
                                fontSize = 9.sp,
                                color = Color(0xFFFFD54F)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Action Buttons Row: Save Local File & Share Intent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            isExporting = true
                            val res = SecurityAuditLogExporter.exportSecurityAuditLog(
                                context = context,
                                lastLoginTimestamp = lastLoginTimestamp,
                                autoLockTimeoutMinutes = autoLockTimeoutMinutes,
                                biometricEvents = biometricEvents
                            )
                            isExporting = false
                            if (res != null) {
                                exportResult = res
                                Toast.makeText(context, "Saved to Documents: ${res.plainReportFile.name}", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed to export audit log file.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        border = BorderStroke(1.dp, Color(0xFF00E676)),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF00E676)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("save_local_file_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isExporting) "Saving..." else "Save Local File",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            val targetResult = exportResult ?: SecurityAuditLogExporter.exportSecurityAuditLog(
                                context = context,
                                lastLoginTimestamp = lastLoginTimestamp,
                                autoLockTimeoutMinutes = autoLockTimeoutMinutes,
                                biometricEvents = biometricEvents
                            ).also { exportResult = it }

                            if (targetResult != null) {
                                val targetFile = if (selectedFileFormat == "AES_ENCRYPTED") {
                                    targetResult.encryptedReportFile
                                } else {
                                    targetResult.plainReportFile
                                }
                                SecurityAuditLogExporter.shareAuditLog(context, targetFile)
                            } else {
                                Toast.makeText(context, "Could not generate file to share.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF00E676),
                            contentColor = Color(0xFF07241A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("share_compliance_log_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Share Report",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
}
}
