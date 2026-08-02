package com.example.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class NfcScanLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long,
    val farmerId: String,
    val farmerName: String,
    val tagUid: String,
    val cooperativeName: String = "Ganta District Farmers Co-op",
    val location: String = "Ganta Processing Hub",
    val scanMode: String = "NFC_TAP",
    val status: String = "VERIFIED",
    val notes: String = "Card verified • Ready for batch weighing"
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy • HH:mm:ss", Locale.US)
            return sdf.format(Date(timestamp))
        }
}

object NfcScanLogService {
    private val now = System.currentTimeMillis()
    private val oneMin = 60 * 1000L
    private val oneHour = 60 * oneMin

    private val initialLogs = listOf(
        NfcScanLogEntry(
            id = "nfc-log-101",
            timestamp = now - (3 * oneMin),
            farmerId = "farm-001",
            farmerName = "Flomo K. Kollie",
            tagUid = "04:A2:8F:C1",
            cooperativeName = "Ganta District Farmers Co-op",
            location = "Ganta Processing Hub",
            scanMode = "NFC_HARDWARE_TAP",
            status = "VERIFIED",
            notes = "Tap verified at scale station 1"
        ),
        NfcScanLogEntry(
            id = "nfc-log-102",
            timestamp = now - (22 * oneMin),
            farmerId = "farm-002",
            farmerName = "Sia Mary Tamba",
            tagUid = "04:B5:12:F8",
            cooperativeName = "Foya Women Cassava Producers",
            location = "Foya Mobile Weighing Unit",
            scanMode = "NFC_TAP",
            status = "VERIFIED",
            notes = "High-starch cassava farmer tap confirmed"
        ),
        NfcScanLogEntry(
            id = "nfc-log-103",
            timestamp = now - (48 * oneMin),
            farmerId = "farm-003",
            farmerName = "Emmanuel D. Saye",
            tagUid = "04:C8:3E:99",
            cooperativeName = "Sanniquellie Union Co-op",
            location = "Sanniquellie Collection Station",
            scanMode = "SIMULATED_TAG",
            status = "VERIFIED",
            notes = "Youth farmer ID tag tap"
        ),
        NfcScanLogEntry(
            id = "nfc-log-104",
            timestamp = now - (2 * oneHour),
            farmerId = "farm-004",
            farmerName = "Korto Agnes Flomo",
            tagUid = "04:D1:55:0A",
            cooperativeName = "Zorzor Agricultural Co-op",
            location = "Zorzor Hub Station",
            scanMode = "NFC_HARDWARE_TAP",
            status = "VERIFIED",
            notes = "Yam roots batch identification"
        ),
        NfcScanLogEntry(
            id = "nfc-log-105",
            timestamp = now - (4 * oneHour),
            farmerId = "farm-005",
            farmerName = "Josephine B. Dahnsaw",
            tagUid = "04:E9:21:4B",
            cooperativeName = "Bain-Garr Smallholders",
            location = "Ganta Hub Scale B",
            scanMode = "MANUAL_UID",
            status = "VERIFIED",
            notes = "Manual UID lookup fallback verified"
        )
    )

    private val _scanLogs = MutableStateFlow<List<NfcScanLogEntry>>(initialLogs)
    val scanLogs: StateFlow<List<NfcScanLogEntry>> = _scanLogs.asStateFlow()

    fun logScanEvent(
        farmerId: String,
        farmerName: String,
        tagUid: String,
        cooperativeName: String = "Ganta District Farmers Co-op",
        location: String = "Ganta Processing Hub",
        scanMode: String = "NFC_TAP",
        status: String = "VERIFIED",
        notes: String = "Card tap verified for produce batch weighing"
    ) {
        val newEntry = NfcScanLogEntry(
            timestamp = System.currentTimeMillis(),
            farmerId = farmerId,
            farmerName = farmerName,
            tagUid = tagUid,
            cooperativeName = cooperativeName,
            location = location,
            scanMode = scanMode,
            status = status,
            notes = notes
        )
        _scanLogs.value = listOf(newEntry) + _scanLogs.value
    }

    fun clearHistory() {
        _scanLogs.value = emptyList()
    }
}
