package com.grigroviska.passwordia.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.grigroviska.passwordia.R


class ConfirmationPassword : Fragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_confirmation_password, container, false)
    }

    companion object {
        fun newInstance(password: String): ConfirmationPassword {
            val fragment = ConfirmationPassword()
            val bundle = Bundle()
            bundle.putString("password", password)
            fragment.arguments = bundle
            return fragment
        }
    }
}