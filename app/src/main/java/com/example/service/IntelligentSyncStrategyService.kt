package com.example.service

import com.example.data.db.entities.ProduceBatchEntity
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets

enum class BandwidthCondition(val label: String, val networkType: String, val maxPacketSizeKb: Int) {
    CELLULAR_LOW_BANDWIDTH("2G / EDGE / 3G Low-Bandwidth", "Cellular 2G/3G", 2),
    HIGH_BANDWIDTH_WIFI("Wi-Fi / 4G / 5G High-Bandwidth", "Wi-Fi/4G", 50)
}

enum class SyncStrategyType(val title: String, val description: String) {
    METADATA_PACKET_PRIORITY(
        "Low-Data Metadata Packet Priority",
        "Pushes ultra-compact financial & weighing metadata packets (~180B) to secure records immediately over weak cellular links."
    ),
    FULL_DATABASE_SNAPSHOT(
        "Full Database Snapshot",
        "Pushes complete Room SQLite records (~4.5KB) including extended hub telemetry, scale logs, and QR audit data."
    )
}

data class MetadataPacket(
    val batchId: String,
    val batchCode: String,
    val farmerId: String,
    val weightKg: Double,
    val payoutLrd: Double,
    val starchPct: Double,
    val timestamp: Long,
    val priority: String = "HIGH", // "HIGH" for pending payouts, "NORMAL" for standard
    val checksum: String
)

data class SyncPayloadMetrics(
    val strategyType: SyncStrategyType,
    val packetCount: Int,
    val totalTransmittedBytes: Int,
    val totalFullSnapshotBytes: Int,
    val bytesSaved: Int,
    val savingsPercentage: Int,
    val compressedPayloadPreview: String,
    val smsFallbackPreview: String
)

data class SyncLogEntry(
    val timestampText: String,
    val strategyUsed: SyncStrategyType,
    val recordCount: Int,
    val bytesSent: Int,
    val bytesSaved: Int,
    val statusText: String
)

object IntelligentSyncStrategyService {

    /**
     * Constructs ultra-lightweight metadata packets prioritizing core financial and weighing data.
     * Reduces payload size by ~95% compared to full DB snapshots.
     */
    fun createMetadataPackets(batches: List<ProduceBatchEntity>): List<MetadataPacket> {
        return batches.map { batch ->
            val hashSource = "${batch.id}-${batch.weightKg}-${batch.totalPayoutLrd}"
            val checksum = hashSource.hashCode().toString(16).takeLast(6)
            MetadataPacket(
                batchId = batch.id,
                batchCode = batch.batchCode,
                farmerId = batch.farmerId,
                weightKg = batch.weightKg,
                payoutLrd = batch.totalPayoutLrd,
                starchPct = batch.starchPercentage,
                timestamp = batch.timestamp,
                priority = if (batch.payoutStatus == "PENDING") "HIGH" else "NORMAL",
                checksum = checksum
            )
        }
    }

    /**
     * Calculates size metrics and generates payload previews for low-bandwidth vs full sync.
     */
    fun computeSyncPayloadMetrics(
        batches: List<ProduceBatchEntity>,
        strategy: SyncStrategyType
    ): SyncPayloadMetrics {
        if (batches.isEmpty()) {
            return SyncPayloadMetrics(
                strategyType = strategy,
                packetCount = 0,
                totalTransmittedBytes = 0,
                totalFullSnapshotBytes = 0,
                bytesSaved = 0,
                savingsPercentage = 0,
                compressedPayloadPreview = "{\"status\":\"idle\",\"pending_batches\":0}",
                smsFallbackPreview = "SMS://+231770001122?body=SYNC_IDLE"
            )
        }

        val metadataPackets = createMetadataPackets(batches)

        // Build compact metadata JSON payload
        val metadataJsonArray = JSONArray()
        metadataPackets.forEach { pkt ->
            val obj = JSONObject().apply {
                put("c", pkt.batchCode)
                put("f", pkt.farmerId)
                put("w", pkt.weightKg)
                put("p", pkt.payoutLrd)
                put("s", pkt.starchPct)
                put("h", pkt.checksum)
            }
            metadataJsonArray.put(obj)
        }
        val metadataJsonString = metadataJsonArray.toString()
        val metadataBytes = metadataJsonString.toByteArray(StandardCharsets.UTF_8).size

        // Build full snapshot JSON payload
        val fullJsonArray = JSONArray()
        batches.forEach { b ->
            val obj = JSONObject().apply {
                put("id", b.id)
                put("batchCode", b.batchCode)
                put("farmerId", b.farmerId)
                put("farmerName", b.farmerName)
                put("cooperativeName", b.cooperativeName)
                put("cropType", b.cropType)
                put("weightKg", b.weightKg)
                put("starchPercentage", b.starchPercentage)
                put("moisturePercentage", b.moisturePercentage)
                put("pricePerKgLrd", b.pricePerKgLrd)
                put("totalPayoutLrd", b.totalPayoutLrd)
                put("payoutStatus", b.payoutStatus)
                put("momoTransactionRef", b.momoTransactionRef)
                put("timestamp", b.timestamp)
                put("scaleDeviceSerial", "SCALE-BLUETOOTH-IP67-NIMBA-09")
                put("gpsCoordinates", "${b.latitude} N, ${b.longitude} W")
                put("operatorNotes", "Verified by hub weighmaster with bluetooth scale calibration zeroing log")
            }
            fullJsonArray.put(obj)
        }
        val fullJsonString = fullJsonArray.toString()
        val fullBytes = fullJsonString.toByteArray(StandardCharsets.UTF_8).size

        val transmittedBytes = if (strategy == SyncStrategyType.METADATA_PACKET_PRIORITY) metadataBytes else fullBytes
        val savedBytes = (fullBytes - metadataBytes).coerceAtLeast(0)
        val savingsPct = if (fullBytes > 0) ((savedBytes.toDouble() / fullBytes) * 100).toInt() else 0

        val smsString = metadataPackets.joinToString("|") {
            "${it.batchCode},${it.farmerId},${it.weightKg},${it.payoutLrd}"
        }

        return SyncPayloadMetrics(
            strategyType = strategy,
            packetCount = batches.size,
            totalTransmittedBytes = transmittedBytes,
            totalFullSnapshotBytes = fullBytes,
            bytesSaved = if (strategy == SyncStrategyType.METADATA_PACKET_PRIORITY) savedBytes else 0,
            savingsPercentage = if (strategy == SyncStrategyType.METADATA_PACKET_PRIORITY) savingsPct else 0,
            compressedPayloadPreview = if (strategy == SyncStrategyType.METADATA_PACKET_PRIORITY) metadataJsonString else fullJsonString.take(300) + "...",
            smsFallbackPreview = "SMS://+231770001122?body=" + smsString.take(120)
        )
    }
}
