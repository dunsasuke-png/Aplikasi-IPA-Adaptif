package com.app.manfaattumbuhan.domain.usecase

import com.app.manfaattumbuhan.domain.model.Tumbuhan
import com.app.manfaattumbuhan.domain.repository.TumbuhanRepository

class GetTumbuhanUseCase(private val tumbuhanRepository: TumbuhanRepository) {
    fun getAll(): List<Tumbuhan> = tumbuhanRepository.getAllTumbuhan()
    fun getById(id: Int): Tumbuhan? = tumbuhanRepository.getTumbuhanById(id)
}
