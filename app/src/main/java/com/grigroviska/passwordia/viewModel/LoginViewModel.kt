package com.grigroviska.passwordia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.grigroviska.passwordia.dao.loginDao
import com.grigroviska.passwordia.dao.loginRepo
import com.grigroviska.passwordia.database.loginDatabase
import com.grigroviska.passwordia.database.loginDatabase_Impl
import com.grigroviska.passwordia.model.LoginData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val repo: loginRepo
    val allLogin: LiveData<List<LoginData>>
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    private val logDatabase: loginDatabase = loginDatabase.getDatabase(application)
    private val loginDao: loginDao = logDatabase.loginDao()

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

    fun update(loginData: LoginData) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.update(loginData)
        }
    }

    fun getLoginById(id: Int): LiveData<LoginData> {
        return repo.getLoginById(id)
    }

    fun signOutAndClearData(
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) { // Veritabanı ve Firebase işlemleri için IO dispatcher kullan
            try {
                // 1. Firebase'den çıkış yap
                auth.signOut()

                // 2. Room veritabanı dosyalarını tamamen sil
                deleteRoomDatabaseFiles(getApplication()) // application context'i geçiriyoruz

                // ÖNEMLİ: Singleton veritabanı instance'ını sıfırlamak (AppDatabase içinde)
                // Bu adım, bir sonraki veritabanı çağrısında yeni bir instance oluşturulmasını sağlar.
                // Eğer LoginDatabase içinde INSTANCE'ı null'a set edebileceğiniz bir reset metodu varsa onu kullanın.
                // Yoksa, basitçe INSTANCE = null atamasını kendi sınıfınızda yapın.
                // Örneğin: LoginDatabase.resetInstance() veya LoginDatabase.INSTANCE = null (private olmayan durumda)

                // En basit yöntem, veritabanı singletonını sıfırlamak için bir metot eklemek:
                loginDatabase.resetInstance() // <-- Bu metodu LoginDatabase.kt içine ekleyeceğiz.

                // İşlem başarılı oldu, ana iş parçacığına geri dönerek UI'ı güncelle
                launch(Dispatchers.Main) {
                    onSuccess()
                }
            } catch (e: Exception) {
                // Hata oluştu, ana iş parçacığına geri dönerek hatayı bildir
                launch(Dispatchers.Main) {
                    onFailure(e)
                }
            }
        }
    }

    fun updateFavoriteStatus(loginData: LoginData) {
        viewModelScope.launch(Dispatchers.IO) {
            repo.updateFavoriteStatus(loginData)
        }
    }

    /**
     * Room veritabanı ile ilişkili tüm dosyaları siler.
     * @param context Application Context.
     */
    private fun deleteRoomDatabaseFiles(context: Application) {
        val databaseName = "passwordia_database" // AppDatabase sınıfınızdaki veritabanı adınız
        val databasePath = context.getDatabasePath(databaseName)

        if (databasePath.exists()) {
            try {
                // Ana veritabanı dosyasını ve ilişkili dosyaları sil
                val walFile = File(databasePath.path + "-wal")
                val shmFile = File(databasePath.path + "-shm")
                val journalFile = File(databasePath.path + "-journal") // Eski Room sürümleri için

                if (databasePath.delete()) {
                    // Sadece ana dosya silindiğinde diğerlerini kontrol et
                    if (walFile.exists()) walFile.delete()
                    if (shmFile.exists()) shmFile.delete()
                    if (journalFile.exists()) journalFile.delete()
                    android.util.Log.d("DB_DEL", "Veritabanı ve ilişkili dosyalar başarıyla silindi.")
                } else {
                    android.util.Log.e("DB_DEL", "Ana veritabanı dosyası silinemedi: $databasePath")
                }

            } catch (e: Exception) {
                android.util.Log.e("DB_DEL", "Veritabanı dosyalarını silerken hata: ${e.message}", e)
            }
        } else {
            android.util.Log.d("DB_DEL", "Veritabanı dosyası bulunamadı: $databasePath")
        }
    }

    val allCategories: LiveData<List<String>> = loginDao.getAllCategories()



}
