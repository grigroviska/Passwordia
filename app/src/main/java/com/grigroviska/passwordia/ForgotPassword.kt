package com.grigroviska.passwordia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.databinding.ActivityForgotPasswordBinding

class ForgotPassword : AppCompatActivity() {

    private lateinit var binding : ActivityForgotPasswordBinding

    private lateinit var auth : FirebaseAuth

    private lateinit var email : TextInputEditText
    private lateinit var resetButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        email = binding.email
        resetButton = binding.resetPassword

        val emailFromSignIn = intent.getStringExtra("email")
        email.setText(emailFromSignIn.toString())

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

        resetButton.setOnClickListener {

            // Send password reset email to the user's email address
            FirebaseAuth.getInstance().sendPasswordResetEmail(email.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Password reset email sent successfully
                        Toast.makeText(this, "Password reset email sent.", Toast.LENGTH_SHORT).show()
                    } else {
                        // Failed to send password reset email
                        Toast.makeText(this, "Failed to send password reset email.", Toast.LENGTH_SHORT).show()
                    }
                }

        }

    }
}