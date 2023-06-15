package com.grigroviska.passwordia.activities

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.grigroviska.passwordia.databinding.ActivityAddCategoryBinding

class AddCategory : AppCompatActivity() {

    private lateinit var binding: ActivityAddCategoryBinding
    private lateinit var chipGroup: ChipGroup
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chipGroup = binding.chipGroup

        sharedPreferences = getSharedPreferences("categories", Context.MODE_PRIVATE)
        createDefaultChips()

        setupInputCategory()
        setupCreateCategoryButton()
        setupBeingWritten()

        binding.beingWritten.setOnClickListener {
            addCategoryFromInput()
        }
    }

    private fun setupInputCategory() {
        binding.inputCategory.setOnKeyListener { _, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
                val input = binding.inputCategory.text.toString().trim()
                if (input.isNotEmpty()) {
                    if (!isCategoryExist(input)) {
                        addChip(input)
                    }
                    binding.inputCategory.text = null
                    return@setOnKeyListener true
                }
            }
            return@setOnKeyListener false
        }

        binding.inputCategory.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.beingWritten.text = s
            }

            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().trim()
                if (input.isNotEmpty()) {
                    showCreateChip()
                } else {
                    hideCreateChip()
                }
            }
        })
    }

    private fun setupCreateCategoryButton() {
        binding.createCategory.setOnClickListener {
            addCategoryFromInput()
        }
    }

    private fun setupBeingWritten() {
        binding.beingWritten.setOnClickListener {
            addCategoryFromInput()
        }
    }

    private fun addCategoryFromInput() {
        val input = binding.inputCategory.text.toString().trim()
        if (input.isNotEmpty()) {
            if (!isCategoryExist(input)) {
                addChip(input)
            }
            binding.inputCategory.text = null

            val intent = Intent()
            intent.putExtra("selectedCategory", input)
            setResult(Activity.RESULT_OK, intent)
            finish()
        }
    }

    private fun createDefaultChips() {
        val defaultCategories = listOf("Business", "Entertainment", "Games", "Shopping", "Social")
        val categories = sharedPreferences.getStringSet("categories", mutableSetOf())

        categories?.addAll(defaultCategories)

        sharedPreferences.edit().putStringSet("categories", categories).apply()

        categories?.forEach { category ->
            val isDefault = defaultCategories.contains(category)
            createChip(category, isDefault)
        }
    }

    private fun addChip(text: String) {
        val chip = Chip(this)
        chip.text = text

        chipGroup.addView(chip)
        saveCategory(text)

        val intent = Intent()
        intent.putExtra("selectedCategory", text)
        setResult(Activity.RESULT_OK, intent)
    }

    private fun createChip(text: String, isDefault: Boolean) {
        val chip = Chip(this)
        chip.text = text
        chip.isCloseIconVisible = !isDefault
        chipGroup.addView(chip)

        if (!isDefault) {
            chip.setOnCloseIconClickListener {
                val selectedCategory = chip.text.toString()
                removeChip(selectedCategory)
                chipGroup.removeView(chip)
            }
        }

        chip.setOnClickListener {
            val selectedCategory = chip.text.toString()
            val intent = Intent()
            intent.putExtra("selectedCategory", selectedCategory)
            setResult(Activity.RESULT_OK, intent)
            saveCategory(selectedCategory)
            finish()
        }
    }

    private fun showCreateChip() {
        binding.searchChipGroup.visibility = View.VISIBLE
        binding.createCategory.visibility = View.VISIBLE
        binding.chipGroup.visibility = View.GONE
    }

    private fun hideCreateChip() {
        binding.searchChipGroup.visibility = View.GONE
        binding.createCategory.visibility = View.GONE
        binding.chipGroup.visibility = View.VISIBLE
    }

    private fun saveCategory(category: String) {
        val categories = sharedPreferences.getStringSet("categories", mutableSetOf())
        categories?.add(category)
        sharedPreferences.edit().putStringSet("categories", categories).apply()
    }

    private fun removeChip(text: String) {
        val categories = sharedPreferences.getStringSet("categories", mutableSetOf())
        categories?.remove(text)
        sharedPreferences.edit().putStringSet("categories", categories).apply()
    }

    private fun isCategoryExist(category: String): Boolean {
        val categories = sharedPreferences.getStringSet("categories", mutableSetOf())
        return categories?.contains(category) == true
    }
}
