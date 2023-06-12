package com.grigroviska.passwordia.activities

import android.content.ClipData
import android.content.ClipboardManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.grigroviska.passwordia.databinding.ActivityGeneratorBinding

class Generator : AppCompatActivity() {

    private lateinit var binding : ActivityGeneratorBinding

    private lateinit var output : TextInputEditText
    private lateinit var length : Slider
    private lateinit var digits : SwitchCompat
    private lateinit var letters : SwitchCompat
    private lateinit var symbols : SwitchCompat
    private lateinit var copy : MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGeneratorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        output = binding.outputPassword
        length = binding.lengthBar
        digits = binding.digits
        letters = binding.letters
        symbols = binding.symbols
        copy = binding.copyText

        digits.isChecked = true
        letters.isChecked = true
        symbols.isChecked = true

        newCode()

        binding.generatePasswordLayout.setEndIconOnClickListener {

            newCode()

        }

        copy.setOnClickListener {

            // Get the newCode value from the output field
            val newCode = output.text.toString()

            // Copy the newCode value to the clipboard
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("newCode", newCode)
            clipboard.setPrimaryClip(clip)

            // Show a Toast message to confirm the newCode has been copied
            Toast.makeText(this, "Copied", Toast.LENGTH_SHORT).show()

        }

        length.addOnChangeListener { slider, value, fromUser ->

            newCode()

        }

        digits.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && !letters.isChecked && !symbols.isChecked) {
                digits.isChecked = true
            }
            newCode()
        }

        letters.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && !digits.isChecked && !symbols.isChecked) {
                letters.isChecked = true
            }
            newCode()
        }

        symbols.setOnCheckedChangeListener { _, isChecked ->
            if (!isChecked && !digits.isChecked && !letters.isChecked) {
                symbols.isChecked = true
            }
            newCode()
        }


    }

    private fun generateCode(length: Int, useLetters: Boolean, useNumbers: Boolean, useSpecialCharacters: Boolean): String {
        val letters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val numbers = "0123456789"
        val specialCharacters = "!@#$%^&*()_+-=\\|[]{};':\",./<>?"

        var allowedCharacters = ""
        if (useLetters) {
            allowedCharacters += letters
        }
        if (useNumbers) {
            allowedCharacters += numbers
        }
        if (useSpecialCharacters) {
            allowedCharacters += specialCharacters
        }

        val password = StringBuilder(length)
        val random = java.util.Random()
        for (i in 0 until length) {
            val randomIndex = random.nextInt(allowedCharacters.length)
            password.append(allowedCharacters[randomIndex])
        }

        return password.toString()
    }

    private fun getPasswordStrength(password: String): String {
        val length = password.length
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val strength = when {
            length < 6 -> "Weak"
            length < 8 -> "Moderate"
            length < 10 && hasUpperCase && hasLowerCase && (hasDigit || hasSpecialChar) -> "Strong"
            length >= 10 && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar -> "Very Strong"
            else -> "Moderate"
        }


        return strength
    }

    private fun newCode(){

        val newCode = generateCode(length.value.toInt(), letters.isChecked , digits.isChecked , symbols.isChecked)
        output.setText(newCode)
        val strength = getPasswordStrength(newCode)
        binding.strengthOrWeak.text = strength

        val controlBar = binding.controlBar

        when (strength) {
            "Weak" -> controlBar.setBackgroundColor(Color.RED)
            "Moderate" -> controlBar.setBackgroundColor(Color.YELLOW)
            "Strong", "Very Strong" -> controlBar.setBackgroundColor(Color.GREEN)
            else -> controlBar.setBackgroundColor(Color.YELLOW)
        }

    }

}
