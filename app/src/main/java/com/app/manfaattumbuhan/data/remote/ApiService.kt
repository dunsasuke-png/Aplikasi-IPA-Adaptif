package com.app.manfaattumbuhan.data.remote

import com.app.manfaattumbuhan.data.remote.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // ==================== AUTH via RPC (bcrypt) ====================

    @POST("rest/v1/rpc/login_guru")
    suspend fun loginGuru(
        @Body request: RpcLoginGuruRequest
    ): Response<List<GuruInfo>>

    @POST("rest/v1/rpc/login_siswa")
    suspend fun loginSiswa(
        @Body request: RpcLoginSiswaRequest
    ): Response<List<SiswaInfo>>

    // Get profil guru by id
    @GET("rest/v1/guru")
    suspend fun getGuruById(
        @Query("id") idEq: String,   // format: eq.uuid
        @Query("select") select: String = "*"
    ): Response<List<GuruInfo>>

    // Update profil guru
    @PATCH("rest/v1/guru")
    suspend fun updateGuruProfil(
        @Query("id") idEq: String,
        @Body request: UpdateGuruProfilRequest
    ): Response<List<GuruInfo>>

    // ==================== SISWA ====================

    // Get all siswa (Guru Only)
    @GET("rest/v1/siswa")
    suspend fun getSiswaList(
        @Query("select") select: String = "*",
        @Query("kelas") kelas: String? = null,    // format: eq.kelas
        @Query("status") status: String? = null,  // format: eq.aktif
        @Query("order") order: String = "created_at.desc"
    ): Response<List<SiswaInfo>>

    // Get siswa by id
    @GET("rest/v1/siswa")
    suspend fun getSiswaById(
        @Query("id") idEq: String,
        @Query("select") select: String = "*"
    ): Response<List<SiswaInfo>>

    // Create siswa
    @POST("rest/v1/siswa")
    suspend fun createSiswa(
        @Body request: CreateSiswaRequest
    ): Response<List<SiswaInfo>>

    // Update siswa
    @PATCH("rest/v1/siswa")
    suspend fun updateSiswa(
        @Query("id") idEq: String,
        @Body request: UpdateSiswaRequest
    ): Response<List<SiswaInfo>>

    // Update profil siswa
    @PATCH("rest/v1/siswa")
    suspend fun updateProfilSiswa(
        @Query("id") idEq: String,
        @Body request: UpdateProfilRequest
    ): Response<List<SiswaInfo>>

    // Delete siswa
    @DELETE("rest/v1/siswa")
    suspend fun deleteSiswa(
        @Query("id") idEq: String
    ): Response<Unit>

    // ==================== MATERI ====================

    @GET("rest/v1/materi")
    suspend fun getMateriList(
        @Query("select") select: String = "*",
        @Query("tingkat") tingkat: String? = null,  // format: eq.mudah
        @Query("guru_id") guruId: String? = null,   // format: eq.uuid
        @Query("order") order: String = "urutan.asc"
    ): Response<List<MateriApi>>

    @GET("rest/v1/materi")
    suspend fun getMateriById(
        @Query("id") idEq: String,
        @Query("select") select: String = "*"
    ): Response<List<MateriApi>>

    @POST("rest/v1/materi")
    suspend fun createMateri(
        @Body request: CreateMateriRequest
    ): Response<List<MateriApi>>

    @PATCH("rest/v1/materi")
    suspend fun updateMateri(
        @Query("id") idEq: String,
        @Body request: UpdateMateriRequest
    ): Response<List<MateriApi>>

    @DELETE("rest/v1/materi")
    suspend fun deleteMateri(
        @Query("id") idEq: String
    ): Response<Unit>

    // ==================== SOAL ====================

    @GET("rest/v1/soal")
    suspend fun getSoalList(
        @Query("select") select: String = "*",
        @Query("tingkat") tingkat: String? = null,  // format: eq.pretest
        @Query("guru_id") guruId: String? = null,
        @Query("order") order: String = "created_at.desc"
    ): Response<List<SoalApi>>

    @GET("rest/v1/soal")
    suspend fun getSoalById(
        @Query("id") idEq: String,
        @Query("select") select: String = "*"
    ): Response<List<SoalApi>>

    @POST("rest/v1/soal")
    suspend fun createSoal(
        @Body request: CreateSoalRequest
    ): Response<List<SoalApi>>

    @PATCH("rest/v1/soal")
    suspend fun updateSoal(
        @Query("id") idEq: String,
        @Body request: UpdateSoalRequest
    ): Response<List<SoalApi>>

    @DELETE("rest/v1/soal")
    suspend fun deleteSoal(
        @Query("id") idEq: String
    ): Response<Unit>

    // ==================== NILAI ====================

    @GET("rest/v1/nilai")
    suspend fun getNilaiList(
        @Query("siswa_id") siswaIdEq: String,
        @Query("select") select: String = "*",   // tanpa join soal, soal_id bisa berupa text
        @Query("order") order: String = "created_at.desc"
    ): Response<List<NilaiApi>>

    @POST("rest/v1/nilai")
    @Headers("Prefer: return=representation")
    suspend fun createNilai(
        @Body request: CreateNilaiRequest
    ): Response<List<NilaiApi>>
}
