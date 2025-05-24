package com.grigroviska.passwordia.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.passwordia.databinding.ActivityCreateAuthenticatorBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel

class CreateAuthenticator : AppCompatActivity() {

    private lateinit var binding: ActivityCreateAuthenticatorBinding
    private lateinit var loginViewModel: LoginViewModel
    private var loginData: LoginData? = null
    private var loginId: Int = -1

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 200
        private const val REQUEST_CODE_SCANNER = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAuthenticatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        // ID alınıyor
        loginId = intent.getIntExtra("loginId", -1)

        // Eğer loginId geçerliyse veriyi getir
        if (loginId != -1) {
            loginViewModel.getLoginById(loginId).observe(this) { login ->
                login?.let {
                    loginData = it
                    fillUI(it)
                } ?: run {
                    Toast.makeText(this, "Kayıt bulunamadı.", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }

        // QR iconuna basıldığında
        binding.secretKeyLayout.setEndIconOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, Scanner::class.java)
                startActivityForResult(intent, REQUEST_CODE_SCANNER)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }
        }

        // Kayıt butonu
        binding.createAuthData.setOnClickListener {
            if (loginData == null) {
                saveLoginData()
            } else {
                updateLoginData()
            }
        }
    }

    private fun fillUI(data: LoginData) {
        binding.apply {
            accountName.setText(data.accountName)
            secretKeyLayout.visibility = View.INVISIBLE
            secretKey.visibility = View.INVISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SCANNER && resultCode == RESULT_OK) {
            val secretKey = data?.getStringExtra("secret")
            val accountName = data?.getStringExtra("accountName")
            binding.secretKey.setText(secretKey)
            binding.accountName.setText(accountName)
        }
    }

    private fun saveLoginData() {
        val accountName = binding.accountName.text.toString().trim()
        val secretKey = binding.secretKey.text.toString().trim()

        if (accountName.isNotEmpty() && secretKey.isNotEmpty()) {
            val newLoginData = LoginData(
                id = 0,
                userName = null,
                alternateUserName = null,
                password = null,
                website = null,
                notes = null,
                itemName = null,
                category = null,
                accountName = accountName,
                totpKey = secretKey
            )
            loginViewModel.insert(newLoginData)
            finish()
        } else {
            Toast.makeText(this, "Lütfen gerekli alanları doldurun!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateLoginData() {
        val accountName = binding.accountName.text.toString().trim()

        if (accountName.isNotEmpty()) {
            val updatedData = loginData!!.copy(accountName = accountName)
            loginViewModel.update(updatedData)
            finish()
        } else {
            Toast.makeText(this, "Lütfen hesap adını doldurun!", Toast.LENGTH_SHORT).show()
        }
    }
}
