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
            val passwordText = password.text.toString().trim()
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