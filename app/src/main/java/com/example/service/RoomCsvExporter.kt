package com.example.service

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import androidx.core.content.FileProvider
import com.example.data.db.entities.CooperativeEntity
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.HubOperationEntity
import com.example.data.db.entities.MoMoFloatEntity
import com.example.data.db.entities.ProduceBatchEntity
import com.example.security.SecureAuditLogger
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class CsvExportResult(
    val exportFile: File,
    val recordCount: Int,
    val timestampText: String
)

object RoomCsvExporter {

    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val stringVal = value.toString()
        return if (stringVal.contains(",") || stringVal.contains("\"") || stringVal.contains("\n")) {
            "\"" + stringVal.replace("\"", "\"\"") + "\""
        } else {
            stringVal
        }
    }

    fun exportProduceBatchesToCsv(batches: List<ProduceBatchEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Batch Code,Farmer ID,Farmer Name,Cooperative Name,Crop Type,Weight (kg),Starch (%),Moisture (%),Price/kg (LRD),Total Payout (LRD),Payout Status,MoMo Ref,Location,Synced,Timestamp\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (b in batches) {
            val timeStr = dateFormat.format(Date(b.timestamp))
            sb.append("${escapeCsv(b.id)},")
                .append("${escapeCsv(b.batchCode)},")
                .append("${escapeCsv(b.farmerId)},")
                .append("${escapeCsv(b.farmerName)},")
                .append("${escapeCsv(b.cooperativeName)},")
                .append("${escapeCsv(b.cropType)},")
                .append("${b.weightKg},")
                .append("${b.starchPercentage},")
                .append("${b.moisturePercentage},")
                .append("${b.pricePerKgLrd},")
                .append("${b.totalPayoutLrd},")
                .append("${escapeCsv(b.payoutStatus)},")
                .append("${escapeCsv(b.momoTransactionRef)},")
                .append("${escapeCsv(b.locationName)},")
                .append("${b.isSynced},")
                .append("${escapeCsv(timeStr)}\n")
        }
        return sb.toString()
    }

    /**
     * Generates a lightweight CSV formatted for offline sharing via Bluetooth or local transfer.
     */
    fun exportLightweightBatchCsv(batches: List<ProduceBatchEntity>, filterMode: String): String {
        val filtered = when (filterMode) {
            "UNSYNCED" -> batches.filter { !it.isSynced }
            "RECENT" -> batches.sortedByDescending { it.timestamp }.take(50)
            else -> batches
        }
        val sb = StringBuilder()
        val formattedTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        val modeLabel = when (filterMode) {
            "UNSYNCED" -> "UNSYNCED RECORDS ONLY"
            "RECENT" -> "RECENT 50 RECORDS"
            else -> "ALL BATCH HISTORY"
        }
        sb.append("# LIBERIA SMALLHOLDER AGRI-COOP BATCH EXPORT ($modeLabel)\n")
        sb.append("# Generated: $formattedTime | Record Count: ${filtered.size}\n")
        sb.append("Batch Code,Farmer Name,Farmer ID,Crop,Weight (kg),Payout (LRD),Status,Synced,Location,Timestamp\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
        for (b in filtered) {
            val timeStr = dateFormat.format(Date(b.timestamp))
            sb.append("${escapeCsv(b.batchCode)},")
                .append("${escapeCsv(b.farmerName)},")
                .append("${escapeCsv(b.farmerId)},")
                .append("${escapeCsv(b.cropType)},")
                .append("${b.weightKg},")
                .append("${b.totalPayoutLrd},")
                .append("${escapeCsv(b.payoutStatus)},")
                .append("${if (b.isSynced) "YES" else "NO_PENDING"},")
                .append("${escapeCsv(b.locationName)},")
                .append("${escapeCsv(timeStr)}\n")
        }
        return sb.toString()
    }

    fun exportFarmersToCsv(farmers: List<FarmerEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Cooperative ID,Cooperative Name,National ID,Full Name,Phone Number,MoMo Number,Gender,Year of Birth,Is Youth,TME 419 Cuttings,Total Batches,Total Earnings (LRD)\n")
        for (f in farmers) {
            sb.append("${escapeCsv(f.id)},")
                .append("${escapeCsv(f.cooperativeId)},")
                .append("${escapeCsv(f.cooperativeName)},")
                .append("${escapeCsv(f.nationalId)},")
                .append("${escapeCsv(f.fullName)},")
                .append("${escapeCsv(f.phoneNumber)},")
                .append("${escapeCsv(f.momoNumber)},")
                .append("${escapeCsv(f.gender)},")
                .append("${f.yearOfBirth},")
                .append("${f.isYouth},")
                .append("${f.seedCuttingsAllocated},")
                .append("${f.totalBatchesDelivered},")
                .append("${f.totalEarningsLrd}\n")
        }
        return sb.toString()
    }

    fun exportCooperativesToCsv(cooperatives: List<CooperativeEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Name,County,District,Lead Person,Phone,Member Count\n")
        for (c in cooperatives) {
            sb.append("${escapeCsv(c.id)},")
                .append("${escapeCsv(c.name)},")
                .append("${escapeCsv(c.county)},")
                .append("${escapeCsv(c.district)},")
                .append("${escapeCsv(c.leadPerson)},")
                .append("${escapeCsv(c.phone)},")
                .append("${c.memberCount}\n")
        }
        return sb.toString()
    }

    fun exportHubOperationsToCsv(hubs: List<HubOperationEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Hub Name,County,Solar Capacity (kW),Flash Dryer Active,Daily Raw Tons,HQCF Yield Tons,Starch Tons,Animal Feed Tons,Sifting Passed,Moisture (%),Active Trikes\n")
        for (h in hubs) {
            sb.append("${escapeCsv(h.id)},")
                .append("${escapeCsv(h.hubName)},")
                .append("${escapeCsv(h.county)},")
                .append("${h.solarCapacityKw},")
                .append("${h.flashDryerActive},")
                .append("${h.dailyRawTons},")
                .append("${h.hqcfYieldTons},")
                .append("${h.industrialStarchTons},")
                .append("${h.animalFeedTons},")
                .append("${h.siftingMeshPassed},")
                .append("${h.moistureContentPct},")
                .append("${h.activeCargoTrikes}\n")
        }
        return sb.toString()
    }

    fun exportMoMoFloatsToCsv(floats: List<MoMoFloatEntity>): String {
        val sb = StringBuilder()
        sb.append("ID,Hub Location,Orange MoMo Float (LRD),MTN MoMo Float (LRD),Is Sufficient Float,Last Refreshed Timestamp\n")
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
        for (m in floats) {
            val timeStr = dateFormat.format(Date(m.lastRefreshedTime))
            sb.append("${escapeCsv(m.id)},")
                .append("${escapeCsv(m.hubLocation)},")
                .append("${m.orangeMoMoFloatLrd},")
                .append("${m.mtnMoMoFloatLrd},")
                .append("${m.isSufficientFloat},")
                .append("${escapeCsv(timeStr)}\n")
        }
        return sb.toString()
    }

    fun exportFullMasterCsv(
        batches: List<ProduceBatchEntity>,
        farmers: List<FarmerEntity>,
        cooperatives: List<CooperativeEntity>,
        hubs: List<HubOperationEntity>,
        floats: List<MoMoFloatEntity>
    ): String {
        val sb = StringBuilder()
        val timeHeader = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        sb.append("# LIBERIA SMALLHOLDER AGRI-COOP ROOM DATABASE MASTER AUDIT EXPORT\n")
        sb.append("# Generated At: $timeHeader\n")
        sb.append("# Total Records: ${batches.size + farmers.size + cooperatives.size + hubs.size + floats.size}\n\n")

        sb.append("=== TABLE: PRODUCE BATCHES ===\n")
        sb.append(exportProduceBatchesToCsv(batches))
        sb.append("\n=== TABLE: FARMERS REGISTRY ===\n")
        sb.append(exportFarmersToCsv(farmers))
        sb.append("\n=== TABLE: COOPERATIVES ===\n")
        sb.append(exportCooperativesToCsv(cooperatives))
        sb.append("\n=== TABLE: HUB OPERATIONS ===\n")
        sb.append(exportHubOperationsToCsv(hubs))
        sb.append("\n=== TABLE: MOMO FLOAT BALANCES ===\n")
        sb.append(exportMoMoFloatsToCsv(floats))

        return sb.toString()
    }

    /**
     * Saves CSV string to a local file in app storage / Downloads and returns file details.
     */
    fun saveCsvToLocalDirectory(context: Context, csvContent: String, fileNamePrefix: String): CsvExportResult? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "${fileNamePrefix}_$timeStamp.csv"

            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            val file = File(storageDir, fileName)
            FileOutputStream(file).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                    writer.write(csvContent)
                }
            }

            val lineCount = csvContent.lines().count { it.isNotBlank() }
            val formattedTime = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())

            SecureAuditLogger.recordAction(
                context = context,
                action = "DATABASE_EXPORTED",
                category = "COMPLIANCE_EXPORT",
                detail = "Exported $lineCount records ($fileNamePrefix) to local CSV backup file: ${file.name}"
            )

            CsvExportResult(
                exportFile = file,
                recordCount = lineCount,
                timestampText = formattedTime
            )
        } catch (e: Exception) {
            Log.e("RoomCsvExporter", "Failed to save CSV file", e)
            null
        }
    }

    /**
     * Shares a saved CSV file via Android Intent chooser
     */
    fun shareCsvFile(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Room Database CSV Backup - ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "Attached CSV backup exported from local Room SQLite database.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Room DB CSV Backup"))
        } catch (e: Exception) {
            Log.e("RoomCsvExporter", "Failed to share CSV via Intent", e)
        }
    }
}
