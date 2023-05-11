package com.grigroviska.passwordia.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.FragmentConfirmationPasswordBinding
import com.grigroviska.passwordia.databinding.FragmentForgotPasswordBinding

class ForgotPassword : Fragment() {

    private lateinit var binding : FragmentForgotPasswordBinding

    private lateinit var auth : FirebaseAuth

    private lateinit var email : TextInputEditText
    private lateinit var resetButton : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentForgotPasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()

        email = binding.email
        resetButton = binding.resetPassword

        val endIconDrawable = resources.getDrawable(R.drawable.ic_forgot, null)
        binding.emailLayout.endIconDrawable = endIconDrawable

        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.emailLayout.helperText = null
            }

            override fun afterTextChanged(p0: Editable?) {
            }


        })

        resetButton.setOnClickListener {
            val emailText = email.text.toString().trim()
            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {
                // Send password reset email to the user's email address
                FirebaseAuth.getInstance().sendPasswordResetEmail(email.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Password reset email sent successfully
                            Toast.makeText(requireContext(), "Password reset email sent.", Toast.LENGTH_SHORT).show()
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseException) {
                                val message = exception.message
                                if (message == "There is no user record corresponding to this identifier. The user may have been deleted.") {
                                    // User not found in the system
                                    binding.emailLayout.error = "No user found with this email address."
                                    return@addOnCompleteListener
                                }
                            }
                            // Other error occurred
                            Toast.makeText(requireContext(), "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } else {
                binding.emailLayout.helperText = "Please provide a valid email address."
            }
        }



        return view
    }


    companion object {
        fun newInstance(): ForgotPassword {
            return ForgotPassword()
        }
    }

}