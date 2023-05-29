package com.grigroviska.passwordia.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.grigroviska.passwordia.activities.MainActivity
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.activities.HomeActivity
import com.grigroviska.passwordia.databinding.FragmentSignInPasswordBinding


class SignInPassword : Fragment() {

    private lateinit var binding: FragmentSignInPasswordBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var email: String
    private lateinit var password: TextInputEditText
    private lateinit var signInButton: Button
    private lateinit var navController: NavController

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignInPasswordBinding.inflate(inflater, container, false)
        val view = binding.root


        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        navController = Navigation.findNavController(view)

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        password = binding.masterPassword
        signInButton = binding.signIn

        email = arguments?.getString("email").toString()

        val spinner: Spinner = binding.accounts

        if(currentUser!=null) {

            email = currentUser.email.toString()

        }

        val items = mutableListOf<String>()
        items.add(email)
        items.addAll(listOf("Add another account"))

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, items)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedItem = items[position]
                if (selectedItem == "Add another account") {
                    auth.signOut()
                    navController.navigate(R.id.action_signInPassword_to_signInEmail)
                    navController.popBackStack()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }

        signInButton.setOnClickListener {
            if(currentUser!=null){
                val passwordText = password.text.toString().trim()
                signInUser(currentUser.email.toString(), passwordText)

            }else{

                val passwordText = password.text.toString().trim()
                signInUser(email, passwordText)

            }

        }

        password.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                binding.passwordLayout.helperText = null
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.goToForgotPassword.setOnClickListener {

            navController.navigate(R.id.action_signInPassword_to_forgotPassword)

        }

    }

    private fun signInUser(email: String, password: String) {
        if (password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), getString(R.string.sign_in_successful), Toast.LENGTH_SHORT).show()
                        status()
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            binding.passwordLayout.helperText = getString(R.string.invalid_user)
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            binding.passwordLayout.helperText = getString(R.string.wrong_master_password)
                        } catch (e: Exception) {
                            binding.passwordLayout.helperText = getString(R.string.authentication_failed)
                        }
                    }
                }
        } else {
            binding.passwordLayout.helperText =getString(R.string.enter_a_password)
        }
    }

    private fun status(){
        val sharedPreferences = requireContext().getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)
        val entryType = sharedPreferences.getString("entry_type", "")
        when (entryType) {
            "" -> {

                navController.navigate(R.id.action_signInPassword_to_selectEntry)

            }
            "biometric" -> {

                val intent = Intent(requireContext(), MainActivity::class.java)
                startActivity(intent)
                requireActivity().finish()

            }
            else -> {

                val intent = Intent(requireContext(), HomeActivity::class.java)
                startActivity(intent)
                requireActivity().finish()

            }
        }

    }
}