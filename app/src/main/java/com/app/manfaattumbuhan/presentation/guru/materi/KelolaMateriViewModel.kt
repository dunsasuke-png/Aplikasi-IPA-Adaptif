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
                val auth = TokenManager.getToken()
                val response = apiService.getMateriList(auth, tingkat = tingkat)

                if (loadToken != token) return@launch

                if (response.isSuccessful && response.body()?.success == true) {
                    val allMateri = (response.body()?.data?.materi ?: emptyList())
                        .sortedWith(compareBy(
                            { it.created_at ?: "" },  // urut berdasarkan waktu dibuat
                            { it.urutan }              // fallback jika created_at sama
                        ))

                    val totalPages = kotlin.math.max(
                        1,
                        kotlin.math.ceil(allMateri.size.toDouble() / ITEMS_PER_PAGE).toInt()
                    )
                    val targetPage = preservePage.coerceIn(1, totalPages)

                    _allMateriList.postValue(allMateri)
                    _totalItems.postValue(allMateri.size)
                    _totalPages.postValue(totalPages)
                    _currentPage.postValue(targetPage)
                    applyPage(allMateri, targetPage)
                } else {
                    _message.postValue("Gagal memuat materi")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                if (loadToken == token) _isLoading.postValue(false)
            }
        }
    }

    private fun applyPage(all: List<MateriApi>, page: Int) {
        val from = (page - 1) * ITEMS_PER_PAGE
        val to = kotlin.math.min(from + ITEMS_PER_PAGE, all.size)
        _materiList.postValue(if (from < all.size) all.subList(from, to) else emptyList())
    }

    fun setLevelFilter(level: String?) {
        loadMateri(tingkat = level, preservePage = 1)
    }

    fun goToPage(page: Int) {
        val all = _allMateriList.value ?: return
        val total = _totalPages.value ?: 1
        val safePage = page.coerceIn(1, total)
        _currentPage.value = safePage
        applyPage(all, safePage)
    }

    // ──────────────────────────────────────────────────────────────────────────
    // CRUD
    // ──────────────────────────────────────────────────────────────────────────

    fun createMateri(nama: String, namaGambar: String, manfaat: String, tingkat: String, gambarUrl: String?, videoUrl: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val auth     = TokenManager.getToken()
                val allNow   = _allMateriList.value ?: emptyList()
                // urutan = jumlah materi dengan tingkat ini + 1 (otomatis)
                val urutanInLevel = allNow.count { it.tingkat == tingkat } + 1
                val request  = CreateMateriRequest(
                    nama       = nama,
                    deskripsi  = namaGambar.ifBlank { "-" },
                    manfaat    = manfaat,
                    gambar_url = gambarUrl,
                    video_url  = videoUrl,
                    urutan     = urutanInLevel,
                    tingkat    = tingkat
                )
                val response = apiService.createMateri(auth, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.postValue("Materi berhasil ditambahkan ke level ${tingkat.replaceFirstChar { it.uppercase() }}")
                    loadMateri(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _message.postValue("Gagal menambahkan materi")
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
                val auth     = TokenManager.getToken()
                val request  = UpdateMateriRequest(
                    nama       = nama,
                    deskripsi  = namaGambar.ifBlank { "-" },
                    manfaat    = manfaat,
                    gambar_url = gambarUrl,
                    video_url  = videoUrl,
                    tingkat    = tingkat
                )
                val response = apiService.updateMateri(auth, id, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    _message.postValue("Materi #$displayNumber berhasil diperbarui")
                    loadMateri(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _message.postValue("Gagal memperbarui materi")
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
                val auth     = TokenManager.getToken()
                val response = apiService.deleteMateri(auth, id)
                val ok = response.isSuccessful && (response.body()?.success != false)
                if (ok) {
                    _message.postValue("Materi #$displayNumber berhasil dihapus")
                    loadMateri(currentFilter, preservePage = _currentPage.value ?: 1)
                } else {
                    _message.postValue("Gagal menghapus materi")
                }
            } catch (e: Exception) {
                _message.postValue("Error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }
}
