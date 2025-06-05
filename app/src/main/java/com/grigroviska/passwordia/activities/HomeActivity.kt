package com.grigroviska.passwordia.activities

import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.R
import com.grigroviska.passwordia.adapter.LoginDataAdapter
import com.grigroviska.passwordia.databinding.ActivityHomeBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel

class HomeActivity : AppCompatActivity(), ViewModelStoreOwner {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private var adapter: LoginDataAdapter? = null
    private lateinit var viewModel: LoginViewModel
    private lateinit var searchView: SearchView
    private lateinit var overlayView: View
    private var originalLoginDataList: List<LoginData> = emptyList()
    private lateinit var prefs: SharedPreferences
    private var hiddenTags: Set<String> = emptySet()
    private var isHideTagFeatureEnabled: Boolean = false
    private lateinit var loginDataAdapter: LoginDataAdapter

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    private var clicked = false

    private val chipSelectedColor: Int by lazy { ContextCompat.getColor(this, R.color.backgroundDarkColor) }
    private val chipUnselectedColor: Int by lazy { ContextCompat.getColor(this, R.color.progressBarColor) }
    private val chipTextColor: Int by lazy { ContextCompat.getColor(this, R.color.white) }
    private val chipColorStateList: ColorStateList by lazy {
        ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf(-android.R.attr.state_checked)
            ),
            intArrayOf(chipSelectedColor, chipUnselectedColor)
        )
    }
    private val allCategoriesChipText: String by lazy { getString(R.string.label_all) }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        searchView = binding.searchBox
        overlayView = binding.overlayView

        prefs = getSharedPreferences("settings", MODE_PRIVATE)
        loadHiddenTagSettings()

        setupViewModel()
        setupChipGroupObserver()
        setupClickListeners()
    }

    private fun setupViewModel() {
        try {
            viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
            loginDataAdapter = LoginDataAdapter(emptyList()) { loginData ->
                viewModel.updateFavoriteStatus(loginData)
            }
            viewModel.allLogin.observe(this) { loginData ->
                originalLoginDataList = loginData
                val currentCategories = viewModel.allCategories.value ?: emptyList()
                repopulateChipGroup(currentCategories)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "ViewModel Hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show() }
    }

    private fun setupChipGroupObserver() {
        viewModel.allCategories.observe(this) { categories ->
            repopulateChipGroup(categories)
        }
    }

    private fun repopulateChipGroup(categories: List<String>) {
        val chipGroup = binding.filterChipGroup
        chipGroup.setOnCheckedChangeListener(null)
        chipGroup.isSingleSelection = true

        val previouslyCheckedChipId = chipGroup.checkedChipId
        var previouslyCheckedChipText: String? = null
        if (previouslyCheckedChipId != View.NO_ID) {
            previouslyCheckedChipText = chipGroup.findViewById<Chip>(previouslyCheckedChipId)?.text?.toString()
        }

        chipGroup.removeAllViews()

        if (originalLoginDataList.isEmpty()) {
            chipGroup.visibility = View.GONE
            applyFilters()
            return
        } else {
            chipGroup.visibility = View.VISIBLE
        }

        if (previouslyCheckedChipText == null && chipGroup.childCount == 0 && originalLoginDataList.isNotEmpty()) {
            previouslyCheckedChipText = allCategoriesChipText
        }

        val lowerHiddenTagsForChipPopulation = if (isHideTagFeatureEnabled) {
            hiddenTags
        } else {
            emptySet()
        }

        val allChip = createChip(allCategoriesChipText)
        chipGroup.addView(allChip)
        val visibleCategories = categories.filter { category ->
            !isHideTagFeatureEnabled || !lowerHiddenTagsForChipPopulation.contains(category.lowercase())
        }

        visibleCategories.forEach { category ->
            chipGroup.addView(createChip(category))
        }

        var foundAndCheckedPrevious = false
        if (previouslyCheckedChipText != null) {
            for (i in 0 until chipGroup.childCount) {
                val chip = chipGroup.getChildAt(i) as? Chip
                if (chip?.text.toString().equals(previouslyCheckedChipText, ignoreCase = true)) {
                    chip?.isChecked = true
                    foundAndCheckedPrevious = true
                    break
                }
            }
        }

        if (!foundAndCheckedPrevious && chipGroup.childCount > 0) {
            (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
        } else if (chipGroup.childCount == 0 && originalLoginDataList.isNotEmpty()) {
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            applyFilters()
        }
        applyFilters()
    }

    private fun createChip(text: String): Chip {
        return Chip(this).apply {
            this.text = text
            isCheckable = true
            chipBackgroundColor = chipColorStateList
            setTextColor(chipTextColor)
            this.id = View.generateViewId()
        }
    }

    private fun setupClickListeners() {
        binding.fab.setOnClickListener {
            onAddButtonClicked()
        }

        binding.createLogin.setOnClickListener {
            startActivity(Intent(this, CreateLoginData::class.java))
            closeFabMenu()
        }

        binding.createAuthenticator.setOnClickListener {
            startActivity(Intent(this, CreateAuthenticator::class.java))
            closeFabMenu()
        }

        binding.loginText.setOnClickListener {
            binding.createLogin.performClick()
        }

        binding.authText.setOnClickListener {
            binding.createAuthenticator.performClick()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                applyFilters()
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (!newText.isNullOrEmpty()) {
                    binding.filterChipGroup.visibility = View.GONE
                } else {
                    if (!searchView.hasFocus()) {
                        binding.filterChipGroup.visibility = View.VISIBLE
                    } else {
                    }
                }
                applyFilters()
                return true
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            Log.d("HomeActivity_Search", "onFocusChange - hasFocus: $hasFocus, query: '${searchView.query}'")
            if (hasFocus) {
                binding.filterChipGroup.visibility = View.GONE
                overlayView.visibility = View.VISIBLE
            } else {
                overlayView.visibility = View.GONE
                if (searchView.query.isNullOrEmpty()) {
                    binding.filterChipGroup.visibility = View.VISIBLE
                }
                applyFilters()
            }
        }

        searchView.setOnCloseListener {
            false
        }

        overlayView.setOnClickListener {
            if (clicked) {
                onAddButtonClicked()
            }
            if (searchView.hasFocus()) {
                searchView.setQuery("", false)
                searchView.clearFocus()
            }
        }

        binding.menuButton.setOnClickListener {
            showBottomSheetMenu()
        }
    }

    private fun closeFabMenu() {
        if (clicked) {
            setAnimation(open = false)
            setVisibility(visible = false)
            clicked = false
        }
    }

    private fun showBottomSheetMenu() {
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
        val bottomSheetDialog = BottomSheetDialog(this)
        bottomSheetDialog.setContentView(bottomSheetView)

        bottomSheetView.findViewById<LinearLayout>(R.id.homeLayout)?.setOnClickListener {
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<LinearLayout>(R.id.generateLayout)?.setOnClickListener {
            startActivity(Intent(this, Generator::class.java))
            bottomSheetDialog.dismiss()
        }
        bottomSheetView.findViewById<LinearLayout>(R.id.settingsLayout)?.setOnClickListener {
            startActivity(Intent(this, SettingsMenu::class.java))
            bottomSheetDialog.dismiss()
        }
        bottomSheetDialog.show()
    }

    private fun onAddButtonClicked() {
        val willOpen = !clicked
        setAnimation(open = willOpen)
        setVisibility(visible = willOpen)
        clicked = willOpen
    }

    override fun onResume() {
        super.onResume()
        val oldHiddenTags = hiddenTags.toSet()
        val oldIsHideTagFeatureEnabled = isHideTagFeatureEnabled

        loadHiddenTagSettings()

        if (oldHiddenTags != hiddenTags || oldIsHideTagFeatureEnabled != isHideTagFeatureEnabled) {
            viewModel.allCategories.value?.let { currentCategories ->
                repopulateChipGroup(currentCategories)
            } ?: applyFilters()
        } else {
            applyFilters()
        }

        if (clicked) {
            closeFabMenu()
        }
        if (!searchView.hasFocus() && searchView.query.isNullOrEmpty()) {
            binding.filterChipGroup.visibility = View.VISIBLE
        }
    }

    private fun loadHiddenTagSettings() {
        isHideTagFeatureEnabled = prefs.getBoolean("hide_tag_enabled", false)
        hiddenTags = prefs.getStringSet("hidden_tags", emptySet())?.map { it.lowercase() }?.toSet() ?: emptySet()
    }

    private fun applyFilters() {
        var filteredList = originalLoginDataList
        val isChipGroupVisible = binding.filterChipGroup.visibility == View.VISIBLE

        if (isHideTagFeatureEnabled && hiddenTags.isNotEmpty()) {
            filteredList = filteredList.filter { loginData ->
                val itemTags = loginData.category?.split(",")
                    ?.map { it.trim().lowercase() }
                    ?.filter { it.isNotEmpty() }
                    ?.toSet() ?: emptySet()
                val hiddenMatch = itemTags.intersect(hiddenTags).isEmpty()
                hiddenMatch
            }
        }

        var categoryToFilterBy: String? = null
        if (isChipGroupVisible) {
            val chipGroup = binding.filterChipGroup
            val checkedChipId = chipGroup.checkedChipId

            if (checkedChipId != View.NO_ID && checkedChipId != -1) {
                val checkedChip = chipGroup.findViewById<Chip>(checkedChipId)
                if (checkedChip != null) {
                    val selectedChipText = checkedChip.text?.toString()

                    if (selectedChipText != null && !selectedChipText.equals(allCategoriesChipText, ignoreCase = true)) {
                        if (!isHideTagFeatureEnabled || !hiddenTags.contains(selectedChipText.lowercase())) {
                            categoryToFilterBy = selectedChipText
                        } else {
                            if (chipGroup.childCount > 0 && chipGroup.getChildAt(0).id != checkedChipId) {
                                (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
                            }
                        }
                    } else {
                    }
                } else {
                }
            } else if (chipGroup.childCount > 0 && (checkedChipId == View.NO_ID || checkedChipId == -1)) {
                (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
            }

            if (categoryToFilterBy != null) {
                filteredList = filteredList.filter { loginData ->
                    val match = loginData.category.equals(categoryToFilterBy, ignoreCase = true)
                    match
                }
            }
        } else {
        }

        val currentQuery = searchView.query?.toString()
        if (!currentQuery.isNullOrBlank()) {
            val queryLower = currentQuery.lowercase()
            filteredList = filteredList.filter { loginData ->
                val isItemNameMatch = loginData.itemName?.lowercase()?.contains(queryLower) ?: false
                val isUserNameMatch = loginData.userName?.lowercase()?.contains(queryLower) ?: false
                val isWebsiteMatch = loginData.website?.lowercase()?.contains(queryLower) ?: false
                val isAuthenticatorMatch = loginData.accountName?.lowercase()?.contains(queryLower) ?: false
                isItemNameMatch || isUserNameMatch || isWebsiteMatch || isAuthenticatorMatch
            }
        }

        if (adapter == null) {
            adapter = loginDataAdapter
            binding.dataList.layoutManager = LinearLayoutManager(this)
            binding.dataList.adapter = adapter
        }
        adapter?.setData(filteredList)

        val isFinalListEmpty = filteredList.isEmpty()
        val isSearchCurrentlyActive = !currentQuery.isNullOrBlank()
        val isSpecificCategoryCurrentlySelected = isChipGroupVisible && categoryToFilterBy != null

        if (isFinalListEmpty) {
            binding.dataList.visibility = View.GONE
            binding.homeImage.visibility = View.VISIBLE
            binding.noData.visibility = View.VISIBLE

            when {
                isSearchCurrentlyActive && isSpecificCategoryCurrentlySelected -> {
                    binding.noData.text = getString(R.string.no_data)
                }
                isSearchCurrentlyActive -> {
                    binding.noData.text = getString(R.string.no_data)
                }
                isSpecificCategoryCurrentlySelected -> {
                    binding.noData.text = getString(R.string.no_data)
                }
                else -> {
                    binding.noData.text = getString(R.string.no_data)
                }
            }
        } else {
            binding.dataList.visibility = View.VISIBLE
            binding.homeImage.visibility = View.GONE
            binding.noData.visibility = View.GONE
        }
    }

    private fun setAnimation(open: Boolean) {
        val fabAnimation = if (open) rotateOpen else rotateClose
        val itemAnimation = if (open) fromBottom else toBottom

        binding.fab.startAnimation(fabAnimation)
        binding.createLogin.startAnimation(itemAnimation)
        binding.createAuthenticator.startAnimation(itemAnimation)
    }

    private fun setVisibility(visible: Boolean) {
        val visibilityState = if (visible) View.VISIBLE else View.INVISIBLE
        overlayView.visibility = if (visible && clicked) View.VISIBLE else View.GONE
        binding.createLogin.visibility = visibilityState
        binding.createAuthenticator.visibility = visibilityState
        binding.loginText.visibility = visibilityState
        binding.authText.visibility = visibilityState
    }
}