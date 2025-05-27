package com.grigroviska.passwordia.activities

import BackupDialog
import com.grigroviska.passwordia.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.passwordia.databinding.ActivitySettingsMenuBinding
import com.grigroviska.passwordia.viewModel.LoginViewModel
import androidx.core.net.toUri

class SettingsMenu : AppCompatActivity() {

    private lateinit var binding : ActivitySettingsMenuBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val biometricSwitch: SwitchCompat = binding.biometricSwitch

        sharedPreferences = this.getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)
        val savedEntryType = sharedPreferences.getString("entry_type", "")
        biometricSwitch.isChecked = savedEntryType == "biometric"

        biometricSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                saveEntryType("biometric")
            }else{
                saveEntryType("manuel")
            }
        }

        binding.sendFeedbackLayout.setOnClickListener {

            val intent = Intent(Intent.ACTION_SENDTO)
            intent.data = "mailto:".toUri()
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("alperkaragozoglu3@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Crash Or Suggest")
            startActivity(intent)

        }

        binding.backupLayout.setOnClickListener {

            showBackupDialog()

        }

        binding.signOutLayout.setOnClickListener {
            showSignOutConfirmationDialog()
        }

    }

    private fun showBackupDialog() {
        val dialog = BackupDialog()
        dialog.show(supportFragmentManager, "password_generator_dialog")
    }


    private fun saveEntryType(entryType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("entry_type", entryType)
        editor.apply()
    }

    private fun showSignOutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sign_out_title))
            .setMessage(getString(R.string.sign_out_confirmation_message))
            .setPositiveButton(getString(R.string.yes)) { dialog, which ->
                performSignOutAndClearData()
            }
            .setNegativeButton(getString(R.string.no)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    private fun performSignOutAndClearData() {
        viewModel.signOutAndClearData(
            onSuccess = {

                // Çıkış yapıldıktan sonra MainActivity'ye yönlendir
                val intent = Intent(this, SignMain::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            },
            onFailure = { exception ->
                Toast.makeText(this, ": ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }


}