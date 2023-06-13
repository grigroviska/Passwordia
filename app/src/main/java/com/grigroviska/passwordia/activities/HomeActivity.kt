package com.grigroviska.passwordia.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.adapter.loginDataAdapter
import com.grigroviska.passwordia.databinding.ActivityHomeBinding
import com.grigroviska.passwordia.model.LoginData
import com.grigroviska.passwordia.viewModel.LoginViewModel

class HomeActivity : AppCompatActivity(), ViewModelStoreOwner {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: loginDataAdapter
    private var loginDataList: List<LoginData> = emptyList()
    private lateinit var viewModel: LoginViewModel

    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)

        auth = FirebaseAuth.getInstance()
        searchView = binding.searchBox

        try{
            adapter = loginDataAdapter(emptyList(), viewModelStore)
            viewModel = ViewModelProvider(this@HomeActivity)[LoginViewModel::class.java]
            viewModel.allLogin.observe(this@HomeActivity) { loginData ->
                loginDataList = loginData
                adapter = loginDataAdapter(loginDataList, viewModelStore)
                binding.dataList.adapter = adapter
                binding.dataList.layoutManager = LinearLayoutManager(this)
                adapter.notifyDataSetChanged()

                if (loginData.isEmpty()) {
                    binding.dataList.visibility = View.GONE
                    binding.homeImage.visibility = View.VISIBLE
                    binding.noData.visibility = View.VISIBLE
                } else {
                    binding.dataList.visibility = View.VISIBLE
                    binding.homeImage.visibility = View.GONE
                    binding.noData.visibility = View.GONE
                }
            }

        }catch (e: Exception){

            Toast.makeText(this, e.localizedMessage, Toast.LENGTH_SHORT).show()

        }


        binding.fab.setOnClickListener {

            val intent = Intent(this, CreateLoginData::class.java)
            startActivity(intent)
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Metni gönderilen metinle arayın
                search(query)
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Metin her değiştiğinde arama yapın
                search(newText)
                return false
            }
        })
    }

    private fun search(query: String) {
        val filteredList = loginDataList.filter { loginData ->
            // Öğeyle ilgili metni filtreleyin (örneğin, başlık, kullanıcı adı, vb.)
                    loginData.itemName.contains(query, ignoreCase = true) ||
                    loginData.userName.contains(query, ignoreCase = true) ||
                    loginData.website.contains(query, ignoreCase = true)
        }

        // Filtrelenmiş listeyi adaptöre ayarlayın
        adapter.setData(filteredList)

        // Verilerin güncellendiğini bildirin
        adapter.notifyDataSetChanged()
    }


}