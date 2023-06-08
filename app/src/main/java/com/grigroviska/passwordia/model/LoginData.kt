package com.grigroviska.passwordia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loginData_table")
data class LoginData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val userName: String,
    val alternateUserName: String,
    val password: String,
    val website: String,
    val notes: String,
    val itemName: String,
    val category: String
)