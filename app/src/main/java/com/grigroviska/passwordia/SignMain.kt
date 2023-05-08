package com.grigroviska.passwordia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.grigroviska.passwordia.Fragments.SignInEmail

class SignMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_main)

        val fragment = SignInEmail.newInstance()

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}