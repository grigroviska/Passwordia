package com.grigroviska.passwordia.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.activities.MainActivity
import com.grigroviska.passwordia.databinding.FragmentSignInEmailBinding

class SignInEmail : Fragment() {

    private lateinit var binding: FragmentSignInEmailBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var email: TextInputEditText
    private lateinit var nextButton: Button
    private lateinit var signUpButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignInEmailBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        val sharedPreferences = requireContext().getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)
        val entryType = sharedPreferences.getString("entry_type", "")

        email = binding.email
        nextButton = binding.nextPage
        signUpButton = binding.signUp

        if (currentUser != null) {
            if (entryType == "manuel") {
                val passwordFragment = SignInPassword.newInstance(email.toString())
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, passwordFragment)
                    .addToBackStack(null)
                    .commit()
            } else if (entryType == "biometric") {
                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            } else {
                val selectEntryFragment = SelectEntry()
                requireActivity().supportFragmentManager.beginTransaction()
                    .replace(R.id.frameLayout, selectEntryFragment)
                    .addToBackStack(null)
                    .commit()
            }
        }

        nextButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                checkEmailExists(emailText)
            } else {
                binding.emailLayout.helperText = "Please provide a valid email address."
            }
        }

        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailLayout.helperText =null
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

        signUpButton.setOnClickListener {
            val signUpFragment = SignUpEmail.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, signUpFragment)
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun checkEmailExists(email: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList()
                    if (signInMethods.isNotEmpty()) {
                        // Email already exists, navigate to password fragment
                        val passwordFragment = SignInPassword.newInstance(email)
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, passwordFragment)
                            .addToBackStack(null)
                            .commit()
                    } else {
                        // Email does not exist
                        binding.emailLayout.helperText = "We couldn't find this account.Please check the email address."
                    }
                } else {
                    // Error occurred while checking email
                    binding.emailLayout.helperText = "Error occurred while checking email!"
                }
            }
    }

    companion object {
        fun newInstance(): SignInEmail {
            return SignInEmail()
        }
    }
}