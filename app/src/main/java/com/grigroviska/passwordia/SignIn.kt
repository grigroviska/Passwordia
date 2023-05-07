package com.grigroviska.passwordia

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.grigroviska.passwordia.databinding.ActivitySignInBinding

class SignIn : AppCompatActivity() {

    private lateinit var binding : ActivitySignInBinding

    private lateinit var auth: FirebaseAuth

    private lateinit var email : TextInputEditText
    private lateinit var masterPassword : TextInputEditText
    private lateinit var signInButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()

        }

        email = binding.email
        masterPassword = binding.masterPassword
        signInButton = binding.signIn

        signInButton.setOnClickListener {

            val emailText = email.text.toString().trim()
            val passwordText = masterPassword.text.toString().trim()

            signInUser(emailText, passwordText)

        }

        binding.goToSignUp.setOnClickListener {

            val intent = Intent(this, SignUp::class.java)
            intent.putExtra("email", email.text.toString().trim())
            startActivity(intent)
            finish()

        }

        binding.goToForgotPassword.setOnClickListener {

            val intent = Intent(this, ForgotPassword::class.java)
            intent.putExtra("email", email.text.toString().trim())
            startActivity(intent)
            finish()

        }

        email.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (p0.toString().trim().isNotEmpty()){

                    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(p0.toString().trim()).matches()){

                        binding.emailLayout.error = "Enter a valid email address!"

                    }else{

                        binding.emailLayout.error = null

                    }

                }else{

                    binding.emailLayout.error = null

                }

            }

            override fun afterTextChanged(p0: Editable?) {
            }


        })

        masterPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                getPasswordStrength(p0.toString().trim())

            }

            override fun afterTextChanged(p0: Editable?) {
            }


        })

    }

    private fun signInUser(emailText: String, passwordText: String) {

        if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()){

            if (getPasswordStrength(passwordText)){

                auth.signInWithEmailAndPassword(emailText, passwordText)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            Toast.makeText(this, "Sign in successful $emailText", Toast.LENGTH_SHORT).show()

                            val intent = Intent(this, MainActivity::class.java)
                            startActivity(intent)
                            finish()

                        } else {
                            try {
                                throw task.exception!!
                            } catch (e: FirebaseAuthInvalidUserException) {
                                Toast.makeText(this, "Invalid user", Toast.LENGTH_SHORT).show()
                            } catch (e: FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }

            }
            else{

                Toast.makeText(this, "Invalid password", Toast.LENGTH_SHORT).show()

            }


        }else if (emailText.isNotEmpty() && emailText.isNotBlank()){

            binding.emailLayout.error = "Enter a valid email address!"

        }else{

            binding.emailLayout.error = ""

        }

    }

    private fun getPasswordStrength(password: String): Boolean{
        val length = password.length
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val strength = when {
            length < 1 -> false
            length < 6 -> false
            length < 8 -> false
            length < 10 && hasUpperCase && hasLowerCase && (hasDigit || hasSpecialChar) -> true
            length >= 10 && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar -> true
            else -> false
        }

        when {
            length < 1 -> binding.passwordLayout.error =""
            length < 6 -> binding.passwordLayout.error = "I pretend not to see this password"
            length < 8 -> binding.passwordLayout.error =  "I open my eyes a little go ahead"
            length < 10 && hasUpperCase && hasLowerCase && (hasDigit || hasSpecialChar) ->binding.passwordLayout.error =""
            length >= 10 && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar ->binding.passwordLayout.error =""
            else -> binding.passwordLayout.error =  "I open my eyes a little go ahead"
        }

        return strength

    }

}