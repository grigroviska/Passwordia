package com.grigroviska.passwordia.activities

import android.os.Handler
import android.os.Looper
import com.grigroviska.passwordia.TOTPGenerator // TOTPGenerator sınıfınızın burada olduğunu varsayıyorum
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// TOTP kodunu ve süresini yöneten sınıf
class TotpTimerManager(
    private val onTotpCodeGenerated: (String) -> Unit,
    private val onTimerTick: (Float) -> Unit,
    private val onTimerFinished: () -> Unit
) {

    private val totpGenerator = TOTPGenerator()
    private val handler = Handler(Looper.getMainLooper())
    private var timerJob: Job? = null
    private var totpKey: String? = null

    companion object {
        private const val TOTP_INTERVAL_SECONDS = 30L
        private const val PROGRESS_UPDATE_INTERVAL_MS = 100L
    }

    fun startTotpTimer(key: String) {
        // Eğer zaten aynı anahtar için bir sayaç çalışıyorsa, tekrar başlatmaya gerek yok.
        if (totpKey == key && timerJob?.isActive == true) {
            return
        }
        totpKey = key
        stopTotpTimer()

        timerJob = CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                val currentTimeMillis = System.currentTimeMillis()
                val currentSecond = (currentTimeMillis / 1000) % TOTP_INTERVAL_SECONDS
                val remainingSeconds = TOTP_INTERVAL_SECONDS - currentSecond

                val code = totpGenerator.generateTOTP(key)
                handler.post { onTotpCodeGenerated(code) }

                val initialProgress = (currentSecond.toFloat() / TOTP_INTERVAL_SECONDS) * TOTP_INTERVAL_SECONDS
                handler.post { onTimerTick(initialProgress) }

                val totalProgressSteps = (remainingSeconds * 1000 / PROGRESS_UPDATE_INTERVAL_MS).toInt()
                for (i in 0 until totalProgressSteps) {

                    val animatedProgress = initialProgress + (i.toFloat() / totalProgressSteps) * (TOTP_INTERVAL_SECONDS - initialProgress)
                    handler.post { onTimerTick(animatedProgress) }
                    delay(PROGRESS_UPDATE_INTERVAL_MS)
                }

                handler.post { onTimerFinished() }
                delay(1000)
            }
        }
    }

    /**
     * TOTP sayacını durdurur.
     */
    fun stopTotpTimer() {
        timerJob?.cancel() // Coroutine işini iptal et
        timerJob = null // Referansı temizle
        totpKey = null // Anahtarı temizle
    }
}