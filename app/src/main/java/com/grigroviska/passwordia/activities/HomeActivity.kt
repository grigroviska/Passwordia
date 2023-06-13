package com.grigroviska.passwordia.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.R
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

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_open_anim) }
    private val rotateClose: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.rotate_close_anim) }
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.from_bottom_anim) }
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this, R.anim.to_bottom_anim) }

    private var clicked = false

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

            /*val intent = Intent(this, CreateLoginData::class.java)
            startActivity(intent)*/
            onAddButtonClicked()

        }

        binding.createLogin.setOnClickListener{

            val intent = Intent(this, CreateLoginData::class.java)
            startActivity(intent)

        }

        binding.createAuthenticator.setOnClickListener {

            val intent = Intent(this, CreateAuthenticator::class.java)
            startActivity(intent)

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
    }

    private fun search(query: String) {
        val filteredList = loginDataList.filter { loginData ->
                    loginData.itemName.contains(query, ignoreCase = true) ||
                    loginData.userName.contains(query, ignoreCase = true) ||
                    loginData.website.contains(query, ignoreCase = true)
        }

        adapter.setData(filteredList)

        adapter.notifyDataSetChanged()
    }

    private fun onAddButtonClicked(){

        setVisibility(clicked)
        setAnimation(clicked)

        clicked = !clicked

    }

    private fun setAnimation(clicked : Boolean) {

        if (!clicked) {
            binding.createLogin.visibility = View.VISIBLE
            binding.createAuthenticator.visibility = View.VISIBLE
            binding.createLogin.startAnimation(fromBottom)
            binding.createAuthenticator.startAnimation(fromBottom)
            binding.fab.startAnimation(rotateOpen)
        } else {
            binding.createLogin.visibility = View.INVISIBLE
            binding.createAuthenticator.visibility = View.INVISIBLE
            binding.createLogin.startAnimation(toBottom)
            binding.createAuthenticator.startAnimation(toBottom)
            binding.fab.startAnimation(rotateClose)
        }

    }

    private fun setVisibility(clicked : Boolean) {
        if (!clicked){

            binding.createLogin.visibility = View.VISIBLE
            binding.createAuthenticator.visibility = View.VISIBLE

        }else{

            binding.createLogin.visibility = View.INVISIBLE
            binding.createAuthenticator.visibility = View.INVISIBLE

        }
    }

}