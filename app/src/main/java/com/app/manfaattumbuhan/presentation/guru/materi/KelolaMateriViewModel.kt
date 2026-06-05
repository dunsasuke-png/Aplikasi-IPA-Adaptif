package com.app.manfaattumbuhan.presentation.guru.materi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.CreateMateriRequest
import com.app.manfaattumbuhan.data.remote.model.MateriApi
import com.app.manfaattumbuhan.data.remote.model.UpdateMateriRequest
import kotlinx.coroutines.launch

class KelolaMateriViewModel : ViewModel() {

    private val apiService = ApiConfig.createService<ApiService>()

    private val _allMateriList = MutableLiveData<List<MateriApi>>()
    val allMateriList: LiveData<List<MateriApi>> = _allMateriList

    private val _materiList = MutableLiveData<List<MateriApi>>()
    val materiList: LiveData<List<MateriApi>> = _materiList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _totalItems = MutableLiveData(0)
    val totalItems: LiveData<Int> = _totalItems

    /** filterOffset = 0 (tingkat filter sudah server-side, nomor mulai dari 1) */
    val filterOffset: LiveData<Int> = MutableLiveData(0)

    private var currentFilter: String? = null  // null = semua
    private var currentSearchQuery = ""

    @Volatile private var loadToken: Long = 0L

    companion object {
        const val ITEMS_PER_PAGE = 5
    }

    // ──────────────────────────────────────────────────────────────────────────
    // Load & Pagination
    // ──────────────────────────────────────────────────────────────────────────

    fun loadMateri(tingkat: String? = currentFilter, preservePage: Int = 1) {
        currentFilter = tingkat
        _isLoading.value = true

        val token = System.nanoTime()
        loadToken = token

        _allMateriList.value = emptyList()
        _materiList.value = emptyList()
        _currentPage.value = preservePage
        _totalPages.value = 1
        _totalItems.value = 0

        viewModelScope.launch {
            try {
                val tingkatQuery = if (!tingkat.isNullOrBlank()) "eq.$tingkat" else null
                val response = apiService.getMateriList(
                    tingkat = tingkatQuery
                )

                if (loadToken != token) return@launch

                if (response.isSuccessful) {
                    val allMateri = response.body() ?: emptyList()
                    _allMateriList.postValue(allMateri)
                    applyFilterAndPagination(allMateri, preservePage)
                } else {
                    _message.postValue("Gagal memuat materi: ${response.code()}")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                if (loadToken == token) _isLoading.postValue(false)
            }
        }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        val allMateri = _allMateriList.value ?: return
        applyFilterAndPagination(allMateri, 1)
    }

    private fun applyFilterAndPagination(allMateri: List<MateriApi>, targetPage: Int = 1) {
        var filteredList = allMateri
        if (currentSearchQuery.isNotBlank()) {
            filteredList = filteredList.filter {
                it.nama.contains(currentSearchQuery, ignoreCase = true) ||
                it.deskripsi.contains(currentSearchQuery, ignoreCase = true) ||
                it.manfaat.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        val totalPages = kotlin.math.max(
            1,
            kotlin.math.ceil(filteredList.size.toDouble() / ITEMS_PER_PAGE).toInt()
        )
        val safePage = targetPage.coerceIn(1, totalPages)

        _totalItems.postValue(filteredList.size)
        _totalPages.postValue(totalPages)
        _currentPage.postValue(safePage)

        val fromIndex = (safePage - 1) * ITEMS_PER_PAGE
        val toIndex = kotlin.math.min(fromIndex + ITEMS_PER_PAGE, filteredList.size)
        _materiList.postValue(if (fromIndex < filteredList.size) filteredList.subList(fromIndex, toIndex) else emptyList())
    }

    fun goToPage(page: Int) {
        val all = _allMateriList.value ?: return
        applyFilterAndPagination(all, page)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CRUD
    // ──────────────────────────────────────────────────────────────────────────

    fun createMateri(nama: String, namaGambar: String, manfaat: String, tingkat: String, gambarUrl: String?, videoUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val guruId = com.app.manfaattumbuhan.data.local.TokenManager.getUserId()

                // Hitung urutan berdasarkan semua materi level yang sama
                val allResponse = apiService.getMateriList(tingkat = "eq.$tingkat")
                val urutanInLevel = if (allResponse.isSuccessful) {
                    (allResponse.body() ?: emptyList()).size + 1
                } else 1

                val request = CreateMateriRequest(
                    nama       = nama,
                    deskripsi  = namaGambar.ifBlank { "-" },
                    manfaat    = manfaat,
                    gambar_url = gambarUrl,
                    video_url  = videoUrl,
                    urutan     = urutanInLevel,
                    tingkat    = tingkat,
                    guru_id    = guruId
                )
                val response = apiService.createMateri(request)
                if (response.isSuccessful) {
                    _message.postValue("Materi berhasil ditambahkan ke level ${tingkat.replaceFirstChar { it.uppercase() }}")
                    loadMateri(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _message.postValue("Gagal menambahkan materi: ${response.code()}")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMateri(id: String, nama: String, namaGambar: String, manfaat: String, tingkat: String, gambarUrl: String?, videoUrl: String?, displayNumber: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = UpdateMateriRequest(
                    nama       = nama,
                    deskripsi  = namaGambar.ifBlank { "-" },
                    manfaat    = manfaat,
                    gambar_url = gambarUrl,
                    video_url  = videoUrl,
                    tingkat    = tingkat
                )
                val response = apiService.updateMateri("eq.$id", request)
                if (response.isSuccessful) {
                    _message.postValue("Materi #$displayNumber berhasil diperbarui")
                    loadMateri(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _message.postValue("Gagal memperbarui materi: ${response.code()}")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMateri(id: String, displayNumber: Int = 0) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = apiService.deleteMateri("eq.$id")
                if (response.isSuccessful) {
                    _message.postValue("Materi #$displayNumber berhasil dihapus")
                    loadMateri(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _message.postValue("Gagal menghapus materi: ${response.code()}")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
