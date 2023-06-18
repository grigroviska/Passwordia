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
import androidx.biometric.BiometricManager
import androidx.navigation.NavController
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.activities.MainActivity
import com.grigroviska.passwordia.databinding.FragmentSelectEntryBinding

class SelectEntry : Fragment() {

    private lateinit var binding : FragmentSelectEntryBinding
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var navController : NavController
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentSelectEntryBinding.inflate(inflater, container, false)
        val view = binding.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        auth = FirebaseAuth.getInstance()
        val currentUserEmail = auth.currentUser!!.email.toString()

        navController = Navigation.findNavController(view)

        val manuelEntryRadioButton: RadioButton = binding.manuelEntry
        val biometricEntryRadioButton: RadioButton = binding.biometricEntry
        val biometricSupported = checkBiometricSupport()
        biometricEntryRadioButton.isEnabled = biometricSupported

        sharedPreferences = requireContext().getSharedPreferences("Passwordia.EntryType", Context.MODE_PRIVATE)

        manuelEntryRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                saveEntryType("manuel")
            }
        }


        biometricEntryRadioButton.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                saveEntryType("biometric")
            }
        }

        binding.continueButton.setOnClickListener {

            binding.continueButton.setOnClickListener {
                val isManuelEntrySelected = manuelEntryRadioButton.isChecked
                val isBiometricEntrySelected = biometricEntryRadioButton.isChecked

                if (isManuelEntrySelected || isBiometricEntrySelected) {

                    if (isManuelEntrySelected) {

                        val action = SelectEntryDirections.actionSelectEntryToSignInPassword(currentUserEmail)
                        navController.navigate(action)

                    } else {
                        val intent = Intent(requireContext(), MainActivity::class.java)
                        startActivity(intent)
                    }
                } else {
                    Toast.makeText(requireContext(), getString(R.string.please_select_an_entry_option), Toast.LENGTH_SHORT).show()
                }
            }

        }


    }

    private fun saveEntryType(entryType: String) {
        val editor = sharedPreferences.edit()
        editor.putString("entry_type", entryType)
        editor.apply()
    }

    private fun checkBiometricSupport(): Boolean {
        val biometricManager = BiometricManager.from(requireContext())
        val canAuthenticate = biometricManager.canAuthenticate()
        return canAuthenticate == BiometricManager.BIOMETRIC_SUCCESS
    }

}
