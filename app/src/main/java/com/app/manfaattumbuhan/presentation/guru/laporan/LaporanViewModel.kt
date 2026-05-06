package com.app.manfaattumbuhan.presentation.guru.laporan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.NilaiApi
import com.app.manfaattumbuhan.data.remote.model.SiswaInfo
import kotlinx.coroutines.launch

data class LaporanItem(
    val siswa: SiswaInfo,
    val nilaiList: List<NilaiApi>,
    val rataRata: Double
)

class LaporanViewModel : ViewModel() {

    private val apiService = ApiConfig.createService<ApiService>()

    private val _laporanList = MutableLiveData<List<LaporanItem>>()
    val laporanList: LiveData<List<LaporanItem>> = _laporanList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val token = TokenManager.getToken()
                val siswaResponse = apiService.getSiswaList(token)

                if (siswaResponse.isSuccessful && siswaResponse.body()?.success == true) {
                    val siswaList = siswaResponse.body()!!.data!!.siswa
                    val laporanItems = mutableListOf<LaporanItem>()

                    for (siswa in siswaList) {
                        try {
                            val nilaiResponse = apiService.getNilaiList(token, siswa.id)
                            if (nilaiResponse.isSuccessful && nilaiResponse.body()?.success == true) {
                                val nilaiList = nilaiResponse.body()!!.data!!.nilai
                                val rata = if (nilaiList.isNotEmpty()) nilaiList.map { it.nilai }.average() else 0.0
                                laporanItems.add(LaporanItem(siswa, nilaiList, rata))
                            } else {
                                laporanItems.add(LaporanItem(siswa, emptyList(), 0.0))
                            }
                        } catch (_: Exception) {
                            laporanItems.add(LaporanItem(siswa, emptyList(), 0.0))
                        }
                    }
                    _laporanList.postValue(laporanItems)
                } else {
                    _error.postValue(siswaResponse.body()?.message ?: "Gagal memuat data")
                }
            } catch (e: Exception) {
                _error.postValue("Error: ${e.message}")
            } finally {
                _isLoading.postValue(false)
            }
        }
    }
}
