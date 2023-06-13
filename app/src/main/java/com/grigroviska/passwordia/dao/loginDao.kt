package com.grigroviska.passwordia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.grigroviska.passwordia.model.LoginData

@Dao
interface loginDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(loginData: LoginData)

    @Query("SELECT * FROM loginData_table ORDER BY id DESC")
    fun getAllData(): LiveData<List<LoginData>>

    @Delete
    suspend fun delete(loginData: LoginData)

    @Update
    suspend fun update(loginData: LoginData)

}