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

    @Query("SELECT * FROM passwordia_database ORDER BY id DESC")
    fun getAllData(): LiveData<List<LoginData>>

    @Query("SELECT * FROM passwordia_database ORDER BY id DESC")
    fun getAllDataNonLive(): List<LoginData>

    @Delete
    suspend fun delete(loginData: LoginData)

    @Query("DELETE FROM passwordia_database")
    fun clearAll()

    @Update
    suspend fun update(loginData: LoginData)

    @Query("SELECT * FROM passwordia_database WHERE id = :id")
    fun getLoginById(id: Int): LiveData<LoginData>

    @Query("SELECT DISTINCT category FROM passwordia_database WHERE category IS NOT NULL AND category != ''")
    fun getAllCategories(): LiveData<List<String>>

    @Query("UPDATE passwordia_database SET isFavorite = :isFavorite WHERE id = :loginId")
    suspend fun updateFavoriteStatus(loginId: Int, isFavorite: Boolean)

}