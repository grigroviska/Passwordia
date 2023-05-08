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
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.FragmentSignInPasswordBinding
import com.grigroviska.passwordia.databinding.FragmentSignUpEmailBinding

class SignUpEmail : Fragment() {

    private lateinit var binding : FragmentSignUpEmailBinding

    private lateinit var auth : FirebaseAuth

    private lateinit var nextButton: Button
    private lateinit var email: TextInputEditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignUpEmailBinding.inflate(inflater, container, false)
        val view = binding.root

        email = binding.email
        nextButton = binding.nextPage

        nextButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                val passwordFragment = SignUpPassword.newInstance(emailText)
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, passwordFragment)
                    .addToBackStack(null)
                    .commit()
            } else {
                binding.emailLayout.error = "Lütfen geçerli bir e-posta adresi girin."
            }
        }

        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        return view
    }

    companion object {
        fun newInstance(): SignUpEmail {
            return SignUpEmail()
        }
    }
}