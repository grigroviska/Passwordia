package com.grigroviska.passwordia.Fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.textfield.TextInputEditText
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.FragmentConfirmationPasswordBinding


class ConfirmationPassword : Fragment() {

    private lateinit var binding: FragmentConfirmationPasswordBinding
    private lateinit var signUpButton: Button
    private lateinit var password: TextInputEditText
    private lateinit var confirmationPassword: TextInputEditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentConfirmationPasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        password = binding.masterPasswordConfirmation
        confirmationPassword = binding.masterPasswordConfirmation
        signUpButton = binding.signUp

        val passwordFromPreviousFragment = arguments?.getString("password")
        binding.masterPassword.setText(passwordFromPreviousFragment)

        signUpButton.setOnClickListener {
            val passwordText = password.text.toString().trim()
            val confirmationPasswordText = confirmationPassword.text.toString().trim()

            if (passwordText.isNotEmpty() && confirmationPasswordText.isNotEmpty()) {
                if (passwordText == confirmationPasswordText) {

                } else {
                    binding.confirmationPasswordLayout.helperText = "Data do not confirm each other."
                }
            } else {
                binding.confirmationPasswordLayout.helperText = "Should not be empty"
            }
        }

        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                checkPasswordStrength(p0.toString())
            }
        })

        return view
    }

    private fun checkPasswordStrength(password: String) {
        // Implement password strength checking logic if needed
    }

    companion object {
        fun newInstance(email: String, password: String): ConfirmationPassword {
            val fragment = ConfirmationPassword()
            val bundle = Bundle()
            bundle.putString("email", email)
            bundle.putString("password", password)
            fragment.arguments = bundle
            return fragment
        }
    }
}