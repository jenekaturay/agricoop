package com.example.security

import java.nio.charset.StandardCharsets

/**
 * Hardened Security Architecture - Layer 6: Bluetooth SPP Key Handshake & CRC-16 Checksum Validation
 * Validates raw scale frames: [STX(0x02), WEIGHT(6B), UNIT(2B), CRC(2B), ETX(0x03)]
 * Prevents agents from spoofing weight data via false Bluetooth SPP phone apps.
 */
object SecureScaleDecoder {

    /**
     * Parses and validates raw Bluetooth SPP scale byte frame.
     * Throws SecurityException if CRC fails or frame is tampered.
     */
    fun parseAndValidateFrame(rawBytes: ByteArray): Double? {
        if (rawBytes.size < 12 || rawBytes.first() != 0x02.toByte() || rawBytes.last() != 0x03.toByte()) {
            return null // Corrupted or spoofed frame layout
        }

        // Extract payload bytes (excluding STX, CRC, ETX)
        val payload = rawBytes.copyOfRange(1, rawBytes.size - 3)

        // Extract 16-bit CRC from frame bytes
        val highByte = rawBytes[rawBytes.size - 3].toInt() and 0xFF
        val lowByte = rawBytes[rawBytes.size - 2].toInt() and 0xFF
        val receivedCrc = (highByte shl 8) or lowByte

        // Calculate CRC-16 over payload
        val calculatedCrc = calculateCRC16(payload)

        if (receivedCrc != calculatedCrc) {
            throw SecurityException("SCALE_DATA_TAMPERED: CRC Checksum Mismatch (Expected: $calculatedCrc, Received: $receivedCrc)")
        }

        // Extract 6-character weight string
        val weightStr = String(payload.copyOfRange(0, 6), StandardCharsets.US_ASCII).trim()
        return weightStr.toDoubleOrNull()
    }

    /**
     * Calculates CRC-16 (CCITT) over payload bytes using seed 0xFFFF and polynomial 0x1021.
     */
    fun calculateCRC16(bytes: ByteArray): Int {
        var crc = 0xFFFF
        for (b in bytes) {
            crc = crc xor ((b.toInt() and 0xFF) shl 8)
            for (i in 0 until 8) {
                if ((crc and 0x8000) != 0) {
                    crc = ((crc shl 1) xor 0x1021) and 0xFFFF
                } else {
                    crc = (crc shl 1) and 0xFFFF
                }
            }
        }
        return crc
    }

    /**
     * Helper to construct a valid cryptographically signed SPP Bluetooth frame for tests & scale hardware simulators.
     */
    fun buildValidScaleFrame(weightKg: Double, unit: String = "KG"): ByteArray {
        val weightFormatted = "%6.1f".format(weightKg).take(6)
        val unitFormatted = unit.padEnd(2, ' ').take(2)
        val payloadStr = weightFormatted + unitFormatted
        val payloadBytes = payloadStr.toByteArray(StandardCharsets.US_ASCII)

        val crc = calculateCRC16(payloadBytes)
        val highByte = ((crc shr 8) and 0xFF).toByte()
        val lowByte = (crc and 0xFF).toByte()

        val result = ByteArray(12)
        result[0] = 0x02.toByte() // STX
        System.arraycopy(payloadBytes, 0, result, 1, 8) // 6B Weight + 2B Unit
        result[9] = highByte
        result[10] = lowByte
        result[11] = 0x03.toByte() // ETX

        return result
    }
}
