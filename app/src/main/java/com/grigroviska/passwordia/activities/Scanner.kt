package com.grigroviska.passwordia.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.zxing.ResultPoint
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.ActivityScannerBinding
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView

class Scanner : AppCompatActivity() {

    private lateinit var binding: ActivityScannerBinding
    private lateinit var barcodeScannerView: DecoratedBarcodeView
    private lateinit var barcodeCallback: BarcodeCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityScannerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        barcodeScannerView = findViewById(R.id.barcodeScanner)
        barcodeScannerView.setStatusText("Scan a barcode")

        barcodeCallback = object : BarcodeCallback {
            override fun barcodeResult(result: BarcodeResult?) {
                result?.let {
                    val scannedCode = result.text
                    handleScannedCode(scannedCode)
                }
            }

            override fun possibleResultPoints(resultPoints: MutableList<ResultPoint>?) {}
        }
    }

    override fun onResume() {
        super.onResume()
        barcodeScannerView.resume()
        barcodeScannerView.decodeContinuous(barcodeCallback)
    }

    override fun onPause() {
        super.onPause()
        barcodeScannerView.pause()
    }

    private fun handleScannedCode(code: String) {
        val otpData = extractOTPData(code)
        otpData?.let {
            val accountName = otpData.accountName
            val secret = otpData.secret

            if (accountName != null && secret != null) {
                val resultIntent = Intent()
                resultIntent.putExtra("accountName", accountName)
                resultIntent.putExtra("secret", secret)
                setResult(RESULT_OK, resultIntent)
                finish()
            } else {
                showToast("Invalid QR code data")
            }
        } ?: run {
            showToast("Invalid QR code data")
        }
    }

    private fun extractOTPData(code: String): OtpData? {
        val uri = Uri.parse(code)
        val issuer = extractAccountName(code)
        val secret = uri.getQueryParameter("secret")
        val accountName = "$issuer"

        return if (!accountName.isNullOrBlank() && !secret.isNullOrBlank()) {
            OtpData(accountName, secret)
        } else {
            null
        }
    }

    private data class OtpData(
        val accountName: String?,
        val secret: String?
    )

    private fun extractAccountName(code: String): String? {
        val start = code.indexOf("totp/") + 5
        val end = code.indexOf("?", start)
        if (start != -1 && end != -1 && end > start) {
            return code.substring(start, end)
        }
        return null
    }

    private fun showToast(message: String) {
        Toast.makeText(this@Scanner, message, Toast.LENGTH_SHORT).show()
    }
}
