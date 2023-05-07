package com.grigroviska.passwordia

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.grigroviska.passwordia.databinding.ActivitySignUpBinding

class SignUp : AppCompatActivity() {

    private lateinit var binding : ActivitySignUpBinding

    private lateinit var email : TextInputEditText
    private lateinit var masterPassword : TextInputEditText
    private lateinit var confirmationPassword : TextInputEditText
    private lateinit var signUpButton : Button
    private lateinit var checkPasswordStrong : TextView
    private lateinit var controlBar : View

    private lateinit var auth : FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialize
        auth = FirebaseAuth.getInstance()

        email = binding.email
        masterPassword = binding.masterPassword
        confirmationPassword = binding.masterPasswordConfirmation
        signUpButton = binding.signUp
        checkPasswordStrong = binding.checkPassword
        controlBar = binding.controlBar

        val emailFromSignUp = intent.getStringExtra("email")
        email.setText(emailFromSignUp.toString())

        masterPassword.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                checkPasswordStrong.visibility = View.VISIBLE
                controlBar.visibility = View.VISIBLE
                newCode()
                checkPasswordsMatch()
            }

            override fun afterTextChanged(p0: Editable?) {
            }
        })

        confirmationPassword.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                checkPasswordsMatch()
            }

            override fun afterTextChanged(p0: Editable?) {
            }


        })

        email.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                validateEmail(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {
            }
        })

        signUpButton.setOnClickListener {

            val emailText = email.text.toString().trim()
            val passwordText = masterPassword.text.toString().trim()

            if (android.util.Patterns.EMAIL_ADDRESS.matcher(emailText).matches()) {

                if (getPasswordStrength(passwordText) == "I like the password" || getPasswordStrength(passwordText) == "I fell in love with the password") {

                    val auth = FirebaseAuth.getInstance()

                    auth.createUserWithEmailAndPassword(emailText, passwordText)
                        .addOnCompleteListener(this) { task ->
                            if (task.isSuccessful) {

                                Toast.makeText(this@SignUp, "Your account has been successfully created", Toast.LENGTH_LONG).show()
                                val intent = Intent(this@SignUp, MainActivity::class.java)
                                startActivity(intent)
                                finish()

                            } else {
                                try {
                                    throw task.exception!!
                                } catch (e: FirebaseAuthInvalidUserException) {
                                    Toast.makeText(this@SignUp, e.message, Toast.LENGTH_LONG).show()
                                } catch (e: FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(this@SignUp, e.message, Toast.LENGTH_LONG).show()
                                } catch (e: Exception) {
                                    Toast.makeText(this@SignUp, e.message, Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                }else if(getPasswordStrength(passwordText) == "I pretend not to see this password" || getPasswordStrength(passwordText) == "I open my eyes a little go ahead"){

                    Toast.makeText(this@SignUp, "You must enter a strong password in your Passwordia app.", Toast.LENGTH_LONG).show()

                }

            }

        }

    }

    private fun getPasswordStrength(password: String): String {
        val length = password.length
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasDigit = password.any { it.isDigit() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        val strength = when {
            length < 1 -> ""
            length < 6 -> "I pretend not to see this password"
            length < 8 -> "I open my eyes a little go ahead"
            length < 10 && hasUpperCase && hasLowerCase && (hasDigit || hasSpecialChar) -> "I like the password"
            length >= 10 && hasUpperCase && hasLowerCase && hasDigit && hasSpecialChar -> "I fell in love with the password"
            else -> "I open my eyes a little go ahead"
        }


        return strength
    }

    private fun newCode(){

        val strength = getPasswordStrength(binding.masterPassword.text.toString())
        binding.checkPassword.text = strength

        val controlBar = binding.controlBar

        when (strength) {
            "" -> controlBar.visibility = View.INVISIBLE
            "I pretend not to see this password" -> controlBar.setBackgroundColor(Color.RED)
            "I open my eyes a little go ahead" -> controlBar.setBackgroundColor(Color.YELLOW)
            "I like the password", "I fell in love with the password" -> controlBar.setBackgroundColor(Color.GREEN)
            else -> controlBar.setBackgroundColor(Color.YELLOW)
        }

    }

    private fun checkPasswordsMatch() {
        val masterPasswordText = masterPassword.text.toString().trim()
        val confirmationPasswordText = confirmationPassword.text.toString().trim()

        val passwordsMatch = masterPasswordText == confirmationPasswordText

        if(masterPasswordText.isNotEmpty() && confirmationPasswordText.isNotEmpty()) {
            if (!passwordsMatch) {
                binding.confirmationPasswordLayout.helperText = "Passwords do not match!"
            } else {
                binding.confirmationPasswordLayout.helperText = null
            }
        }

    }

    private fun validateEmail(email: String){
        if (email.isEmpty()) {
            binding.emailLayout.error = null
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.emailLayout.error = "Enter a valid email address!"
        } else {
            binding.emailLayout.error = null
        }
    }

}