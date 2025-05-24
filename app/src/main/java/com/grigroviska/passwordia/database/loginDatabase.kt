package com.grigroviska.passwordia.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.grigroviska.passwordia.dao.loginDao
import com.grigroviska.passwordia.model.LoginData

@Database(entities = [LoginData::class], version = 1, exportSchema = false)
abstract class loginDatabase : RoomDatabase() {

    abstract fun loginDao() : loginDao

    companion object{

        @Volatile
        private var INSTANCE : loginDatabase? = null
        fun getDatabase(context: Context): loginDatabase{

            return INSTANCE ?: synchronized(this){

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    loginDatabase::class.java,
                    "passwordia_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                return instance
            }

        }

        fun resetInstance() {
            INSTANCE = null
        }
    }

}