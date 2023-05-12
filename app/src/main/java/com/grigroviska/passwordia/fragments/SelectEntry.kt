package com.grigroviska.passwordia.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.activities.MainActivity
import com.grigroviska.passwordia.databinding.FragmentSelectEntryBinding
import com.grigroviska.passwordia.fragments.SignInPassword

class SelectEntry : Fragment() {

    private lateinit var binding : FragmentSelectEntryBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSelectEntryBinding.inflate(inflater, container, false)
        val view = binding.root

        val manuelEntryRadioButton: RadioButton = binding.manuelEntry
        val biometricEntryRadioButton: RadioButton = binding.biometricEntry

        sharedPreferences = requireContext().getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)

        val selectedRadioButtonBorder = resources.getDrawable(R.drawable.selected_radio_button_border)

        manuelEntryRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.background = selectedRadioButtonBorder
                saveEntryType("manuel")
            } else {
                buttonView.background = null
            }
        }

        biometricEntryRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                buttonView.background = selectedRadioButtonBorder
                saveEntryType("biometric")
            } else {
                buttonView.background = null
            }
        }

        binding.continueButton.setOnClickListener {

            binding.continueButton.setOnClickListener {
                val isManuelEntrySelected = manuelEntryRadioButton.isChecked
                val isBiometricEntrySelected = biometricEntryRadioButton.isChecked

                if (isManuelEntrySelected || isBiometricEntrySelected) {

                    if (isManuelEntrySelected) {

                        val signInPasswordFragment = SignInPassword()
                        requireActivity().supportFragmentManager.beginTransaction()
                            .replace(R.id.frameLayout, signInPasswordFragment)
                            .addToBackStack(null)
                            .commit()

                    } else {
                        val goToHome = Intent(requireContext(), MainActivity::class.java)
                        startActivity(goToHome)
                        requireActivity().finish()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please select an entry option", Toast.LENGTH_SHORT).show()
                }
            }

        }

        return view
    }

    private fun saveEntryType(entryType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("entry_type", entryType)
        editor.apply()
    }

    companion object {
        fun newInstance(): Fragment {
            return Fragment()
        }
    }
}
