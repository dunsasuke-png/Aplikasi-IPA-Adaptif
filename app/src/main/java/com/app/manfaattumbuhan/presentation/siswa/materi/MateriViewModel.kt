package com.app.manfaattumbuhan.presentation.siswa.materi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.repository.TumbuhanRepositoryImpl
import com.app.manfaattumbuhan.domain.model.Tumbuhan
import com.app.manfaattumbuhan.domain.usecase.GetTumbuhanUseCase
import kotlinx.coroutines.launch

class MateriViewModel(private val getTumbuhanUseCase: GetTumbuhanUseCase) : ViewModel() {

    private val _tumbuhanList = MutableLiveData<List<Tumbuhan>>()
    val tumbuhanList: LiveData<List<Tumbuhan>> = _tumbuhanList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _selectedTumbuhan = MutableLiveData<Tumbuhan?>()
    val selectedTumbuhan: LiveData<Tumbuhan?> = _selectedTumbuhan

    private val repository = TumbuhanRepositoryImpl()

    /**
     * Muat materi dari server berdasarkan tingkat.
     * Persis seperti loadSoal(tingkat) di sisi guru/soal siswa —
     * server yang memfilter, bukan client.
     */
    fun loadTumbuhan(tingkat: String = "mudah") {
        viewModelScope.launch {
            _isLoading.value = true
            _tumbuhanList.value = emptyList()
            try {
                val normalized = tingkat.trim().lowercase()
                val list = repository.getAllTumbuhanFromApi(normalized)
                _tumbuhanList.value = list
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectTumbuhan(tumbuhan: Tumbuhan) {
        _selectedTumbuhan.value = tumbuhan
    }
}
