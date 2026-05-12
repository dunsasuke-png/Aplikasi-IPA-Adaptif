package com.app.manfaattumbuhan.domain.repository

import com.app.manfaattumbuhan.domain.model.HasilBelajar
import com.app.manfaattumbuhan.domain.model.Soal

interface SoalRepository {
    fun getAllSoal(): List<Soal>
    fun getSoalById(id: Int): Soal?
    fun addSoal(soal: Soal)
    fun updateSoal(soal: Soal)
    fun deleteSoal(id: Int)
    fun getHasilBelajar(): List<HasilBelajar>
}
