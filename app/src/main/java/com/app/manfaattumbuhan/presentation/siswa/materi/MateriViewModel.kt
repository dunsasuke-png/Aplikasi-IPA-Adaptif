package com.app.manfaattumbuhan.presentation.siswa.materi

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.manfaattumbuhan.domain.model.Tumbuhan
import com.app.manfaattumbuhan.domain.usecase.GetTumbuhanUseCase

class MateriViewModel(private val getTumbuhanUseCase: GetTumbuhanUseCase) : ViewModel() {

    private val _tumbuhanList = MutableLiveData<List<Tumbuhan>>()
    val tumbuhanList: LiveData<List<Tumbuhan>> = _tumbuhanList

    private val _selectedTumbuhan = MutableLiveData<Tumbuhan?>()
    val selectedTumbuhan: LiveData<Tumbuhan?> = _selectedTumbuhan

    fun loadTumbuhan() {
        _tumbuhanList.value = getTumbuhanUseCase.getAll()
    }

    fun selectTumbuhan(tumbuhan: Tumbuhan) {
        _selectedTumbuhan.value = tumbuhan
    }
}
