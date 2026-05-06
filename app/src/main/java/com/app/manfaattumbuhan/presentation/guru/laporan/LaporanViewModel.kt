package com.app.manfaattumbuhan.presentation.guru.laporan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.NilaiApi
import com.app.manfaattumbuhan.data.remote.model.SiswaInfo
import com.app.manfaattumbuhan.data.remote.model.SoalReference
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
                            val apiNilai = if (nilaiResponse.isSuccessful && nilaiResponse.body()?.success == true) {
                                nilaiResponse.body()!!.data!!.nilai
                            } else {
                                emptyList()
                            }

                            val localNilai = StaticData.getNilaiByUserId(siswa.id).map { nilai ->
                                NilaiApi(
                                    id = "local-${nilai.id}",
                                    siswa_id = nilai.siswaId,
                                    soal_id = "latihan-${nilai.tingkat}",
                                    nilai = nilai.nilai.toDouble(),
                                    catatan = "Benar ${nilai.benar} dari ${nilai.totalSoal} - Level ${nilai.tingkat}",
                                    created_at = nilai.tanggal,
                                    soal = SoalReference(judul = "Latihan ${nilai.tingkat}")
                                )
                            }

                            val combined = mergeNilai(apiNilai, localNilai)
                            val rata = if (combined.isNotEmpty()) combined.map { it.nilai }.average() else 0.0
                            laporanItems.add(LaporanItem(siswa, combined, rata))
                        } catch (_: Exception) {
                            val localNilai = StaticData.getNilaiByUserId(siswa.id).map { nilai ->
                                NilaiApi(
                                    id = "local-${nilai.id}",
                                    siswa_id = nilai.siswaId,
                                    soal_id = "latihan-${nilai.tingkat}",
                                    nilai = nilai.nilai.toDouble(),
                                    catatan = "Benar ${nilai.benar} dari ${nilai.totalSoal} - Level ${nilai.tingkat}",
                                    created_at = nilai.tanggal,
                                    soal = SoalReference(judul = "Latihan ${nilai.tingkat}")
                                )
                            }
                            val rata = if (localNilai.isNotEmpty()) localNilai.map { it.nilai }.average() else 0.0
                            laporanItems.add(LaporanItem(siswa, localNilai, rata))
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

    private fun mergeNilai(apiNilai: List<NilaiApi>, localNilai: List<NilaiApi>): List<NilaiApi> {
        val apiIds = apiNilai.map { it.id }.toSet()
        val uniqueLocal = localNilai.filter { it.id !in apiIds }
        return apiNilai + uniqueLocal
    }
}
