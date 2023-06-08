package com.grigroviska.passwordia.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.grigroviska.passwordia.databinding.ActivityCreateLoginDataBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel

class CreateLoginData : AppCompatActivity() {
    private lateinit var binding: ActivityCreateLoginDataBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateLoginDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loginViewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        binding.createLoginData.setOnClickListener {
            if (::loginViewModel.isInitialized) {
                saveLoginData()
            }
        }
    }

    private fun saveLoginData() {
        try {

            val userName = binding.username.text.toString().trim()
            val alternateUserName = binding.alternateUsername.text.toString().trim()
            val password = binding.pass.text.toString().trim()
            val website = binding.website.text.toString().trim()
            val notes = binding.notes.text.toString().trim()
            val itemName = binding.itemName.text.toString().trim()
            val category = binding.category.text.toString().trim()

            if (validateFields(userName, password, website, itemName, category)) {
                val loginData = LoginData(
                    0, // Geçici olarak 0 değerini kullanarak otomatik olarak artan bir ID atayabilirsiniz
                    userName,
                    alternateUserName,
                    password,
                    website,
                    notes,
                    itemName,
                    category
                )

                loginViewModel.insert(loginData)

                Toast.makeText(this, "Veri başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
                println(loginData)

            } else {
                Toast.makeText(this, "Lütfen gerekli alanları doldurun", Toast.LENGTH_SHORT).show()
            }

        }catch (e: Exception){

            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

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
}
