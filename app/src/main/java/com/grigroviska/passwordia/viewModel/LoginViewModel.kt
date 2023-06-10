package com.grigroviska.passwordia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.grigroviska.passwordia.dao.loginRepo
import com.grigroviska.passwordia.database.loginDatabase
import com.grigroviska.passwordia.model.LoginData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: loginRepo
    val allLogin: LiveData<List<LoginData>>

    init {
        val dao = loginDatabase.getDatabase(application).loginDao()
        repo = loginRepo(dao)
        allLogin = repo.allLogin
    }

    fun insert(loginData: LoginData) = viewModelScope.launch(Dispatchers.IO) {
        repo.insert(loginData)
    }

    fun deleteLoginData(loginData: LoginData) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.delete(loginData)
        }
    }

}