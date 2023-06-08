package com.grigroviska.passwordia.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.adapter.loginDataAdapter
import com.grigroviska.passwordia.databinding.ActivityHomeBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: loginDataAdapter
    private var loginDataList: List<LoginData> = emptyList()
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()


        try{
            adapter = loginDataAdapter(emptyList())
            viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
            viewModel.allLogin.observe(this) { loginData ->
                loginDataList = loginData
                adapter = loginDataAdapter(loginDataList)
                binding.dataList.adapter = adapter
                binding.dataList.layoutManager = LinearLayoutManager(this)
                adapter.notifyDataSetChanged()
            }

        }catch (e: Exception){

            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

        }


        binding.fab.setOnClickListener {
            val intent = Intent(this, CreateLoginData::class.java)
            startActivity(intent)
        }
    }
}