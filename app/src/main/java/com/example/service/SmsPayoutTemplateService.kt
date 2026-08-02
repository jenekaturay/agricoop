package com.example.service

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.data.db.entities.FarmerEntity
import com.example.data.db.entities.ProduceBatchEntity

enum class MobileMoneyCarrier(val displayName: String, val shortCode: String, val brandColorHex: Long) {
    ORANGE_MONEY("Orange Money Liberia", "*144#", 0xFFE65100),
    MTN_MOMO("Lonestar MTN MoMo", "*156#", 0xFF1565C0)
}

data class UssdPayoutTemplate(
    val carrier: MobileMoneyCarrier,
    val ussdDialCode: String,        // e.g. *144*2*1*0770123456*35700#
    val displayString: String,       // Human friendly summary
    val recipientNumber: String,
    val amountLrd: Double,
    val batchCode: String,
    val farmerName: String
)

data class SmsPayoutTemplate(
    val shortCode: String,           // e.g. "+231770001122" or "1122"
    val smsBody: String,             // Formatted offline payload: "PAYOUT|BATCH-NIM-2026-001|0770123456|35700|CASSAVA|450|78291"
    val uriString: String,           // smsto:+231770001122?body=...
    val batchCode: String,
    val farmerName: String,
    val amountLrd: Double,
    val securityChecksum: String
)

object SmsPayoutTemplateService {

    const val DEFAULT_GATEWAY_SHORTCODE = "+231770001122"

    /**
     * Formats a ProduceBatchEntity + optional FarmerEntity into a USSD dialing template for Orange Money or MTN MoMo.
     */
    fun formatUssdTemplate(
        batch: ProduceBatchEntity,
        farmer: FarmerEntity? = null,
        carrier: MobileMoneyCarrier = MobileMoneyCarrier.ORANGE_MONEY
    ): UssdPayoutTemplate {
        val recipientPhone = farmer?.momoNumber?.ifEmpty { "0770001122" } ?: "0770001122"
        val cleanPhone = recipientPhone.replace(Regex("[^0-9]"), "")
        val amountInt = batch.totalPayoutLrd.toInt().coerceAtLeast(1)

        val ussdCode = when (carrier) {
            MobileMoneyCarrier.ORANGE_MONEY -> "*144*2*1*$cleanPhone*$amountInt#"
            MobileMoneyCarrier.MTN_MOMO -> "*156*1*1*$cleanPhone*$amountInt#"
        }

        val display = "${carrier.displayName}: Dial $ussdCode to transfer LRD $amountInt to ${farmer?.fullName ?: batch.farmerName}"

        return UssdPayoutTemplate(
            carrier = carrier,
            ussdDialCode = ussdCode,
            displayString = display,
            recipientNumber = cleanPhone,
            amountLrd = batch.totalPayoutLrd,
            batchCode = batch.batchCode,
            farmerName = farmer?.fullName ?: batch.farmerName
        )
    }

    /**
     * Formats a ProduceBatchEntity into a structured SMS payload for offline processing.
     */
    fun formatSmsTemplate(
        batch: ProduceBatchEntity,
        farmer: FarmerEntity? = null,
        gatewayShortCode: String = DEFAULT_GATEWAY_SHORTCODE
    ): SmsPayoutTemplate {
        val recipientPhone = farmer?.momoNumber?.ifEmpty { "0770001122" } ?: "0770001122"
        val checksum = generateChecksum(batch.batchCode, batch.totalPayoutLrd)

        val smsBody = "PAYOUT|${batch.batchCode}|${batch.farmerId}|$recipientPhone|${batch.totalPayoutLrd.toInt()}|${batch.cropType}|${batch.weightKg.toInt()}|$checksum"
        val encodedBody = Uri.encode(smsBody)
        val uriString = "smsto:$gatewayShortCode?body=$encodedBody"

        return SmsPayoutTemplate(
            shortCode = gatewayShortCode,
            smsBody = smsBody,
            uriString = uriString,
            batchCode = batch.batchCode,
            farmerName = farmer?.fullName ?: batch.farmerName,
            amountLrd = batch.totalPayoutLrd,
            securityChecksum = checksum
        )
    }

    /**
     * Multi-batch bulk payout SMS format for low-bandwidth offline submission
     */
    fun formatBulkBatchSms(
        batches: List<ProduceBatchEntity>,
        gatewayShortCode: String = DEFAULT_GATEWAY_SHORTCODE
    ): SmsPayoutTemplate {
        val totalPayout = batches.sumOf { it.totalPayoutLrd }
        val batchCodes = batches.joinToString(",") { it.batchCode }
        val checksum = generateChecksum(batchCodes, totalPayout)

        val smsBody = "BULK_PAYOUT|COUNT:${batches.size}|TOTAL:${totalPayout.toInt()}|$batchCodes|$checksum"
        val encodedBody = Uri.encode(smsBody)
        val uriString = "smsto:$gatewayShortCode?body=$encodedBody"

        return SmsPayoutTemplate(
            shortCode = gatewayShortCode,
            smsBody = smsBody,
            uriString = uriString,
            batchCode = "BULK_${batches.size}_BATCHES",
            farmerName = "Multiple Farmers (${batches.size})",
            amountLrd = totalPayout,
            securityChecksum = checksum
        )
    }

    /**
     * Generates an Android Intent to launch the phone dialer with pre-filled USSD string.
     */
    fun createUssdDialIntent(ussdCode: String): Intent {
        val encodedCode = Uri.encode(ussdCode)
        return Intent(Intent.ACTION_DIAL, Uri.parse("tel:$encodedCode"))
    }

    /**
     * Generates an Android Intent to launch the SMS app with pre-filled recipient and message body.
     */
    fun createSmsIntent(template: SmsPayoutTemplate): Intent {
        return Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:${template.shortCode}")
            putExtra("sms_body", template.smsBody)
        }
    }

    private fun generateChecksum(seed: String, amount: Double): String {
        val raw = "$seed:$amount:LIBERIA_AGRI_KEY"
        return raw.hashCode().toString().takeLast(6).replace("-", "7")
    }
}
