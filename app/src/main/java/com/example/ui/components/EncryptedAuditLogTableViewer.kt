package com.example.ui.components

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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.entities.AuditLogEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Encrypted Audit Log Table Viewer for Internal Security Reviews.
 * Renders all captured sensitive system actions (batch deletions, database exports,
 * financial dispatches, biometric verifications) stored in the SQLCipher 256-bit AES Room database.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncryptedAuditLogTableViewer(
    auditLogs: List<AuditLogEntity>,
    modifier: Modifier = Modifier
) {
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }

    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm:ss", Locale.US) }

    val filteredLogs = remember(auditLogs, selectedCategoryFilter) {
        if (selectedCategoryFilter == "ALL") {
            auditLogs
        } else {
            auditLogs.filter { it.category == selectedCategoryFilter }
        }
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0B2219)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.6f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = Color(0xFF00E676),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Internal Audit Log Vault",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Room Table: `audit_logs` (SQLCipher AES-256)",
                            fontSize = 10.sp,
                            color = Color(0xFF81C784),
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }

                Surface(
                    color = Color(0xFF1B5E20),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(0.5.dp, Color(0xFF00E676))
                ) {
                    Text(
                        text = "${filteredLogs.size} Events",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00E676),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Category Filter Chips Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    "ALL" to "All Logs",
                    "SENSITIVE_DATA_MUTATION" to "Deletions",
                    "COMPLIANCE_EXPORT" to "Exports",
                    "AUTHENTICATION" to "Biometric",
                    "FINANCIAL_TRANSACTION" to "Payouts"
                ).forEach { (catKey, label) ->
                    val isSelected = selectedCategoryFilter == catKey
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = catKey },
                        label = {
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF07241A) else Color.White
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF00E676),
                            containerColor = Color(0xFF123D2D)
                        ),
                        modifier = Modifier.testTag("audit_filter_$catKey")
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Log List Container
            if (filteredLogs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .background(Color(0xFF051B13), RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No audit log records found in encrypted table.",
                        fontSize = 11.sp,
                        color = Color(0xFF80CBC4)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filteredLogs, key = { it.id }) { item ->
                        AuditLogRowCard(log = item, formattedTime = dateFormat.format(Date(item.timestamp)))
                    }
                }
            }
        }
    }
}

@Composable
private fun AuditLogRowCard(
    log: AuditLogEntity,
    formattedTime: String
) {
    val (badgeBg, badgeFg, icon) = when (log.action) {
        "BATCH_DELETED" -> Triple(Color(0xFFFF2A2A), Color.White, Icons.Default.Delete)
        "DATABASE_EXPORTED", "SECURITY_LOG_EXPORTED" -> Triple(Color(0xFF0288D1), Color.White, Icons.Default.FileDownload)
        "MOMO_PAYOUT_INITIATED" -> Triple(Color(0xFFFFB300), Color(0xFF212121), Icons.Default.Payments)
        "BIOMETRIC_AUTH_VERIFIED" -> Triple(Color(0xFF00E676), Color(0xFF07241A), Icons.Default.Fingerprint)
        else -> Triple(Color(0xFF7E57C2), Color.White, Icons.Default.Shield)
    }

    Surface(
        color = Color(0xFF051B13),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(0.5.dp, Color(0xFF1B5E20)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("audit_log_item_${log.id}")
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Action Badge
                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = badgeFg,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = log.action,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = badgeFg
                        )
                    }
                }

                // Timestamp
                Text(
                    text = formattedTime,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF80D8FF)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Detail Text
            Text(
                text = log.detail,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(4.dp))

            // User & Device Metadata Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "User: ${log.userId}",
                        fontSize = 9.sp,
                        color = Color(0xFFB0BEC5)
                    )
                }

                if (log.deviceFingerprint.isNotEmpty()) {
                    Text(
                        text = "HW: ${log.deviceFingerprint.take(12)}...",
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace,
                        color = Color(0xFF80CBC4)
                    )
                }
            }
        }
    }
}
