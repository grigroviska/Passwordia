package com.grigroviska.passwordia.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.grigroviska.passwordia.databinding.ActivityCreateAuthenticatorBinding

class CreateAuthenticator : AppCompatActivity() {

    private lateinit var binding : ActivityCreateAuthenticatorBinding
    private val CAMERA_PERMISSION_REQUEST_CODE = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAuthenticatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        binding.secretKeyLayout.setEndIconOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, Scanner::class.java)
                startActivityForResult(intent, REQUEST_CODE_SCANNER)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }

        }

        binding.saveData.setOnClickListener {



        }

    }

    companion object {
        private const val REQUEST_CODE_SCANNER = 100
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCANNER && resultCode == RESULT_OK) {
            val scannedCode = data?.getStringExtra("scannedCode")
            binding.secretKey.setText(scannedCode)
        }
    }
}