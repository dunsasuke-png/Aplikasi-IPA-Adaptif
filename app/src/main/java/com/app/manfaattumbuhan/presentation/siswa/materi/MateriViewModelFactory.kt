package com.app.manfaattumbuhan.presentation.siswa.materi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.app.manfaattumbuhan.domain.usecase.GetTumbuhanUseCase

class MateriViewModelFactory(
    private val getTumbuhanUseCase: GetTumbuhanUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MateriViewModel::class.java)) {
            return MateriViewModel(getTumbuhanUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
