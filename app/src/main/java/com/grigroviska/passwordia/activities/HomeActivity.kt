package com.grigroviska.passwordia.activities

import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
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
    private var loginDataList: List<LoginData> = emptyList()
    private lateinit var viewModel: LoginViewModel
    private lateinit var searchView : SearchView
    private lateinit var overlayView: View

    //Animation
    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        /*this.window.setFlags(
            WindowManager.LayoutParams.FLAG_SECURE,
            WindowManager.LayoutParams.FLAG_SECURE
        )*/



        auth = FirebaseAuth.getInstance()
        searchView = binding.searchBox
        overlayView = binding.overlayView


        try {
            viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
            viewModel.allLogin.observe(this) { loginData ->
                loginDataList = loginData
                if (adapter == null) {
                    adapter = LoginDataAdapter(loginDataList)
                    binding.dataList.adapter = adapter
                    binding.dataList.layoutManager = LinearLayoutManager(this)
                } else {
                    adapter?.setData(loginDataList)
                }

                updateUIVisibility(loginData.isEmpty())
            }
        } catch (e: Exception) {
            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()
        }


        val chipGroup = binding.filterChipGroup


        val selectedColor = ContextCompat.getColor(this, R.color.backgroundDarkColor)
        val unselectedColor = ContextCompat.getColor(this, R.color.progressBarColor)
        val textColor = ContextCompat.getColor(this, R.color.white) // Metin rengi

        // Chip durumları için ColorStateList oluştur
        val chipColorStateList = ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked), // Seçili durum
                intArrayOf(-android.R.attr.state_checked) // Seçili olmayan durum (varsayılan)
            ),
            intArrayOf(
                selectedColor,  // Seçili durum için renk
                unselectedColor // Seçili olmayan durum için renk
            )
        )

        viewModel.allCategories.observe(this) { categories ->
            chipGroup.setOnCheckedChangeListener(null) // Listener'ı geçici olarak kaldır
            chipGroup.removeAllViews()

            // "All" (Tümü) Chip'i
            val allChip = Chip(this).apply {
                text = "All" // Veya "Tümü" olarak yerelleştirin
                isCheckable = true
                isChecked = chipGroup.checkedChipId == View.NO_ID // Başlangıçta sadece biri seçili olmalı
                chipBackgroundColor = chipColorStateList // ColorStateList'i ata
                setTextColor(textColor)
                // ID atamak, checkedChipId'nin doğru çalışması için önemlidir
                this.id = View.generateViewId()
                if (isChecked) { // Başlangıçta "All" seçili ise
                    chipGroup.check(this.id)
                }
            }
            chipGroup.addView(allChip)

            // Diğer kategori Chip'leri
            categories.forEach { category ->
                val chip = Chip(this).apply {
                    text = category
                    isCheckable = true
                    isChecked = false // Başlangıçta diğerleri seçili değil
                    chipBackgroundColor = chipColorStateList // ColorStateList'i ata
                    setTextColor(textColor)
                    this.id = View.generateViewId() // Her chip için benzersiz ID
                }
                chipGroup.addView(chip)
            }

            // Başlangıçta "All" chip'inin seçili olmasını sağla (eğer henüz bir şey seçilmemişse)
            if (chipGroup.checkedChipId == View.NO_ID && chipGroup.childCount > 0) {
                (chipGroup.getChildAt(0) as? Chip)?.let {
                    it.isChecked = true
                }
            }


            chipGroup.setOnCheckedChangeListener { group, checkedId ->
                // Her chip'in rengi ColorStateList sayesinde otomatik güncellenecektir.
                // Bu listener sadece filtreleme mantığı için kalmalı.

                if (checkedId == View.NO_ID) { // Hiçbir chip seçili değilse (singleSelection false ise olabilir)
                    adapter?.setData(loginDataList) // Tümünü göster
                    // İsteğe bağlı: "All" chip'ini tekrar seçili yap
                    (group.getChildAt(0) as? Chip)?.isChecked = true
                    return@setOnCheckedChangeListener
                }

                val checkedChip = group.findViewById<Chip>(checkedId)
                val selectedCategory = checkedChip?.text?.toString()

                if (selectedCategory == "All" || selectedCategory == "Tümü") {
                    adapter?.setData(loginDataList)
                } else if (selectedCategory != null) {
                    val filteredList = loginDataList.filter { it.category == selectedCategory }
                    adapter?.setData(filteredList)
                } else {
                    // checkedId geçerli bir chip'e işaret etmiyorsa (teorik olarak olmamalı)
                    adapter?.setData(loginDataList)
                }
            }
        }

        binding.fab.setOnClickListener {

            onAddButtonClicked()

        }

        binding.createLogin.setOnClickListener{

            val intent = Intent(this, CreateLoginData::class.java)
            startActivity(intent)
            setVisibility(true)
            setAnimation(true)

        }

        binding.createAuthenticator.setOnClickListener {

            val intent = Intent(this, CreateAuthenticator::class.java)
            startActivity(intent)
            setVisibility(true)
            setAnimation(true)

        }

        binding.loginText.setOnClickListener {

            val intent = Intent(this, CreateLoginData::class.java)
            startActivity(intent)
            setVisibility(true)
            setAnimation(true)

        }

        binding.authText.setOnClickListener {

            val intent = Intent(this, CreateAuthenticator::class.java)
            startActivity(intent)
            setVisibility(true)
            setAnimation(true)

        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                search(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                search(newText)
                return false
            }
        })


        overlayView.setOnClickListener {
            onAddButtonClicked()
        }

        binding.menuButton.setOnClickListener{

            val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_menu, null)
            val bottomSheetDialog = BottomSheetDialog(this)
            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()

            val home = bottomSheetView.findViewById<LinearLayout>(R.id.homeLayout)
            val generator = bottomSheetView.findViewById<LinearLayout>(R.id.generateLayout)
            val settings = bottomSheetView.findViewById<LinearLayout>(R.id.settingsLayout)

            home.setOnClickListener {

                val intent = Intent(this@HomeActivity, HomeActivity::class.java)
                startActivity(intent)
                bottomSheetDialog.dismiss()

            }

            generator.setOnClickListener {

                val intent = Intent(this@HomeActivity, Generator::class.java)
                startActivity(intent)
                bottomSheetDialog.dismiss()

            }

            settings.setOnClickListener {

                val intent = Intent(this@HomeActivity, SettingsMenu::class.java)
                startActivity(intent)
                bottomSheetDialog.dismiss()

            }
        }
    }

    private fun search(query: String) {
        val filteredList = loginDataList.filter { loginData ->
            val isItemNameMatch = loginData.itemName?.contains(query, ignoreCase = true) ?: false
            val isUserNameMatch = loginData.userName?.contains(query, ignoreCase = true) ?: false
            val isWebsiteMatch = loginData.website?.contains(query, ignoreCase = true) ?: false
            val isAuthenticator = loginData.accountName?.contains(query, ignoreCase = true) ?: false

            isItemNameMatch || isUserNameMatch || isWebsiteMatch || isAuthenticator
        }

        adapter?.setData(filteredList) // DiffUtil ile güncellenmiş verileri set eder
    }


    private fun onAddButtonClicked(){

        setVisibility(clicked)
        setAnimation(clicked)

        clicked = !clicked

    }

    private fun setAnimation(clicked : Boolean) {

        if (!clicked) {
            overlayView.visibility = View.VISIBLE
            binding.createLogin.visibility = View.VISIBLE
            binding.createAuthenticator.visibility = View.VISIBLE
            binding.loginText.visibility = View.VISIBLE
            binding.authText.visibility = View.VISIBLE
            binding.createLogin.startAnimation(fromBottom)
            binding.createAuthenticator.startAnimation(fromBottom)
            binding.fab.startAnimation(rotateOpen)
        } else {
            overlayView.visibility = View.INVISIBLE
            binding.createLogin.visibility = View.INVISIBLE
            binding.createAuthenticator.visibility = View.INVISIBLE
            binding.loginText.visibility = View.INVISIBLE
            binding.authText.visibility = View.INVISIBLE
            binding.createLogin.startAnimation(toBottom)
            binding.createAuthenticator.startAnimation(toBottom)
            binding.fab.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked : Boolean) {
        if (!clicked){
            binding.loginText.visibility = View.VISIBLE
            binding.authText.visibility = View.VISIBLE
            binding.createLogin.visibility = View.VISIBLE
            binding.createAuthenticator.visibility = View.VISIBLE


        }else{

            binding.createLogin.visibility = View.INVISIBLE
            binding.createAuthenticator.visibility = View.INVISIBLE
            binding.loginText.visibility = View.INVISIBLE
            binding.authText.visibility = View.INVISIBLE

        }
    }
    private fun updateUIVisibility(isEmpty: Boolean) {
        if (isEmpty) {
            binding.dataList.visibility = View.GONE
            binding.homeImage.visibility = View.VISIBLE
            binding.noData.visibility = View.VISIBLE
        } else {
            binding.dataList.visibility = View.VISIBLE
            binding.homeImage.visibility = View.GONE
            binding.noData.visibility = View.GONE
        }
    }
}