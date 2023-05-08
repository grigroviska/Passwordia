package com.grigroviska.passwordia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class SignMain : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_main)

        val fragment = SignEmailFragment.newInstance()

        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()
    }
}