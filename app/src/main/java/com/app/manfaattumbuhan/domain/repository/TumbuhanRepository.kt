package com.app.manfaattumbuhan.domain.repository

import com.app.manfaattumbuhan.domain.model.Tumbuhan

interface TumbuhanRepository {
    fun getAllTumbuhan(): List<Tumbuhan>
    fun getTumbuhanById(id: Int): Tumbuhan?
}
