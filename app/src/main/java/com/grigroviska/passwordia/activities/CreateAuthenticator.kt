package com.grigroviska.passwordia.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.databinding.ActivityCreateAuthenticatorBinding
import com.grigroviska.passwordia.databinding.ActivityMainBinding

class CreateAuthenticator : AppCompatActivity() {

    private lateinit var binding : ActivityCreateAuthenticatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateAuthenticatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        binding.saveData.setOnClickListener {



        }

    }
}