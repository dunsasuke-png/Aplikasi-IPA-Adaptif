package com.app.manfaattumbuhan.domain.usecase

import com.app.manfaattumbuhan.domain.model.HasilBelajar
import com.app.manfaattumbuhan.domain.model.Soal
import com.app.manfaattumbuhan.domain.repository.SoalRepository

class GetSoalUseCase(private val soalRepository: SoalRepository) {
    fun getAll(): List<Soal> = soalRepository.getAllSoal()
    fun getById(id: Int): Soal? = soalRepository.getSoalById(id)
    fun add(soal: Soal) = soalRepository.addSoal(soal)
    fun update(soal: Soal) = soalRepository.updateSoal(soal)
    fun delete(id: Int) = soalRepository.deleteSoal(id)
    fun getHasilBelajar(): List<HasilBelajar> = soalRepository.getHasilBelajar()
}
