package com.grigroviska.passwordia.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
                    returnScannedCode(scannedCode)
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

    private fun returnScannedCode(code: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("scannedCode", code)
        setResult(RESULT_OK, resultIntent)
        finish()
    }

}