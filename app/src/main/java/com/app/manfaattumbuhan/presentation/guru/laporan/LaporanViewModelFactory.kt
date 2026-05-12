package com.app.manfaattumbuhan.presentation.guru.laporan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class LaporanViewModelFactory : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LaporanViewModel::class.java)) {
            return LaporanViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
