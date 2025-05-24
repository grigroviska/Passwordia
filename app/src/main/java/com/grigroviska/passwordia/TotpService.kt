package com.grigroviska.passwordia

import kotlinx.coroutines.*

/**
 * TOTP kodunu belirli aralıklarla üretip callback'e ileten servis
 */
class TOTPService {
    private val totpGenerator = TOTPGenerator()
    private val totpPeriod = 30 * 1000L // 30 saniye
    private var totpJob: Job? = null

    fun startTOTPTimer(secretKey: String, callback: (code: String, remainingSeconds: Int) -> Unit) {
        stopTOTPTimer()

        totpJob = CoroutineScope(Dispatchers.IO).launch {
            while (isActive) {
                val currentTimeMillis = System.currentTimeMillis()
                val remainingTime = totpPeriod - (currentTimeMillis % totpPeriod)
                val remainingSeconds = (remainingTime / 1000).toInt()

                val code = totpGenerator.generateTOTP(secretKey)
                callback(code, remainingSeconds)

                delay(1000L)
            }
        }
    }

    fun stopTOTPTimer() {
        totpJob?.cancel()
        totpJob = null
    }
}
