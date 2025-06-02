package com.grigroviska.passwordia.activities

import android.content.Context
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

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)
        loadHiddenTagSettings()

        setupViewModel()
        setupChipGroupObserver()
        setupClickListeners()
    }

    private fun setupViewModel() {
        try {
            viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
            viewModel.allLogin.observe(this) { loginData ->
                originalLoginDataList = loginData
                applyFilters()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "ViewModel Hatası: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            Log.e("HomeActivity_VM", "ViewModel Başlatma Hatası", e)
        }
    }

    private fun setupChipGroupObserver() {
        viewModel.allCategories.observe(this) { categories ->
            Log.d("HomeActivity_Chip", "Kategoriler alındı: $categories")
            repopulateChipGroup(categories)
        }
    }

    private fun repopulateChipGroup(categories: List<String>) {
        Log.d("HomeActivity_Chip", "ChipGroup yeniden dolduruluyor. Mevcut kategoriler: $categories")
        val chipGroup = binding.filterChipGroup
        chipGroup.setOnCheckedChangeListener(null)
        chipGroup.isSingleSelection = true // BURADA EKLENDİ: Tekli seçim modunu etkinleştir

        val previouslyCheckedChipId = chipGroup.checkedChipId
        var previouslyCheckedChipText: String? = null
        if (previouslyCheckedChipId != View.NO_ID) {
            previouslyCheckedChipText = chipGroup.findViewById<Chip>(previouslyCheckedChipId)?.text?.toString()
        }
        if (previouslyCheckedChipText == null && chipGroup.childCount > 0) {
            previouslyCheckedChipText = allCategoriesChipText
        }
        Log.d("HomeActivity_Chip", "Önceden seçili chip metni: $previouslyCheckedChipText")

        chipGroup.removeAllViews()

        val lowerHiddenTagsForChipPopulation = if (isHideTagFeatureEnabled) {
            hiddenTags
        } else {
            emptySet()
        }

        val allChip = createChip(allCategoriesChipText)
        chipGroup.addView(allChip)
        Log.d("HomeActivity_Chip", "'Tümü' chip'i eklendi.")

        val visibleCategories = categories.filter { category ->
            !isHideTagFeatureEnabled || !lowerHiddenTagsForChipPopulation.contains(category.lowercase())
        }
        Log.d("HomeActivity_Chip", "Chipler için görünür kategoriler: $visibleCategories")

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
                    Log.d("HomeActivity_Chip", "Önceki seçim geri yüklendi: ${chip?.text}")
                    break
                }
            }
        }

        if (!foundAndCheckedPrevious && chipGroup.childCount > 0) {
            (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
            Log.d("HomeActivity_Chip", "Önceki seçim bulunamadı veya geri yüklenmedi, ilk chip seçiliyor: ${(chipGroup.getChildAt(0) as? Chip)?.text}")
        } else if (chipGroup.childCount == 0) {
            Log.w("HomeActivity_Chip", "ChipGroup beklenmedik şekilde boş.")
        }

        chipGroup.setOnCheckedChangeListener { group, checkedId ->
            Log.d("HomeActivity_Chip", "ChipGroup onCheckedChange dinleyicisi tetiklendi. CheckedId: $checkedId")
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
                Log.d("HomeActivity_Search", "onQueryTextSubmit: $query")
                applyFilters()
                searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                Log.d("HomeActivity_Search", "onQueryTextChange - newText: $newText, isNullOrEmpty: ${newText.isNullOrEmpty()}")
                if (!newText.isNullOrEmpty()) {
                    Log.d("HomeActivity_Search", "Metin girişi nedeniyle ChipGroup gizleniyor.")
                    binding.filterChipGroup.visibility = View.GONE
                } else {
                    if (!searchView.hasFocus()) {
                        Log.d("HomeActivity_Search", "Metin boş ve arama kutusu odaklı değil, ChipGroup gösteriliyor.")
                        binding.filterChipGroup.visibility = View.VISIBLE
                    } else {
                        Log.d("HomeActivity_Search", "Metin boş ama arama kutusu odaklı, ChipGroup görünürlüğü şimdilik değişmiyor.")
                    }
                }
                applyFilters()
                return true
            }
        })

        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            Log.d("HomeActivity_Search", "onFocusChange - hasFocus: $hasFocus, query: '${searchView.query}'")
            if (hasFocus) {
                Log.d("HomeActivity_Search", "Odaklandı, ChipGroup gizleniyor ve overlay gösteriliyor.")
                binding.filterChipGroup.visibility = View.GONE
                overlayView.visibility = View.VISIBLE
            } else {
                Log.d("HomeActivity_Search", "Odak kaybedildi.")
                overlayView.visibility = View.GONE
                if (searchView.query.isNullOrEmpty()) {
                    Log.d("HomeActivity_Search", "Sorgu boş, ChipGroup gösteriliyor.")
                    binding.filterChipGroup.visibility = View.VISIBLE
                }
                applyFilters()
            }
        }

        searchView.setOnCloseListener {
            Log.d("HomeActivity_Search", "SearchView onClose tetiklendi.")
            false
        }

        overlayView.setOnClickListener {
            Log.d("HomeActivity_Overlay", "Overlay tıklandı.")
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
        Log.d("HomeActivity_Lifecycle", "onResume çağrıldı.")
        val oldHiddenTags = hiddenTags.toSet()
        val oldIsHideTagFeatureEnabled = isHideTagFeatureEnabled

        loadHiddenTagSettings()

        if (oldHiddenTags != hiddenTags || oldIsHideTagFeatureEnabled != isHideTagFeatureEnabled) {
            Log.d("HomeActivity_Lifecycle", "Ayarlar değişti, chipler yeniden dolduruluyor.")
            viewModel.allCategories.value?.let { currentCategories ->
                repopulateChipGroup(currentCategories)
            } ?: applyFilters()
        } else {
            Log.d("HomeActivity_Lifecycle", "Ayarlar değişmedi, filtreler uygulanıyor.")
            applyFilters()
        }

        if (clicked) {
            closeFabMenu()
        }
        if (!searchView.hasFocus() && searchView.query.isNullOrEmpty()) {
            Log.d("HomeActivity_Lifecycle", "onResume: SearchView odaklı değil ve sorgu boş, ChipGroup görünür yapılıyor.")
            binding.filterChipGroup.visibility = View.VISIBLE
        }
    }

    private fun loadHiddenTagSettings() {
        isHideTagFeatureEnabled = prefs.getBoolean("hide_tag_enabled", false)
        hiddenTags = prefs.getStringSet("hidden_tags", emptySet())?.map { it.lowercase() }?.toSet() ?: emptySet()
        Log.d("HomeActivity_Settings", "Ayarlar yüklendi - HideFeatureEnabled: $isHideTagFeatureEnabled, HiddenTags: $hiddenTags")
    }

    private fun applyFilters() {
        Log.d("HomeActivity_Filter", "applyFilters çağrıldı. ChipGroup görünürlüğü: ${binding.filterChipGroup.visibility}, Görünür mü: ${binding.filterChipGroup.visibility == View.VISIBLE}")
        var filteredList = originalLoginDataList
        val isChipGroupVisible = binding.filterChipGroup.visibility == View.VISIBLE

        if (isHideTagFeatureEnabled && hiddenTags.isNotEmpty()) {
            Log.d("HomeActivity_Filter", "Gizli etiket filtresi uygulanıyor. Gizli etiketler: $hiddenTags")
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
            Log.d("HomeActivity_Filter", "ChipGroup görünür. CheckedChipId: $checkedChipId")

            if (checkedChipId != View.NO_ID && checkedChipId != -1) {
                val checkedChip = chipGroup.findViewById<Chip>(checkedChipId)
                if (checkedChip != null) {
                    val selectedChipText = checkedChip.text?.toString()
                    Log.d("HomeActivity_Filter", "Seçili chip metni: $selectedChipText")

                    if (selectedChipText != null && !selectedChipText.equals(allCategoriesChipText, ignoreCase = true)) {
                        if (!isHideTagFeatureEnabled || !hiddenTags.contains(selectedChipText.lowercase())) {
                            categoryToFilterBy = selectedChipText
                            Log.d("HomeActivity_Filter", "Kategoriye göre filtreleniyor: $categoryToFilterBy")
                        } else {
                            Log.w("HomeActivity_Filter", "Gizli bir etiket ($selectedChipText) filtre olarak kullanılmaya çalışıldı. 'Tümü'ne dönülüyor.")
                            if (chipGroup.childCount > 0 && chipGroup.getChildAt(0).id != checkedChipId) {
                                (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
                            }
                        }
                    } else {
                        Log.d("HomeActivity_Filter", "'Tümü' chip'i seçili veya selectedChipText null.")
                    }
                } else {
                    Log.w("HomeActivity_Filter", "ID $checkedChipId olan seçili chip ChipGroup içinde bulunamadı.")
                }
            } else if (chipGroup.childCount > 0 && (checkedChipId == View.NO_ID || checkedChipId == -1)) {
                Log.d("HomeActivity_Filter", "Chip seçili değil veya geçersiz ID, 'Tümü' (ilk chip) seçili olduğundan emin olunuyor.")
                (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
            }

            if (categoryToFilterBy != null) {
                filteredList = filteredList.filter { loginData ->
                    val match = loginData.category.equals(categoryToFilterBy, ignoreCase = true)
                    match
                }
            }
        } else {
            Log.d("HomeActivity_Filter", "ChipGroup görünür DEĞİL. Kategori filtresi atlanıyor.")
        }

        val currentQuery = searchView.query?.toString()
        if (!currentQuery.isNullOrBlank()) {
            val queryLower = currentQuery.lowercase()
            Log.d("HomeActivity_Filter", "Arama sorgusuna göre filtreleniyor: $queryLower")
            filteredList = filteredList.filter { loginData ->
                val isItemNameMatch = loginData.itemName?.lowercase()?.contains(queryLower) ?: false
                val isUserNameMatch = loginData.userName?.lowercase()?.contains(queryLower) ?: false
                val isWebsiteMatch = loginData.website?.lowercase()?.contains(queryLower) ?: false
                val isAuthenticatorMatch = loginData.accountName?.lowercase()?.contains(queryLower) ?: false
                isItemNameMatch || isUserNameMatch || isWebsiteMatch || isAuthenticatorMatch
            }
        }

        if (adapter == null) {
            adapter = LoginDataAdapter(filteredList)
            binding.dataList.layoutManager = LinearLayoutManager(this)
            binding.dataList.adapter = adapter
            Log.d("HomeActivity_Filter", "Liste boyutu ile adaptör oluşturuldu: ${filteredList.size}")
        } else {
            adapter?.setData(filteredList)
            Log.d("HomeActivity_Filter", "Adaptör verileri liste boyutu ile güncellendi: ${filteredList.size}")
        }

        val isFinalListEmpty = filteredList.isEmpty()
        val isSearchCurrentlyActive = !currentQuery.isNullOrBlank()
        val isSpecificCategoryCurrentlySelected = isChipGroupVisible && categoryToFilterBy != null

        if (isFinalListEmpty) {
            binding.dataList.visibility = View.GONE
            binding.homeImage.visibility = View.VISIBLE
            binding.noData.visibility = View.VISIBLE
            Log.d("HomeActivity_Filter", "Nihai liste boş. Arama aktif: $isSearchCurrentlyActive, Kategori seçili: $isSpecificCategoryCurrentlySelected")

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
            Log.d("HomeActivity_Filter", "Nihai liste boş DEĞİL. Boyut: ${filteredList.size}")
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