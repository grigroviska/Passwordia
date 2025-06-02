package com.grigroviska.passwordia.activities

import BackupDialog
import android.app.Dialog
import com.grigroviska.passwordia.R
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.passwordia.databinding.ActivitySettingsMenuBinding
import com.grigroviska.passwordia.viewModel.LoginViewModel
import androidx.core.net.toUri
import androidx.core.content.edit

class SettingsMenu : AppCompatActivity() {

    private lateinit var binding : ActivitySettingsMenuBinding
    private lateinit var viewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        val hideTagSwitch = binding.hideTagViewSwitch
        val hideTagContainer = binding.hideTagContainer
        val hideTagListText = binding.hideTagListText

        val biometricSwitch: SwitchCompat = binding.biometricSwitch

        val entryTypePrefs = this.getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)
        val savedEntryType = entryTypePrefs.getString("entry_type", "")
        biometricSwitch.isChecked = savedEntryType == "biometric"

        biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                saveEntryType("biometric", entryTypePrefs)
            } else {
                saveEntryType("manuel", entryTypePrefs)
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

        val settingsPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        var hiddenTagsSet = settingsPrefs.getStringSet("hidden_tags", emptySet()) ?: emptySet()
        var isHideTagEnabled = settingsPrefs.getBoolean("hide_tag_enabled", false)

        hideTagSwitch.isChecked = isHideTagEnabled
        hideTagContainer.visibility = if (isHideTagEnabled) View.VISIBLE else View.GONE
        updateHideTagListText(settingsPrefs, hideTagListText)

        hideTagSwitch.setOnCheckedChangeListener { _, isChecked ->
            isHideTagEnabled = isChecked
            hideTagContainer.visibility = if (isChecked) View.VISIBLE else View.GONE
            settingsPrefs.edit().putBoolean("hide_tag_enabled", isChecked).apply()
        }

        hideTagListText.setOnClickListener {
            val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)

            val dialogView = layoutInflater.inflate(R.layout.hide_tag, null)
            val etHiddenTags = dialogView.findViewById<EditText>(R.id.etHiddenTags)
            val saveButton = dialogView.findViewById<Button>(R.id.exportButton)

            val currentDialogHiddenTags = settingsPrefs.getStringSet("hidden_tags", emptySet()) ?: emptySet()
            if (currentDialogHiddenTags.isNotEmpty()) {
                etHiddenTags.setText(currentDialogHiddenTags.joinToString(","))
            } else {
                etHiddenTags.setText("")
            }

            dialog.setContentView(dialogView)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
            dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            saveButton.setOnClickListener {
                val input = etHiddenTags.text.toString()
                val newHiddenTags = input.split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()

                settingsPrefs.edit().putStringSet("hidden_tags", newHiddenTags).apply()
                hiddenTagsSet = newHiddenTags

                updateHideTagListText(settingsPrefs, hideTagListText)

                dialog.dismiss()
            }

            dialog.show()
        }
    }

    private fun updateHideTagListText(prefs: SharedPreferences, textView: TextView) {
        val currentHiddenTags = prefs.getStringSet("hidden_tags", emptySet()) ?: emptySet()
        if (currentHiddenTags.isNotEmpty()) {
            textView.text = "${getString(R.string.hidden_tag)}${currentHiddenTags.joinToString(", ")}"
        } else {
            textView.text = getString(R.string.enter_texts_to_hide)
        }
    }

    private fun showBackupDialog() {
        val dialog = BackupDialog()
        dialog.show(supportFragmentManager, "password_generator_dialog")
    }

    private fun saveEntryType(entryType: String, prefs: SharedPreferences) {
        prefs.edit {
            putString("entry_type", entryType)
        }
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
        val entryTypePrefs = getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)
        entryTypePrefs.edit {
            clear()
        }

        val settingsPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        settingsPrefs.edit {
            clear()
        }

        viewModel.signOutAndClearData(
            onSuccess = {
                val intent = Intent(this, SignMain::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            },
            onFailure = { exception ->
                Toast.makeText(this, "Sign out failed: ${exception.message}", Toast.LENGTH_LONG).show()
            }
        )
    }
}