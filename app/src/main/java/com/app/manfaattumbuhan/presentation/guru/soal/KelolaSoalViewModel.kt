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

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> = _totalItems

    private var currentFilter: String? = null
    private var currentSearchQuery = ""

    /**
     * Token untuk mencegah race condition: kalau user cepat ganti filter,
     * response dari request lama tidak boleh menimpa state terbaru.
     */
    @Volatile
    private var loadToken: Long = 0L

    companion object {
        const val ITEMS_PER_PAGE = 5
    }

    fun loadSoal(tingkat: String? = currentFilter, preservePage: Int = 1) {
        currentFilter = tingkat
        _isLoading.value = true

        // Mark this request as the newest.
        val token = System.nanoTime()
        loadToken = token

        // Clear current list immediately so RecyclerView doesn't reuse stale rows/offset while loading.
        _allSoalList.value = emptyList()
        _soalList.value = emptyList()
        _currentPage.value = preservePage
        _totalPages.value = 1
        _totalItems.value = 0

        viewModelScope.launch {
            try {
                val tingkatQuery = if (!tingkat.isNullOrBlank()) "eq.$tingkat" else null
                val response = apiService.getSoalList(
                    tingkat = tingkatQuery
                )

                // If a newer load started, ignore this response.
                if (loadToken != token) return@launch

                if (response.isSuccessful) {
                    val allSoal = response.body() ?: emptyList()
                    _allSoalList.postValue(allSoal)
                    applyFilterAndPagination(allSoal, preservePage)
                } else {
                    _error.postValue("Gagal memuat soal: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                if (loadToken == token) {
                    _isLoading.postValue(false)
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        val allSoal = _allSoalList.value ?: return
        applyFilterAndPagination(allSoal, 1)
    }

    private fun applyFilterAndPagination(allSoal: List<SoalApi>, targetPage: Int = 1) {
        var filteredList = allSoal
        if (currentSearchQuery.isNotBlank()) {
            filteredList = filteredList.filter {
                it.judul.contains(currentSearchQuery, ignoreCase = true) || 
                it.deskripsi.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        val totalPages = kotlin.math.max(1, kotlin.math.ceil(filteredList.size.toDouble() / ITEMS_PER_PAGE).toInt())
        val safePage = targetPage.coerceIn(1, totalPages)

        _totalItems.postValue(filteredList.size)
        _totalPages.postValue(totalPages)
        _currentPage.postValue(safePage)

        val fromIndex = (safePage - 1) * ITEMS_PER_PAGE
        val toIndex = kotlin.math.min(fromIndex + ITEMS_PER_PAGE, filteredList.size)
        _soalList.postValue(if (fromIndex < filteredList.size) filteredList.subList(fromIndex, toIndex) else emptyList())
    }

    fun goToPage(page: Int) {
        val allSoal = _allSoalList.value ?: return
        applyFilterAndPagination(allSoal, page)
    }

    fun addSoal(judul: String, deskripsi: String, fotoUrl: String? = null, videoUrl: String? = null, tingkat: String = "pretest") {
        viewModelScope.launch {
            try {
                val guruId = com.app.manfaattumbuhan.data.local.TokenManager.getUserId()
                val response = apiService.createSoal(
                    CreateSoalRequest(judul, deskripsi, videoUrl, fotoUrl, tingkat, guruId)
                )
                if (response.isSuccessful) {
                    _message.postValue("Soal berhasil ditambahkan")
                    loadSoal(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _error.postValue("Gagal membuat soal: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun updateSoal(id: String, judul: String, deskripsi: String, fotoUrl: String? = null, videoUrl: String? = null, tingkat: String? = null, displayNumber: Int = 0) {
        viewModelScope.launch {
            try {
                val response = apiService.updateSoal(
                    "eq.$id",
                    UpdateSoalRequest(judul, deskripsi, videoUrl, fotoUrl, tingkat)
                )
                if (response.isSuccessful) {
                    _message.postValue("Soal No. $displayNumber berhasil diperbarui")
                    loadSoal(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _error.postValue("Gagal memperbarui soal: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun deleteSoal(id: String, displayNumber: Int = 0) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteSoal("eq.$id")
                if (response.isSuccessful) {
                    _message.postValue("Soal No. $displayNumber berhasil dihapus")
                    val pageAfterDelete = _currentPage.value ?: 1
                    loadSoal(currentFilter, preservePage = pageAfterDelete)
                } else {
                    _error.postValue("Gagal menghapus soal: ${response.code()}")
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
