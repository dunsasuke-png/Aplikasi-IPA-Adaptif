package com.app.manfaattumbuhan.presentation.guru.soal

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.CreateSoalRequest
import com.app.manfaattumbuhan.data.remote.model.SoalApi
import com.app.manfaattumbuhan.data.remote.model.UpdateSoalRequest
import kotlinx.coroutines.launch

class KelolaSoalViewModel : ViewModel() {

    private val apiService = ApiConfig.createService<ApiService>()

    private val _soalList = MutableLiveData<List<SoalApi>>()
    val soalList: LiveData<List<SoalApi>> = _soalList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentFilter: String? = null

    fun loadSoal(tingkat: String? = currentFilter) {
        currentFilter = tingkat
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.getSoalList(token, tingkat = tingkat)
                if (response.isSuccessful && response.body()?.success == true) {
                    _soalList.postValue(response.body()!!.data!!.soal)
                } else {
                    _error.postValue(response.body()?.message ?: "Gagal memuat soal")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun addSoal(judul: String, deskripsi: String, fotoUrl: String? = null, videoUrl: String? = null, tingkat: String = "pretest") {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.createSoal(
                    token,
                    CreateSoalRequest(judul, deskripsi, videoUrl, fotoUrl, tingkat)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    loadSoal()
                } else {
                    _error.postValue(response.body()?.message ?: "Gagal membuat soal")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun updateSoal(id: String, judul: String, deskripsi: String, fotoUrl: String? = null, videoUrl: String? = null, tingkat: String? = null) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.updateSoal(
                    token, id,
                    UpdateSoalRequest(judul, deskripsi, videoUrl, fotoUrl, tingkat)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    loadSoal()
                } else {
                    _error.postValue(response.body()?.message ?: "Gagal memperbarui soal")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun deleteSoal(id: String) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.deleteSoal(token, id)
                if (response.isSuccessful) {
                    loadSoal()
                } else {
                    _error.postValue("Gagal menghapus soal")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }
}

class KelolaSoalViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return KelolaSoalViewModel() as T
    }
}
