package com.grigroviska.passwordia.activities

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.grigroviska.passwordia.R

class PasswordGeneratorDialog : DialogFragment() {

    private lateinit var passwordGeneratorListener: PasswordGeneratorDialogListener

    private lateinit var output : TextInputEditText
    private lateinit var length : Slider
    private lateinit var digits : SwitchCompat
    private lateinit var letters : SwitchCompat
    private lateinit var symbols : SwitchCompat
    private lateinit var controlBar : View
    private lateinit var strengthOrWeak : TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val mDialog = LayoutInflater.from(context).inflate(R.layout.password_generator_dialog, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(mDialog)
            .setPositiveButton("Use") { _, _ ->
                passwordGeneratorListener.onPasswordGenerated(output.text.toString())
            }
            .setNegativeButton("Cancel") { _, _ ->
            }
            .create()

        output = mDialog.findViewById(R.id.passText)
        length = mDialog.findViewById(R.id.lengthBar)
        digits = mDialog.findViewById(R.id.digits)
        letters = mDialog.findViewById(R.id.letters)
        symbols = mDialog.findViewById(R.id.symbols)
        controlBar = mDialog.findViewById(R.id.controlBar)
        strengthOrWeak = mDialog.findViewById(R.id.strengthOrWeak)

        digits.isChecked = true
        letters.isChecked = true
        symbols.isChecked = true

        newCode()

        val pTextLayout = mDialog.findViewById<TextInputLayout>(R.id.passTextLayout)

        pTextLayout.setEndIconOnClickListener {

            newCode()

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

        return dialog
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        try {
            passwordGeneratorListener = context as PasswordGeneratorDialogListener
        } catch (e: ClassCastException) {
            throw ClassCastException("$context must implement PasswordGeneratorDialogListener")
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
        strengthOrWeak.text = strength



        when (strength) {
            "Weak" -> controlBar.setBackgroundColor(Color.RED)
            "Moderate" -> controlBar.setBackgroundColor(Color.YELLOW)
            "Strong", "Very Strong" -> controlBar.setBackgroundColor(Color.GREEN)
            else -> controlBar.setBackgroundColor(Color.YELLOW)
        }

    }
}
