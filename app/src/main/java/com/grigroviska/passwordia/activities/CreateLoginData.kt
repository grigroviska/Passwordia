package com.grigroviska.passwordia.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.passwordia.databinding.ActivityCreateLoginDataBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel
import java.util.regex.Pattern

class CreateLoginData : AppCompatActivity(), PasswordGeneratorDialogListener {
    private lateinit var binding: ActivityCreateLoginDataBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateLoginDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        binding.createLoginData.setOnClickListener {
            saveLoginData()
        }

        binding.passwordGenerator.setOnClickListener {
            showPasswordGeneratorDialog()
        }
    }

    override fun onPasswordGenerated(password: String) {
        binding.pass.setText(password)
    }


    private fun saveLoginData() {
        val userName = binding.username.text.toString().trim()
        val alternateUserName = binding.alternateUsername.text.toString().trim()
        val password = binding.pass.text.toString().trim()
        val website = binding.website.text.toString().trim()
        val notes = binding.notes.text.toString().trim()
        val itemName = binding.itemName.text.toString().trim()
        val category = binding.category.text.toString().trim()

        if (validateFields(userName, password, website, itemName, category) && isValidUrl(website)) {
            val loginData = LoginData(
                0,
                userName,
                alternateUserName,
                password,
                website,
                notes,
                itemName,
                category
            )

            loginViewModel.insert(loginData)
            finish()
        } else {
            Toast.makeText(this, "Please fill in the required fields!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateFields(
        userName: String,
        password: String,
        website: String,
        itemName: String,
        category: String
    ): Boolean {
        return userName.isNotEmpty() && password.isNotEmpty() && website.isNotEmpty() && itemName.isNotEmpty() && category.isNotEmpty()
    }

    private fun showPasswordGeneratorDialog() {
        val dialog = PasswordGeneratorDialog()
        dialog.show(supportFragmentManager, "password_generator_dialog")
    }

    private fun isValidUrl(url: String): Boolean {
        val pattern = Pattern.compile(
            "^((http[s]?|ftp):\\/\\/)?"+ // Protocol
                    "([0-9a-zA-Z]+([.-][0-9a-zA-Z]+)*\\.[a-zA-Z]{2,6}|"+ // Domain names
                    "([0-9]{1,3}\\.){3}[0-9]{1,3})"+ // IP addresses
                    "(\\/[a-zA-Z0-9\\/\\.-]*)?"+ // Path and Query
                    "(\\?[a-zA-Z0-9\\-._?,'\\\\/+&amp;%$#=]*)?" // Query parameters
        )
        val isValid = pattern.matcher(url).matches()
        if (!isValid) {
            Toast.makeText(this, "Enter a valid URL!", Toast.LENGTH_SHORT).show()
        }
        return isValid
    }

}
