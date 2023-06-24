package com.grigroviska.passwordia.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.passwordia.databinding.ActivityCreateAuthenticatorBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel

class CreateAuthenticator : AppCompatActivity() {

    private lateinit var binding : ActivityCreateAuthenticatorBinding
    private val CAMERA_PERMISSION_REQUEST_CODE = 200
    private lateinit var loginViewModel: LoginViewModel
    private var loginData: LoginData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAuthenticatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        val loginId: Int = intent.getIntExtra("loginId", 999999)

        if (loginId != 999999) {
            loginViewModel.allLogin.observe(this@CreateAuthenticator) { loginDataList ->
                loginData = loginDataList.find { it.id == loginId }

                loginData?.let {
                    with(binding) {
                        accountName.setText(it.accountName)

                        createAuthData.setOnClickListener {
                            val updateAccountName = accountName.text.toString().trim()

                            if (updateAccountName.isNotEmpty()) {
                                val updatedAuthenticatorData = LoginData(
                                    loginId,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    null,
                                    updateAccountName,
                                    null
                                )
                                loginViewModel.update(updatedAuthenticatorData)
                                finish()
                            } else {
                                Toast.makeText(this@CreateAuthenticator, "Please fill in the required fields!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } ?: Toast.makeText(this@CreateAuthenticator, "An error has occurred!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.secretKeyLayout.setEndIconOnClickListener {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, Scanner::class.java)
                startActivityForResult(intent, REQUEST_CODE_SCANNER)
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
            }

        }

        binding.createAuthData.setOnClickListener {

            saveLoginData()

        }

    }

    companion object {
        private const val REQUEST_CODE_SCANNER = 100
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
            val loginData = LoginData(
                loginData?.id ?: 0,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                accountName,
                secretKey
            )

            loginViewModel.insert(loginData)
            finish()
        } else {
            Toast.makeText(this, "Please fill in the required fields!", Toast.LENGTH_SHORT).show()
        }
    }
}