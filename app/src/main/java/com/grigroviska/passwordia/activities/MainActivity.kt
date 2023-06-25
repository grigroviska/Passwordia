package com.grigroviska.passwordia.activities

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.BIOMETRIC_STRONG
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.ActivityMainBinding
import java.util.concurrent.Executor

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    private var authenticationFailedCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)


        checkDeviceHasBiometric()

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor, object : BiometricPrompt.AuthenticationCallback(){

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)

                Toast.makeText(this@MainActivity,"Authentication error : $errString",Toast.LENGTH_LONG).show()

            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                val intent = Intent(this@MainActivity, HomeActivity::class.java)
                startActivity(intent)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()

                Toast.makeText(this@MainActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
            }

        })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Passwordia")
            .setSubtitle("Protect your account")
            .setNegativeButtonText("Cancel")
            .build()

        biometricPrompt.authenticate(promptInfo)

        binding.imageView.setOnClickListener {

            promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Passwordia")
                .setSubtitle("Protect your account")
                .setNegativeButtonText("Cancel")
                .build()

            biometricPrompt.authenticate(promptInfo)

        }

    }

    fun checkDeviceHasBiometric(){

        val biometricManager = BiometricManager.from(this)
        when(biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)){

            BiometricManager.BIOMETRIC_SUCCESS ->{

                Log.d("Passwordia_APP","App can authenticate using biometrics.")
                binding.infoText.text = getString(R.string.app_can_authenticate_using_biometrics)

            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE->{

                Log.d("Passwordia_APP","Biometric features are currently unavailable.")
                binding.infoText.text = getString(R.string.biometric_features_are_currently_unavailable)


            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED->{

                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {

                    putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BIOMETRIC_STRONG or DEVICE_CREDENTIAL)

                }

                startActivityForResult(enrollIntent, 100)

            }

        }

    }

}