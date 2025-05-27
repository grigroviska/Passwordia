package com.grigroviska.passwordia.viewModel

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.grigroviska.passwordia.database.loginDatabase
import com.grigroviska.passwordia.model.LoginData
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

class BackupDialogViewModel(application: Application) : AndroidViewModel(application) {

    private val _backupState = MutableLiveData<BackupRestoreState>()
    val backupState: LiveData<BackupRestoreState> = _backupState

    private val _restoreState = MutableLiveData<BackupRestoreState>()
    val restoreState: LiveData<BackupRestoreState> = _restoreState

    // --- Configuration ---
    private val KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256"
    private val ENCRYPTION_ALGORITHM = "AES"
    private val CIPHER_TRANSFORMATION = "AES/GCM/NoPadding" // GCM provides authentication
    private val SALT_SIZE_BYTES = 16 // 128 bits
    private val GCM_IV_LENGTH_BYTES = 12 // Recommended for GCM
    private val GCM_TAG_LENGTH_BITS = 128 // Authentication tag length
    private val KEY_LENGTH_BITS = 256 // AES-256
    private val ITERATION_COUNT = 65536 // PBKDF2 iteration count (higher is more secure but slower)

    private val db = loginDatabase.getDatabase(application)
    private val loginDao = db.loginDao()

    private suspend fun getRoomDataAsString(): String {
        return withContext(Dispatchers.IO) {
            try {
                val allLoginData: List<LoginData> = loginDao.getAllDataNonLive()
                if (allLoginData.isEmpty()) {
                    Log.i("BackupExport", "No data to backup.")
                    "[]"
                } else {
                    Gson().toJson(allLoginData)
                }
            } catch (e: Exception) {
                Log.e("BackupExport", "Error fetching data from Room", e)
                _backupState.postValue(BackupRestoreState.Error("Failed to read data for backup: ${e.message}"))
                "[]"
            }
        }
    }

    private suspend fun importDataToRoom(decryptedJsonData: String) {
        withContext(Dispatchers.IO) {
            try {
                val gson = Gson()
                val type = object : TypeToken<List<LoginData>>() {}.type
                val loginDataToImport: List<LoginData> = gson.fromJson(decryptedJsonData, type)

                if (loginDataToImport.isNullOrEmpty() && decryptedJsonData != "[]") {
                    Log.w("BackupImport", "Decrypted JSON data might be malformed or did not parse into LoginData objects correctly.")
                    if (decryptedJsonData != "[]") {
                        _restoreState.postValue(BackupRestoreState.Error("Backup file seems to be corrupted or in an unexpected format."))
                        return@withContext
                    }
                }


                loginDao.clearAll()

                loginDataToImport.forEach { loginData ->
                    loginDao.insert(loginData)
                }
                Log.d("BackupRestore", "Data imported to Room successfully.")
                _restoreState.postValue(BackupRestoreState.Success("Restore successful!"))

            } catch (e: Exception) {
                Log.e("BackupImport", "Error importing data to Room", e)
                _restoreState.postValue(BackupRestoreState.Error("Import failed: ${e.message}"))
            }
        }
    }


