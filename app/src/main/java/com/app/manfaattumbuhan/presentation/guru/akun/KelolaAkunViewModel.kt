package com.app.manfaattumbuhan.presentation.guru.akun

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.CreateSiswaRequest
import com.app.manfaattumbuhan.data.remote.model.SiswaInfo
import com.app.manfaattumbuhan.data.remote.model.UpdateSiswaRequest
import kotlinx.coroutines.launch

class KelolaAkunViewModel : ViewModel() {

    private val apiService = ApiConfig.createService<ApiService>()

    private val _originalSiswaList = MutableLiveData<List<SiswaInfo>>()

    private val _siswaList = MutableLiveData<List<SiswaInfo>>()
    val siswaList: LiveData<List<SiswaInfo>> = _siswaList

    private var currentSearchQuery = ""

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadSiswa() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = apiService.getSiswaList()
                if (response.isSuccessful) {
                    val list = response.body() ?: emptyList()
                    _originalSiswaList.postValue(list)
                    applyFilter(list)
                } else {
                    _error.postValue("Gagal memuat siswa: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun setSearchQuery(query: String) {
        currentSearchQuery = query
        _originalSiswaList.value?.let { applyFilter(it) }
    }

    private fun applyFilter(list: List<SiswaInfo>) {
        var processedList = list
        if (currentSearchQuery.isNotBlank()) {
            processedList = processedList.filter {
                it.nama.contains(currentSearchQuery, ignoreCase = true) ||
                it.nisn.contains(currentSearchQuery, ignoreCase = true) ||
                it.kelas.contains(currentSearchQuery, ignoreCase = true)
            }
        }
        
        // Sort by nama (alphabetical)
        processedList = processedList.sortedBy { it.nama }
        
        _siswaList.postValue(processedList)
    }

    fun addSiswa(nisn: String, nama: String, kelas: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.createSiswa(
                    CreateSiswaRequest(nisn, nama, kelas, password)
                )
                if (response.isSuccessful) {
                    loadSiswa()
                    onSuccess()
                } else {
                    _error.postValue("Gagal menambah siswa: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun updateSiswa(id: String, nama: String?, nisn: String?, kelas: String?, password: String?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.updateSiswa(
                    "eq.$id",
                    UpdateSiswaRequest(nisn, nama, kelas, password)
                )
                if (response.isSuccessful) {
                    loadSiswa()
                    onSuccess()
                } else {
                    _error.postValue("Gagal memperbarui siswa: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun deleteSiswa(id: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val response = apiService.deleteSiswa("eq.$id")
                if (response.isSuccessful) {
                    loadSiswa()
                    onSuccess()
                } else {
                    _error.postValue("Gagal menghapus siswa: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }
}
