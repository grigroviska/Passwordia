package com.grigroviska.passwordia.activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.grigroviska.passwordia.databinding.ActivitySettingsMenuBinding

class SettingsMenu : AppCompatActivity() {

    private lateinit var binding : ActivitySettingsMenuBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
            intent.data = Uri.parse("mailto:")
            intent.putExtra(Intent.EXTRA_EMAIL, arrayOf("alperkaragozoglu3@gmail.com"))
            intent.putExtra(Intent.EXTRA_SUBJECT, "Crash Or Suggest")
            startActivity(intent)

        }
    }

    private fun saveEntryType(entryType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("entry_type", entryType)
        editor.apply()
    }
}