    fun exportData(password: String, targetUri: Uri) {
        if (password.isBlank()) {
            _backupState.value = BackupRestoreState.Error("Password cannot be empty.")
            return
        }
        _backupState.value = BackupRestoreState.Loading

        viewModelScope.launch {
            try {
                val dataToEncrypt = getRoomDataAsString()

                val salt = ByteArray(SALT_SIZE_BYTES)
                SecureRandom().nextBytes(salt)

                val secretKey = generateKey(password.toCharArray(), salt)

                val iv = ByteArray(GCM_IV_LENGTH_BYTES)
                SecureRandom().nextBytes(iv)
                val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)

                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec)

                val encryptedData = cipher.doFinal(dataToEncrypt.toByteArray(Charsets.UTF_8))

                withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openOutputStream(targetUri)?.use { outputStream ->
                        outputStream.write(salt)
                        outputStream.write(iv)
                        outputStream.write(encryptedData)
                    } ?: run {
                        Log.e("BackupExport", "Could not open output stream for URI.")
                        _backupState.postValue(BackupRestoreState.Error("Could not save backup file."))
                        return@withContext
                    }
                }
                if (_backupState.value !is BackupRestoreState.Error) {
                    _backupState.value = BackupRestoreState.Success("Backup successful!")
                }
            } catch (e: Exception) {
                Log.e("BackupExport", "Error during export", e)
                _backupState.value = BackupRestoreState.Error("Export failed: ${e.message}")
            }
        }
    }

    fun importData(password: String, sourceUri: Uri) {
        if (password.isBlank()) {
            _restoreState.value = BackupRestoreState.Error("Password cannot be empty.")
            return
        }
        _restoreState.value = BackupRestoreState.Loading

        viewModelScope.launch {
            try {
                val backupFileContents: Triple<ByteArray, ByteArray, ByteArray>? = withContext(Dispatchers.IO) {
                    getApplication<Application>().contentResolver.openInputStream(sourceUri)?.use { inputStream ->
                        val saltBytes = ByteArray(SALT_SIZE_BYTES)
                        val ivBytes = ByteArray(GCM_IV_LENGTH_BYTES)

                        if (inputStream.read(saltBytes) != SALT_SIZE_BYTES) {
                            Log.e("BackupImport", "Incomplete salt read from backup file.")
                            _restoreState.postValue(BackupRestoreState.Error("Incomplete salt in backup file."))
                            return@withContext null
                        }
                        if (inputStream.read(ivBytes) != GCM_IV_LENGTH_BYTES) {
                            Log.e("BackupImport", "Incomplete IV read from backup file.")
                            _restoreState.postValue(BackupRestoreState.Error("Incomplete IV in backup file."))
                            return@withContext null
                        }
                        val encryptedBytes = inputStream.readBytes()
                        if (encryptedBytes.isEmpty() && inputStream.read() != -1) {
                            Log.e("BackupImport", "Could not read encrypted data from backup file.")
                            _restoreState.postValue(BackupRestoreState.Error("Could not read encrypted data from backup file."))
                            return@withContext null
                        }
                        Triple(saltBytes, ivBytes, encryptedBytes)
                    } ?: run {
                        Log.e("BackupImport", "Could not open input stream for URI: $sourceUri")
                        _restoreState.postValue(BackupRestoreState.Error("Could not open backup file."))
                        return@withContext null
                    }
                }


                if (backupFileContents == null) {
                    return@launch
                }

                val (salt, iv, encryptedData) = backupFileContents

                val secretKey = generateKey(password.toCharArray(), salt)
                val gcmParameterSpec = GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv)

                val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
                cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec)

                val decryptedBytes = cipher.doFinal(encryptedData)
                val decryptedData = String(decryptedBytes, Charsets.UTF_8)

                importDataToRoom(decryptedData)

            } catch (e: Exception) {
                Log.e("BackupImport", "Error during import process", e)
                if (e is javax.crypto.AEADBadTagException || e.cause is javax.crypto.AEADBadTagException) {
                    _restoreState.value = BackupRestoreState.Error("Import failed: Incorrect password or corrupted file.")
                } else {
                    _restoreState.value = BackupRestoreState.Error("Import failed: ${e.message}")
                }
            }
        }
    }

    private fun generateKey(password: CharArray, salt: ByteArray): SecretKeySpec {
        val factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM)
        val spec = PBEKeySpec(password, salt, ITERATION_COUNT, KEY_LENGTH_BITS)
        val secret = factory.generateSecret(spec)
        return SecretKeySpec(secret.encoded, ENCRYPTION_ALGORITHM)
    }
}

sealed class BackupRestoreState {
    object Idle : BackupRestoreState()
    object Loading : BackupRestoreState()
    data class Success(val message: String) : BackupRestoreState()
    data class Error(val message: String) : BackupRestoreState()
}