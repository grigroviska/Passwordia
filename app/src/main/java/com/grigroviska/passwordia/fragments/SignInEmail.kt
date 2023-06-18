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
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
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
    private lateinit var navController : NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignInEmailBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        navController = Navigation.findNavController(view)

        val sharedPreferences = requireContext().getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)
        val entryType = sharedPreferences.getString("entry_type", "")

        email = binding.email
        nextButton = binding.nextPage
        signUpButton = binding.signUp

        if (currentUser != null) {
            val emailText = email.text.toString().trim()
            when (entryType) {
                "manuel" -> {
                    val action = SignInEmailDirections.actionSignInEmailToSignInPassword(emailText)
                    navController.navigate(action)
                }
                "biometric" -> {
                    val intent = Intent(requireContext(), MainActivity::class.java)
                    startActivity(intent)
                }
                else -> {
                    navController.navigate(R.id.action_signInEmail_to_selectEntry)
                }
            }
        }

        nextButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                checkEmailExists(emailText)
            } else {
                binding.emailLayout.helperText = getString(R.string.valid_email_address)
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
            val emailText = email.text.toString().trim()
            val action = SignInEmailDirections.actionSignInEmailToSignUpEmail(emailText)
            navController.navigate(action)
        }
    }

    private fun checkEmailExists(email: String) {
        auth.fetchSignInMethodsForEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val signInMethods = task.result?.signInMethods ?: emptyList()
                    if (signInMethods.isNotEmpty()) {
                        // Email already exists, navigate to password fragment
                        val action = SignInEmailDirections.actionSignInEmailToSignInPassword(email)
                        navController.navigate(action)
                    } else {
                        // Email does not exist
                        binding.emailLayout.helperText = getString(R.string.we_couldn_t_find_this_account)
                    }
                } else {
                    // Error occurred while checking email
                    binding.emailLayout.helperText = getString(R.string.error_occurred_while_checking_email)
                }
            }
    }
}