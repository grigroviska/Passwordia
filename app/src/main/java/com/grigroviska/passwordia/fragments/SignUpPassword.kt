package com.grigroviska.passwordia.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.FragmentSignUpPasswordBinding

class SignUpPassword : Fragment() {

    private lateinit var binding: FragmentSignUpPasswordBinding
    private lateinit var signUpButton: Button
    private lateinit var password: TextInputEditText
    private lateinit var navController : NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignUpPasswordBinding.inflate(inflater, container, false)
        val view = binding.root


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navController = Navigation.findNavController(view)

        password = binding.masterPassword
        signUpButton = binding.signUp

        signUpButton.setOnClickListener {
            val email = arguments?.getString("email").toString()
            val password = password.text.toString()

            if (getPasswordStrength(password) == getString(R.string.i_like_the_password) || getPasswordStrength(password) == getString(R.string.i_fell_in_love_with_the_password)) {
                val action = SignUpPasswordDirections.actionSignUpPasswordToConfirmationPassword(email, password)
                navController.navigate(action)
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
    }

    private fun getPasswordStrength(password: String) : String{
        val length = password.length
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val strength = when {
            length < 1 -> ""
            length < 6 -> getString(R.string.i_pretend_not_to_see_this_password)
            length < 8 -> getString(R.string.i_open_my_eyes_a_little_go_ahead)
            length < 10 && hasUpperCase && hasLowerCase && (hasDigit || hasSpecialChar) -> getString(
                            R.string.i_like_the_password)
            length >= 10 && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar -> getString(
                            R.string.i_fell_in_love_with_the_password)
            else ->  getString(R.string.i_open_my_eyes_a_little_go_ahead)
        }

        binding.passwordLayout.helperText = strength

        return strength
    }

}
