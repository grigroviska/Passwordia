package com.grigroviska.passwordia.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.fragments.SignInEmail
import com.grigroviska.passwordia.fragments.SignInPasswordDirections

class SignMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_main)

        this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
    }
}