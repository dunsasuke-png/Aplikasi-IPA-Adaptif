package com.app.manfaattumbuhan.data.repository

import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.domain.model.Tumbuhan
import com.app.manfaattumbuhan.domain.repository.TumbuhanRepository

class TumbuhanRepositoryImpl : TumbuhanRepository {

    private val apiService = ApiConfig.createService<ApiService>()

    override fun getAllTumbuhan(): List<Tumbuhan> {
        return StaticData.tumbuhanList
    }

    override fun getTumbuhanById(id: Int): Tumbuhan? {
        return StaticData.tumbuhanList.find { it.id == id }
    }

    suspend fun getAllTumbuhanFromApi(): List<Tumbuhan> {
        return try {
            val token = TokenManager.getToken()
            val response = apiService.getMateriList(token)
            if (response.isSuccessful && response.body()?.success == true) {
                val materiList = response.body()?.data?.materi ?: emptyList()
                materiList.mapIndexed { index, materi ->
                    Tumbuhan(
                        id = index + 1,
                        nama = materi.nama,
                        deskripsi = materi.deskripsi,
                        manfaat = materi.manfaat,
                        imageRes = 0,
                        gambarUrl = materi.gambar_url,
                        videoUrl = materi.video_url,
                        apiId = materi.id
                    )
                }
            } else {
                StaticData.tumbuhanList
            }
        } catch (e: Exception) {
            StaticData.tumbuhanList
        }
    }
}
