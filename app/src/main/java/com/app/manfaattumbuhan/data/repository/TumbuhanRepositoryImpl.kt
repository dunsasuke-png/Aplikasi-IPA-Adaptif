package com.app.manfaattumbuhan.data.repository

import com.app.manfaattumbuhan.data.local.StaticData
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

    /**
     * Ambil materi langsung dari Supabase REST API.
     * Supabase return List<MateriApi> langsung (flat JSON, tanpa wrapper).
     * Filter tingkat via query param, urutan via order=urutan.asc.
     */
    suspend fun getAllTumbuhanFromApi(tingkat: String? = null): List<Tumbuhan> {
        return try {
            val tingkatQuery = if (!tingkat.isNullOrBlank()) "eq.$tingkat" else null
            val response = apiService.getMateriList(
                tingkat = tingkatQuery
            )
            if (response.isSuccessful) {
                val materiList = response.body() ?: emptyList()
                materiList.mapIndexed { index, materi ->
                    Tumbuhan(
                        id        = index + 1,
                        nama      = materi.nama,
                        deskripsi = materi.deskripsi,
                        manfaat   = materi.manfaat,
                        imageRes  = 0,
                        gambarUrl = materi.gambar_url,
                        videoUrl  = materi.video_url,
                        apiId     = materi.id,
                        urutan    = materi.urutan,
                        tingkat   = materi.tingkat.lowercase()
                    )
                }
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
