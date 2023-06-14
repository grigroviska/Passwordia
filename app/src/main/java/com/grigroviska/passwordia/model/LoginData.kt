package com.grigroviska.passwordia.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loginData_table")
data class LoginData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val userName: String? = null,
    val alternateUserName: String? = null,
    val password: String? = null,
    val website: String? = null,
    val notes: String? = null,
    val itemName: String? = null,
    val category: String? = null,
    val accountName: String? = null,
    val totpKey: String? = null,
)