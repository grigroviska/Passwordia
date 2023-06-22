package com.grigroviska.passwordia

import org.apache.commons.codec.binary.Base32
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import kotlin.experimental.and

class TOTPGenerator(private val digits: Int = 6, private val hmacAlgorithm: String = "HmacSHA1") {
    private val timeStep: Long = 30

    fun generateTOTP(secretKey: String): String {
        val counter = getTimeCounter(timeStep)
        val hmac = generateHMAC(secretKey, counter)
        val otp = truncateHMAC(hmac, digits)
        return otp
    }

    private fun getTimeCounter(timeStep: Long): Long {
        val currentTime = System.currentTimeMillis() / 1000
        return currentTime / timeStep
    }

    private fun generateHMAC(secretKey: String, counter: Long): ByteArray {
        val key = Base32().decode(secretKey)
        val data = ByteArray(8)
        var value = counter
        for (i in 7 downTo 0) {
            data[i] = (value and 0xff).toByte()
            value = value shr 8
        }
        val secretKeySpec = SecretKeySpec(key, hmacAlgorithm)
        val mac = Mac.getInstance(hmacAlgorithm)
        mac.init(secretKeySpec)
        return mac.doFinal(data)
    }

    private fun truncateHMAC(hmac: ByteArray, digits: Int): String {
        val offset = hmac[hmac.size - 1] and 0xf
        val binary =
            (hmac[offset.toInt()].toInt() and 0x7f shl 24) or (hmac[offset + 1].toInt() and 0xff shl 16) or
                    (hmac[offset + 2].toInt() and 0xff shl 8) or (hmac[offset + 3].toInt() and 0xff)
        val otp = binary % Math.pow(10.0, digits.toDouble())
        return "%0${digits}d".format(otp.toInt())
    }
}
