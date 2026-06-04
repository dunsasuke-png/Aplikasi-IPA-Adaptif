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

    /**
     * Ambil materi dari server, difilter per tingkat (mudah/sedang/sulit).
     * Persis seperti pola getSoalList — server yang memfilter, bukan client.
     */
    suspend fun getAllTumbuhanFromApi(tingkat: String? = null): List<Tumbuhan> {
        return try {
            val token = TokenManager.getToken()
            val response = apiService.getMateriList(token, tingkat = tingkat)
            if (response.isSuccessful && response.body()?.success == true) {
                val materiList = response.body()?.data?.materi ?: emptyList()
                val filtered = if (tingkat.isNullOrBlank()) {
                    materiList
                } else {
                    val target = tingkat.lowercase()
                    materiList.filter { it.tingkat.equals(target, ignoreCase = true) }
                }
                val sorted = filtered.sortedWith(compareBy(
                    { it.created_at ?: "" },  // urut berdasarkan waktu dibuat
                    { it.urutan }              // fallback
                ))
                sorted.mapIndexed { index, materi ->
                    Tumbuhan(
                        id        = index + 1,      // nomor relatif dalam tingkat ini
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
