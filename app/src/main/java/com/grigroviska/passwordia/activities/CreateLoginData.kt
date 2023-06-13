package com.grigroviska.passwordia.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
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
        this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        val loginId : Int = intent.getIntExtra("loginId", 999999)

        if (loginId != 999999) {

            try {

                loginViewModel.allLogin.observe(this@CreateLoginData) { loginDataList ->

                    val selectedLoginData: LoginData? = loginDataList.find { it.id == loginId }

                    if (selectedLoginData != null) {

                        binding.username.setText(selectedLoginData.userName)
                        binding.alternateUsername.setText(selectedLoginData.alternateUserName)
                        binding.pass.setText(selectedLoginData.password)
                        binding.website.setText(selectedLoginData.website)
                        binding.notes.setText(selectedLoginData.notes)
                        binding.itemName.setText(selectedLoginData.itemName)
                        binding.category.setText(selectedLoginData.category)

                    } else {
                        Toast.makeText(baseContext, "An error has occurred!", Toast.LENGTH_SHORT).show()
                    }

                    binding.createLoginData.setOnClickListener {

                        val updatedUserName = binding.username.text.toString().trim()
                        val updatedAlternateUserName = binding.alternateUsername.text.toString().trim()
                        val updatedPassword = binding.pass.text.toString().trim()
                        val updatedWebsite = binding.website.text.toString().trim()
                        val updatedNotes = binding.notes.text.toString().trim()
                        val updatedItemName = binding.itemName.text.toString().trim()
                        val updatedCategory = binding.category.text.toString().trim()

                        if (validateFields(updatedUserName, updatedPassword, updatedWebsite, updatedItemName, updatedCategory) && isValidUrl(updatedWebsite)) {

                            val updatedLoginData = LoginData(
                                loginId,
                                updatedUserName,
                                updatedAlternateUserName,
                                updatedPassword,
                                updatedWebsite,
                                updatedNotes,
                                updatedItemName,
                                updatedCategory
                            )

                            loginViewModel.update(updatedLoginData)
                            finish()
                        } else {
                            Toast.makeText(this, "Please fill in the required fields!", Toast.LENGTH_SHORT).show()
                        }

                    }
                }

            }catch (e: Exception){

                Toast.makeText(baseContext, e.localizedMessage, Toast.LENGTH_SHORT).show()

            }


        }

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
