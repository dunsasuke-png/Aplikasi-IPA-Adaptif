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

    private val _materiList = MutableLiveData<List<MateriApi>>()
    val materiList: LiveData<List<MateriApi>> = _materiList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _message = MutableLiveData<String>()
    val message: LiveData<String> = _message

    fun loadMateri() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val response = apiService.getMateriList(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _materiList.value = response.body()?.data?.materi ?: emptyList()
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

    fun createMateri(nama: String, deskripsi: String, manfaat: String, gambarUrl: String?, urutan: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val request = CreateMateriRequest(nama, deskripsi, manfaat, gambarUrl, urutan)
                val response = apiService.createMateri(token, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.value = "Materi berhasil ditambahkan"
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

    fun updateMateri(id: String, nama: String, deskripsi: String, manfaat: String, gambarUrl: String?, urutan: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val request = UpdateMateriRequest(nama, deskripsi, manfaat, gambarUrl, urutan)
                val response = apiService.updateMateri(token, id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.value = "Materi berhasil diperbarui"
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

    fun deleteMateri(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val token = TokenManager.getToken()
                val response = apiService.deleteMateri(token, id)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.value = "Materi berhasil dihapus"
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
