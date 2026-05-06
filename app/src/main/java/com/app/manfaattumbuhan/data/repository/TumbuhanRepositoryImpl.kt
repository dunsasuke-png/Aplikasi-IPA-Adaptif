package com.app.manfaattumbuhan.data.repository

import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.domain.model.Tumbuhan
import com.app.manfaattumbuhan.domain.repository.TumbuhanRepository

class TumbuhanRepositoryImpl : TumbuhanRepository {

    override fun getAllTumbuhan(): List<Tumbuhan> {
        return StaticData.tumbuhanList
    }

    override fun getTumbuhanById(id: Int): Tumbuhan? {
        return StaticData.tumbuhanList.find { it.id == id }
    }
}
