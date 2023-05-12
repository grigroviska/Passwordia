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

    companion object {
        private const val ARG_EMAIL = "email"

        fun newInstance(email: String): SignInPassword {
            val fragment = SignInPassword()
            val args = Bundle()
            args.putString(ARG_EMAIL, email)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentSignInPasswordBinding.inflate(inflater, container, false)
        val view = binding.root

        auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser

        password = binding.masterPassword
        signInButton = binding.signIn

        arguments?.let {
            email = it.getString(ARG_EMAIL, "")
        }

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
                    val signInEmailFragment = SignInEmail()
                    requireActivity().supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, signInEmailFragment)
                        .addToBackStack(null)
                        .commit()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Hiçbir şey seçilmediğinde yapılacak işlemler
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

            val forgotPasswordFragment = ForgotPassword.newInstance()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, forgotPasswordFragment)
                .addToBackStack(null)
                .commit()

        }

        return view
    }

    private fun signInUser(email: String, password: String) {
        if (password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(requireContext(), "Sign in successful", Toast.LENGTH_SHORT).show()
                        status()
                    } else {
                        try {
                            throw task.exception!!
                        } catch (e: FirebaseAuthInvalidUserException) {
                            binding.passwordLayout.helperText = "Invalid user"
                        } catch (e: FirebaseAuthInvalidCredentialsException) {
                            binding.passwordLayout.helperText = "Wrong Master Password."
                        } catch (e: Exception) {
                            binding.passwordLayout.helperText = "Authentication failed"
                        }
                    }
                }
        } else {
            binding.passwordLayout.helperText ="Enter a password"
        }
    }

    private fun status(){
        val sharedPreferences = requireContext().getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)
        val entryType = sharedPreferences.getString("entry_type", "")
        if(entryType==""){

            val selectEntryFragment = SelectEntry()
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, selectEntryFragment)
                .addToBackStack(null)
                .commit()

        }else if(entryType == "biometric"){

            val intent = Intent(requireContext(), MainActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }else{

            val intent = Intent(requireContext(), HomeActivity::class.java)
            startActivity(intent)
            requireActivity().finish()

        }

    }
}