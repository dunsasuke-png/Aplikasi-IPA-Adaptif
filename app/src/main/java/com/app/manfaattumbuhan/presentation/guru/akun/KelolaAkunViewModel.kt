package com.app.manfaattumbuhan.presentation.guru.akun

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.CreateSiswaRequest
import com.app.manfaattumbuhan.data.remote.model.SiswaInfo
import com.app.manfaattumbuhan.data.remote.model.UpdateSiswaRequest
import kotlinx.coroutines.launch

class KelolaAkunViewModel : ViewModel() {

    private val apiService = ApiConfig.createService<ApiService>()

    private val _siswaList = MutableLiveData<List<SiswaInfo>>()
    val siswaList: LiveData<List<SiswaInfo>> = _siswaList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadSiswa() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.getSiswaList(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    _siswaList.postValue(response.body()!!.data!!.siswa)
                } else {
                    _error.postValue(response.body()?.message ?: "Gagal memuat siswa")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    fun addSiswa(nim: String, nama: String, kelas: String, password: String) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.createSiswa(
                    token,
                    CreateSiswaRequest(nim, nama, kelas, password)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    loadSiswa()
                } else {
                    _error.postValue(response.body()?.message ?: "Gagal menambah siswa")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun updateSiswa(id: String, nama: String?, nim: String?, kelas: String?, password: String?) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.updateSiswa(
                    token, id,
                    UpdateSiswaRequest(nim, nama, kelas, password)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    loadSiswa()
                } else {
                    _error.postValue(response.body()?.message ?: "Gagal memperbarui siswa")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }

    fun deleteSiswa(id: String) {
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val response = apiService.deleteSiswa(token, id)
                if (response.isSuccessful) {
                    loadSiswa()
                } else {
                    _error.postValue("Gagal menghapus siswa")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            }
        }
    }
}
