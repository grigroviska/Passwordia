package com.grigroviska.passwordia.activities

import android.app.AlertDialog
import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.PasswordGeneratorDialogBinding

class PasswordGeneratorDialog : DialogFragment() {

    private lateinit var output : TextInputEditText
    private lateinit var length : Slider
    private lateinit var digits : SwitchCompat
    private lateinit var letters : SwitchCompat
    private lateinit var symbols : SwitchCompat
    private lateinit var strengthOrWeak : TextView
    private lateinit var controlBar : Slider

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val dialogView = inflater.inflate(R.layout.password_generator_dialog, null)
        val builder = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Use") { dialog, _ ->

                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

        output = dialogView.findViewById(R.id.passText)
        length = dialogView.findViewById(R.id.lengthBar)
        digits = dialogView.findViewById(R.id.digits)
        letters = dialogView.findViewById(R.id.letters)
        symbols = dialogView.findViewById(R.id.symbols)
        strengthOrWeak = dialogView.findViewById(R.id.strengthOrWeak)
        controlBar = dialogView.findViewById(R.id.controlBar)

        val passTextLayout = dialogView.findViewById<TextInputLayout>(R.id.passTextLayout)

        passTextLayout.setEndIconOnClickListener {

            newCode()

        }


        return builder.create()
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

        val controlBar = controlBar

        when (strength) {
            "Weak" -> controlBar.setBackgroundColor(Color.RED)
            "Moderate" -> controlBar.setBackgroundColor(Color.YELLOW)
            "Strong", "Very Strong" -> controlBar.setBackgroundColor(Color.GREEN)
            else -> controlBar.setBackgroundColor(Color.YELLOW)
        }

    }

}
