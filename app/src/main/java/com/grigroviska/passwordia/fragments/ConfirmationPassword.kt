package com.grigroviska.passwordia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.FragmentConfirmationPasswordBinding


class ConfirmationPassword : Fragment() {

    private lateinit var auth : FirebaseAuth

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

        //Initialize
        auth = FirebaseAuth.getInstance()

        password = binding.masterPasswordConfirmation
        confirmationPassword = binding.masterPasswordConfirmation
        signUpButton = binding.signUp

        val passwordFromPreviousFragment = arguments?.getString("password")
        binding.masterPassword.setText(passwordFromPreviousFragment)

        val email = arguments?.getString("email")

        signUpButton.setOnClickListener {
            val emailText = email.toString().trim()
            val passwordText = password.text.toString().trim()
            val confirmationPasswordText = confirmationPassword.text.toString().trim()

            if (passwordText.isNotEmpty() && confirmationPasswordText.isNotEmpty()) {
                if (passwordText == confirmationPasswordText) {
                    auth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(requireContext(), "Your account has been successfully created", Toast.LENGTH_LONG).show()
                                val selectEntryFragment = SelectEntry()
                                requireActivity().supportFragmentManager.beginTransaction()
                                    .replace(R.id.frameLayout, selectEntryFragment)
                                    .addToBackStack(null)
                                    .commit()
                            } else {
                                try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                } else {
                    binding.confirmationPasswordLayout.helperText = "Data do not confirm each other."
                }
            } else {
                binding.confirmationPasswordLayout.helperText = "Should not be empty"
            }
        }

        return view
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