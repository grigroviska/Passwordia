package com.grigroviska.passwordia.dao

import androidx.lifecycle.LiveData
import com.grigroviska.passwordia.model.LoginData

class loginRepo(private val loginDao : loginDao){

    val allLogin : LiveData<List<LoginData>> = loginDao.getAllData()

    suspend fun insert(loginData: LoginData){

        loginDao.insert(loginData)

    }

}