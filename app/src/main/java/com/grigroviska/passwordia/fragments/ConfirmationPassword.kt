package com.grigroviska.passwordia.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
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
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentConfirmationPasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        //Initialize
        auth = FirebaseAuth.getInstance()

        password = binding.masterPasswordConfirmation
        confirmationPassword = binding.masterPasswordConfirmation
        signUpButton = binding.signUp

        val passwordFromPreviousFragment = arguments?.getString("pass").toString()
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
                                Toast.makeText(requireContext(), getString(R.string.your_account_has_been_successfully_created), Toast.LENGTH_LONG).show()
                                navController.navigate(R.id.action_confirmationPassword_to_selectEntry)
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
                    binding.confirmationPasswordLayout.helperText = getString(R.string.data_do_not_confirm_each_other)
                }
            } else {
                binding.confirmationPasswordLayout.helperText = getString(R.string.should_not_be_empty)
            }
        }

    }
}