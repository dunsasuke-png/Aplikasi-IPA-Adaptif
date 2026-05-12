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

    private val _allSoalList = MutableLiveData<List<SoalApi>>()

    private val _soalList = MutableLiveData<List<SoalApi>>()
    val soalList: LiveData<List<SoalApi>> = _soalList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> = _totalItems

    private var currentFilter: String? = null

    companion object {
        const val ITEMS_PER_PAGE = 5
    }

    fun loadSoal(tingkat: String? = currentFilter) {
        currentFilter = tingkat
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.getSoalList(token, tingkat = tingkat)
                if (response.isSuccessful && response.body()?.success == true) {
                    val allSoal = response.body()!!.data!!.soal
                    _allSoalList.postValue(allSoal)
                    _totalItems.postValue(allSoal.size)
                    _totalPages.postValue(kotlin.math.max(1, kotlin.math.ceil(allSoal.size.toDouble() / ITEMS_PER_PAGE).toInt()))
                    _currentPage.postValue(1)
                    applyPage(allSoal, 1)
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

    private fun applyPage(allSoal: List<SoalApi>, page: Int) {
        val fromIndex = (page - 1) * ITEMS_PER_PAGE
        val toIndex = kotlin.math.min(fromIndex + ITEMS_PER_PAGE, allSoal.size)
        _soalList.postValue(if (fromIndex < allSoal.size) allSoal.subList(fromIndex, toIndex) else emptyList())
    }

    fun goToPage(page: Int) {
        val allSoal = _allSoalList.value ?: return
        val total = _totalPages.value ?: 1
        val safePage = page.coerceIn(1, total)
        _currentPage.value = safePage
        applyPage(allSoal, safePage)
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
