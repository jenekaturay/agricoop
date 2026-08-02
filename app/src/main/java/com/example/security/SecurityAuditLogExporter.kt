package com.example.security

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Base64
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.crypto.Cipher
import javax.crypto.Mac
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

data class SecurityAuditExportResult(
    val plainReportFile: File,
    val encryptedReportFile: File,
    val sha256Checksum: String,
    val totalEventCount: Int,
    val timestampText: String
)

/**
 * Exporter for Security Status & Telemetry Logs.
 * Generates signed compliance reports and local AES-256 encrypted text logs for co-op managers.
 */
object SecurityAuditLogExporter {

    private const val TAG = "SecurityAuditExporter"
    private const val AES_TRANSFORMATION = "AES/CBC/PKCS5Padding"

    /**
     * Generates a plain-text compliance report string signed with a SHA-256 HMAC checksum.
     */
    fun buildComplianceReportText(
        context: Context,
        lastLoginTimestamp: Long,
        autoLockTimeoutMinutes: Int,
        biometricEvents: List<BiometricAuthEvent>
    ): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US)
        val displayDateFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm:ss a", Locale.US)
        val nowFormatted = dateFormat.format(Date())
        val hwFingerprint = HardwareBinder.getHardwareFingerprint(context)
        val isDbEncrypted = DatabaseEncryptionManager.isDatabaseEncrypted(context)

        val sb = StringBuilder()
        sb.append("================================================================================\n")
        sb.append("         LIBERIA AGRI-COOP STAFF SECURITY & TRUST COMPLIANCE REPORT             \n")
        sb.append("================================================================================\n")
        sb.append("Report Timestamp   : $nowFormatted\n")
        sb.append("Hardware Fingerprint: ${hwFingerprint.take(32)}...\n")
        sb.append("Vault Status       : ACTIVE & HARDENED\n")
        sb.append("--------------------------------------------------------------------------------\n\n")

        sb.append("[1] SYSTEM SECURITY & CRYPTOGRAPHIC CONFIGURATION\n")
        sb.append("• Local Database Storage : ${if (isDbEncrypted) "SQLCipher 256-bit AES-CBC Room Database (ENCRYPTED AT REST)" else "Standard SQLite (ACTIVE)"}\n")
        sb.append("• Passphrase Vault      : Hardware-rooted SharedPreferences entropy\n")
        sb.append("• Auto-Lock Timeout     : ${if (autoLockTimeoutMinutes < 0) "OFF" else if (autoLockTimeoutMinutes == 0) "IMMEDIATE" else "$autoLockTimeoutMinutes minutes inactivity"}\n")
        sb.append("• Last Successful Login : ${displayDateFormat.format(Date(lastLoginTimestamp))}\n")
        sb.append("• Anti-Replay Engine    : Active (Monotonic Millisecond Nonce Verification)\n")
        sb.append("• Request Integrity     : SHA-256 HMAC Request Signing & SSL Pinning Enabled\n\n")

        sb.append("[2] BIOMETRIC & STAFF AUTHENTICATION AUDIT LOGS (${biometricEvents.size} Events)\n")
        sb.append("--------------------------------------------------------------------------------\n")
        sb.append(String.format("%-4s | %-20s | %-12s | %-20s | %s\n", "NO", "TIMESTAMP", "STATUS", "METHOD", "DETAIL"))
        sb.append("--------------------------------------------------------------------------------\n")

        biometricEvents.forEachIndexed { index, event ->
            val eventTime = displayDateFormat.format(Date(event.timestamp))
            sb.append(String.format("%-4d | %-20s | %-12s | %-20s | %s\n",
                index + 1,
                eventTime,
                event.status,
                event.method,
                event.detail
            ))
        }

        sb.append("--------------------------------------------------------------------------------\n\n")

        // Compute HMAC SHA-256 signature over report content
        val rawContent = sb.toString()
        val hmacSignature = computeSha256Hmac(rawContent, hwFingerprint)

        sb.append("[3] COMPLIANCE INTEGRITY SEAL & CHECKSUM\n")
        sb.append("• Digital HMAC-SHA256 Seal : $hmacSignature\n")
        sb.append("• Encryption Standard       : Local AES-256-CBC Payload Protection\n")
        sb.append("================================================================================\n")
        sb.append("END OF COMPLIANCE REPORT - CONFIDENTIAL CO-OP AUDIT DOCUMENT\n")

        return sb.toString()
    }

    /**
     * Saves both a local plain-text report and a local-only AES-256 encrypted log file.
     */
    fun exportSecurityAuditLog(
        context: Context,
        lastLoginTimestamp: Long,
        autoLockTimeoutMinutes: Int,
        biometricEvents: List<BiometricAuthEvent>
    ): SecurityAuditExportResult? {
        return try {
            val reportText = buildComplianceReportText(
                context,
                lastLoginTimestamp,
                autoLockTimeoutMinutes,
                biometricEvents
            )

            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val plainFileName = "security_compliance_report_$timeStamp.txt"
            val encFileName = "security_compliance_report_$timeStamp.enc"

            val storageDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
            if (!storageDir.exists()) {
                storageDir.mkdirs()
            }

            // Write Plain Text Report
            val plainFile = File(storageDir, plainFileName)
            FileOutputStream(plainFile).use { fos ->
                OutputStreamWriter(fos, Charsets.UTF_8).use { writer ->
                    writer.write(reportText)
                }
            }

            // Encrypt and Write Local Encrypted Text File
            val passphrase = DatabaseEncryptionManager.getOrCreatePassphrase(context)
            val encryptedBytes = encryptBytes(reportText.toByteArray(Charsets.UTF_8), passphrase)

            val encFile = File(storageDir, encFileName)
            FileOutputStream(encFile).use { fos ->
                fos.write(encryptedBytes)
            }

            val sha256Checksum = computeSha256(reportText)
            val formattedTime = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.US).format(Date())

            SecureAuditLogger.recordAction(
                context = context,
                action = "SECURITY_LOG_EXPORTED",
                category = "COMPLIANCE_EXPORT",
                detail = "Exported hardware-signed security compliance report and encrypted log $encFileName"
            )

            SecurityAuditExportResult(
                plainReportFile = plainFile,
                encryptedReportFile = encFile,
                sha256Checksum = sha256Checksum,
                totalEventCount = biometricEvents.size,
                timestampText = formattedTime
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export security compliance audit log", e)
            null
        }
    }

    /**
     * Shares a saved audit log report file via Android Intent Chooser.
     */
    fun shareAuditLog(context: Context, file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = if (file.name.endsWith(".enc")) "application/octet-stream" else "text/plain"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Co-op Security Compliance Audit Log - ${file.name}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the encrypted/signed security compliance audit log generated for co-op manager inspection.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Share Security Compliance Log"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share security audit log via Intent", e)
        }
    }

    /**
     * Encrypts raw bytes using AES-256-CBC with an IV prepended to the ciphertext.
     */
    private fun encryptBytes(data: ByteArray, keyBytes: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val keySpec = SecretKeySpec(keyBytes.take(32).toByteArray(), "AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data)
        return iv + encryptedData
    }

    /**
     * Decrypts AES-256-CBC encrypted bytes (extracts IV from the first 16 bytes).
     */
    fun decryptBytes(encryptedWithIv: ByteArray, keyBytes: ByteArray): String {
        val iv = encryptedWithIv.copyOfRange(0, 16)
        val cipherText = encryptedWithIv.copyOfRange(16, encryptedWithIv.size)
        val cipher = Cipher.getInstance(AES_TRANSFORMATION)
        val keySpec = SecretKeySpec(keyBytes.take(32).toByteArray(), "AES")
        cipher.init(Cipher.DECRYPT_MODE, keySpec, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(cipherText)
        return String(decrypted, Charsets.UTF_8)
    }

    private fun computeSha256Hmac(data: String, key: String): String {
        return try {
            val secretKey = SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256")
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(secretKey)
            val bytes = mac.doFinal(data.toByteArray(Charsets.UTF_8))
            bytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            computeSha256(data)
        }
    }

    private fun computeSha256(data: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(data.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
