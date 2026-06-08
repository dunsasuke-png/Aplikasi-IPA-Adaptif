package com.app.manfaattumbuhan.presentation.guru.laporan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    private val _originalLaporanList = MutableLiveData<List<LaporanItem>>()

    private val _laporanList = MutableLiveData<List<LaporanItem>>()
    val laporanList: LiveData<List<LaporanItem>> = _laporanList

    private val _rataKelas = MutableLiveData<Double>()
    val rataKelas: LiveData<Double> = _rataKelas

    private val _totalSiswa = MutableLiveData<Int>()
    val totalSiswa: LiveData<Int> = _totalSiswa

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private var currentSearchQuery = ""
    private var sortState = 0 // 0 = Nama A-Z, 1 = Rata-rata Tertinggi, 2 = Rata-rata Terendah

    fun loadData() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Get all siswa dari Supabase (flat List)
                val siswaResponse = apiService.getSiswaList()

                if (siswaResponse.isSuccessful) {
                    val siswaList = siswaResponse.body() ?: emptyList()
                    val laporanItems = mutableListOf<LaporanItem>()

                    for (siswa in siswaList) {
                        try {
                            val nilaiResponse = apiService.getNilaiList("eq.${siswa.id}")
                            if (nilaiResponse.isSuccessful) {
                                val nilaiList = nilaiResponse.body() ?: emptyList()
                                val rata = if (nilaiList.isNotEmpty()) nilaiList.map { it.nilai }.average() else 0.0
                                laporanItems.add(LaporanItem(siswa, nilaiList, rata))
                            } else {
                                laporanItems.add(LaporanItem(siswa, emptyList(), 0.0))
                            }
                        } catch (_: Exception) {
                            laporanItems.add(LaporanItem(siswa, emptyList(), 0.0))
                        }
                    }
                    _originalLaporanList.postValue(laporanItems)
                    applyFilterAndSort(laporanItems)
                } else {
                    _error.postValue("Gagal memuat data: ${siswaResponse.code()}")
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
        _originalLaporanList.value?.let { applyFilterAndSort(it) }
    }

    fun toggleSort() {
        sortState = (sortState + 1) % 3
        _originalLaporanList.value?.let { applyFilterAndSort(it) }
    }

    fun getSortState(): Int = sortState

    private fun applyFilterAndSort(list: List<LaporanItem>) {
        // Calculate Stats based on original unfiltered list
        _totalSiswa.postValue(list.size)
        val validAverages = list.filter { it.rataRata > 0 }.map { it.rataRata }
        val avg = if (validAverages.isNotEmpty()) validAverages.average() else 0.0
        _rataKelas.postValue(avg)

        // Filter
        var processedList = list
        if (currentSearchQuery.isNotBlank()) {
            processedList = processedList.filter {
                it.siswa.nama.contains(currentSearchQuery, ignoreCase = true) ||
                it.siswa.nisn.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        // Sort
        processedList = when (sortState) {
            1 -> processedList.sortedByDescending { it.rataRata }
            2 -> processedList.sortedBy { it.rataRata }
            else -> processedList.sortedBy { it.siswa.nama }
        }

        _laporanList.postValue(processedList)
    }
}
