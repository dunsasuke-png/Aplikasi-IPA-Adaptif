package com.app.manfaattumbuhan.data.repository

import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.domain.model.HasilBelajar
import com.app.manfaattumbuhan.domain.model.Soal
import com.app.manfaattumbuhan.domain.repository.SoalRepository

class SoalRepositoryImpl : SoalRepository {

    override fun getAllSoal(): List<Soal> {
        return StaticData.soalList.toList()
    }

    override fun getSoalById(id: Int): Soal? {
        return StaticData.soalList.find { it.id == id }
    }

    override fun addSoal(soal: Soal) {
        StaticData.soalList.add(soal)
    }

    override fun updateSoal(soal: Soal) {
        val index = StaticData.soalList.indexOfFirst { it.id == soal.id }
        if (index != -1) {
            StaticData.soalList[index] = soal
        }
    }

    override fun deleteSoal(id: Int) {
        StaticData.soalList.removeAll { it.id == id }
    }

    override fun getHasilBelajar(): List<HasilBelajar> {
        return StaticData.hasilBelajarList
    }
}
