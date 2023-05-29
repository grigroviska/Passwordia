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
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.FragmentSignUpEmailBinding

class SignUpEmail : Fragment() {

    private lateinit var binding : FragmentSignUpEmailBinding

    private lateinit var auth : FirebaseAuth

    private lateinit var nextButton: Button
    private lateinit var email: TextInputEditText
    private lateinit var navController: NavController
    private lateinit var emailText : String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignUpEmailBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        navController = Navigation.findNavController(view)

        email = binding.email
        nextButton = binding.nextPage

        emailText = arguments?.getString("email").toString()

        nextButton.setOnClickListener {
            emailText = email.text.toString().trim()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                val auth = FirebaseAuth.getInstance()

                auth.fetchSignInMethodsForEmail(emailText)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (!signInMethods.isNullOrEmpty()) {
                                binding.emailLayout.error = getString(R.string.email_address_is_already_in_use)
                            } else {
                                val action = SignUpEmailDirections.actionSignUpEmailToSignUpPassword(emailText)
                                navController.navigate(action)
                            }
                        } else {
                            binding.emailLayout.error = getString(R.string.error_occurred_e_mail_address)
                        }
                    }
            } else {
                binding.emailLayout.error = getString(R.string.valid_email_address)
            }
        }

        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailLayout.error = null
            }

            override fun afterTextChanged(p0: Editable?) {}
        })

    }
}