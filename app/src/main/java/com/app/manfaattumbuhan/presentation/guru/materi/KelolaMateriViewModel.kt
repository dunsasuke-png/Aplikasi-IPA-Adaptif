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

    companion object {
        const val ITEMS_PER_PAGE = 5
    }

    fun loadMateri() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val response = apiService.getMateriList(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val allMateri = response.body()?.data?.materi ?: emptyList()
                    _allMateriList.value = allMateri
                    _totalItems.value = allMateri.size
                    _totalPages.value = kotlin.math.max(1, kotlin.math.ceil(allMateri.size.toDouble() / ITEMS_PER_PAGE).toInt())
                    _currentPage.value = 1
                    applyPage(allMateri, 1)
                } else {
                    _message.value = "Gagal memuat materi"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun applyPage(allMateri: List<MateriApi>, page: Int) {
        val fromIndex = (page - 1) * ITEMS_PER_PAGE
        val toIndex = kotlin.math.min(fromIndex + ITEMS_PER_PAGE, allMateri.size)
        _materiList.value = if (fromIndex < allMateri.size) allMateri.subList(fromIndex, toIndex) else emptyList()
    }

    fun goToPage(page: Int) {
        val allMateri = _allMateriList.value ?: return
        val total = _totalPages.value ?: 1
        val safePage = page.coerceIn(1, total)
        _currentPage.value = safePage
        applyPage(allMateri, safePage)
    }

    fun createMateri(nama: String, deskripsi: String, manfaat: String, gambarUrl: String?, videoUrl: String?, urutan: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val request = CreateMateriRequest(nama, deskripsi, manfaat, gambarUrl, videoUrl, urutan)
                val response = apiService.createMateri(token, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.value = "Materi \"$nama\" berhasil ditambahkan"
                    loadMateri()
                } else {
                    _message.value = "Gagal menambahkan materi"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateMateri(id: String, nama: String, deskripsi: String, manfaat: String, gambarUrl: String?, videoUrl: String?, urutan: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val request = UpdateMateriRequest(nama, deskripsi, manfaat, gambarUrl, videoUrl, urutan)
                val response = apiService.updateMateri(token, id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.value = "Materi \"$nama\" berhasil diperbarui"
                    loadMateri()
                } else {
                    _message.value = "Gagal memperbarui materi"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteMateri(id: String, nama: String = "") {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val response = apiService.deleteMateri(token, id)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.value = "Materi \"$nama\" berhasil dihapus"
                    loadMateri()
                } else {
                    _message.value = "Gagal menghapus materi"
                }
            } catch (e: Exception) {
                _message.value = "Error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
