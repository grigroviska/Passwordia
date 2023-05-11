package com.grigroviska.passwordia.fragments

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
import com.grigroviska.passwordia.databinding.FragmentSignUpPasswordBinding

class SignUpPassword : Fragment() {

    private lateinit var binding: FragmentSignUpPasswordBinding
    private lateinit var signUpButton: Button
    private lateinit var password: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignUpPasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        password = binding.masterPassword
        signUpButton = binding.signUp

        signUpButton.setOnClickListener {
            val email = arguments?.getString("email")
            val password = password.text.toString()

            if (getPasswordStrength(password) == "I like the password" || getPasswordStrength(password) == "I fell in love with the password") {
                val confirmationPasswordFragment =
                    ConfirmationPassword.newInstance(email.toString(), password)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, confirmationPasswordFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.passwordLayout.helperText = null
            }

            override fun afterTextChanged(p0: Editable?) {
                getPasswordStrength(p0.toString())
            }
        })

        return view
    }

    private fun getPasswordStrength(password: String) : String{
        val length = password.length
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val strength = when {
            length < 1 -> ""
            length < 6 -> "I pretend not to see this password"
            length < 8 -> "I open my eyes a little go ahead"
            length < 10 && hasUpperCase && hasLowerCase && (hasDigit || hasSpecialChar) -> "I like the password"
            length >= 10 && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar -> "I fell in love with the password"
            else ->  "I open my eyes a little go ahead"
        }

        binding.passwordLayout.helperText = strength

        return strength
    }

    companion object {
        fun newInstance(email: String): SignUpPassword {
            val fragment = SignUpPassword()
            val bundle = Bundle()
            bundle.putString("email", email)
            fragment.arguments = bundle
            return fragment
        }
    }
}
