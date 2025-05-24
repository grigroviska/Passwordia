package com.grigroviska.passwordia.dao

import androidx.lifecycle.LiveData
import com.grigroviska.passwordia.TOTPGenerator
import com.grigroviska.passwordia.model.LoginData

class loginRepo(private val loginDao : loginDao){

    val allLogin : LiveData<List<LoginData>> = loginDao.getAllData()

    fun generateTOTP(totpKey: String): String {
        val totpGenerator = TOTPGenerator()
        return totpGenerator.generateTOTP(totpKey)
    }

    suspend fun insert(loginData: LoginData){

        loginDao.insert(loginData)

    }

    suspend fun delete(loginData: LoginData) {
        loginDao.delete(loginData)
    }

    suspend fun update(loginData: LoginData) {
        loginDao.update(loginData)
    }

    fun getLoginById(id: Int): LiveData<LoginData> {
        return loginDao.getLoginById(id)
    }

}