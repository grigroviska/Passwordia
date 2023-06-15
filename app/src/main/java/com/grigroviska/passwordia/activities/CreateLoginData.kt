package com.grigroviska.passwordia.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.chip.Chip
import com.grigroviska.passwordia.databinding.ActivityCreateLoginDataBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel
import java.util.regex.Pattern

class CreateLoginData : AppCompatActivity(), PasswordGeneratorDialogListener {
    private lateinit var binding: ActivityCreateLoginDataBinding
    private lateinit var loginViewModel: LoginViewModel
    private val selectedCategories = mutableListOf<String>()
    private var selectedLoginData: LoginData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateLoginDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        loginViewModel = ViewModelProvider(this).get(LoginViewModel::class.java)

        val loginId: Int = intent.getIntExtra("loginId", 999999)

        if (loginId != 999999) {
            loginViewModel.allLogin.observe(this@CreateLoginData) { loginDataList ->
                selectedLoginData = loginDataList.find { it.id == loginId }

                selectedLoginData?.let {
                    with(binding) {
                        username.setText(it.userName)
                        alternateUsername.setText(it.alternateUserName)
                        pass.setText(it.password)
                        website.setText(it.website)
                        notes.setText(it.notes)
                        itemName.setText(it.itemName)

                        selectedCategories.addAll(it.category!!.split(", ").map { category -> category.trim() })
                        showSelectedCategories()

                        createLoginData.setOnClickListener {
                            val updatedUserName = username.text.toString().trim()
                            val updatedAlternateUserName = alternateUsername.text.toString().trim()
                            val updatedPassword = pass.text.toString().trim()
                            val updatedWebsite = website.text.toString().trim()
                            val updatedNotes = notes.text.toString().trim()
                            val updatedItemName = itemName.text.toString().trim()

                            if (validateFields(updatedUserName, updatedPassword, updatedWebsite, updatedItemName, selectedCategories) && isValidUrl(updatedWebsite)) {
                                val updatedLoginData = LoginData(
                                    loginId,
                                    updatedUserName,
                                    updatedAlternateUserName,
                                    updatedPassword,
                                    updatedWebsite,
                                    updatedNotes,
                                    updatedItemName,
                                    selectedCategories.joinToString(", ")
                                )
                                loginViewModel.update(updatedLoginData)
                                finish()
                            } else {
                                Toast.makeText(this@CreateLoginData, "Please fill in the required fields!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } ?: Toast.makeText(this@CreateLoginData, "An error has occurred!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.createLoginData.setOnClickListener {
            saveLoginData()
        }

        binding.passwordGenerator.setOnClickListener {
            showPasswordGeneratorDialog()
        }

        binding.categoryButton.setOnClickListener {
            val intent = Intent(this@CreateLoginData, AddCategory::class.java)
            startActivityForResult(intent, CATEGORY_REQUEST_CODE)
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

        if (validateFields(userName, password, website, itemName, selectedCategories) && isValidUrl(website)) {
            val loginData = LoginData(
                selectedLoginData?.id ?: 0,
                userName,
                alternateUserName,
                password,
                website,
                notes,
                itemName,
                selectedCategories.joinToString(", ")
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
        selectedCategories: MutableList<String>
    ): Boolean {
        return userName.isNotEmpty() && password.isNotEmpty() && website.isNotEmpty() && itemName.isNotEmpty() && selectedCategories.isNotEmpty()
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


    companion object {
        const val CATEGORY_REQUEST_CODE = 1001
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CATEGORY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            val selectedCategory = data?.getStringExtra("selectedCategory")
            selectedCategory?.let {
                addChip(it)
            }
        }
    }

    private fun addChip(category: String) {
        val isCategorySelected = selectedCategories.contains(category)
        if (!isCategorySelected) {
            val chip = Chip(this)
            chip.text = category
            chip.isCloseIconVisible = true
            chip.setOnCloseIconClickListener {
                binding.chipGroup.removeView(chip)
                selectedCategories.remove(category)
            }
            binding.chipGroup.addView(chip)
            selectedCategories.add(category)
        }
    }

    private fun showSelectedCategories() {
        binding.chipGroup.removeAllViews()
        selectedCategories.clear()
        val categories = selectedLoginData?.category?.split(", ")?.map { it.trim() } ?: emptyList()
        for (category in categories) {
            addChip(category)
        }
    }

}
