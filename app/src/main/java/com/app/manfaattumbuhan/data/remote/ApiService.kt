package com.app.manfaattumbuhan.data.remote

import com.app.manfaattumbuhan.data.remote.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    // Auth
    @POST("api/auth/login-guru")
    suspend fun loginGuru(
        @Body request: LoginGuruRequest
    ): Response<ApiResponse<LoginGuruResponse>>

    @POST("api/auth/login-siswa")
    suspend fun loginSiswa(
        @Body request: LoginSiswaRequest
    ): Response<ApiResponse<LoginSiswaResponse>>

    // Siswa (Guru Only)
    @GET("api/siswa")
    suspend fun getSiswaList(
        @Header("Authorization") token: String,
        @Query("kelas") kelas: String? = null,
        @Query("status") status: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<ApiResponse<SiswaListResponse>>

    @POST("api/siswa")
    suspend fun createSiswa(
        @Header("Authorization") token: String,
        @Body request: CreateSiswaRequest
    ): Response<ApiResponse<SiswaInfo>>

    @GET("api/siswa/{id}")
    suspend fun getSiswaDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<SiswaInfo>>

    @PUT("api/siswa/{id}")
    suspend fun updateSiswa(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateSiswaRequest
    ): Response<ApiResponse<SiswaInfo>>

    @DELETE("api/siswa/{id}")
    suspend fun deleteSiswa(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<Nothing>>

    // Profil Siswa
    @PATCH("api/siswa/{id}/profil")
    suspend fun updateProfil(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateProfilRequest
    ): Response<ApiResponse<SiswaInfo>>

    // Nilai
    @GET("api/siswa/{id}/nilai")
    suspend fun getNilaiList(
        @Header("Authorization") token: String,
        @Path("id") siswaId: String,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<ApiResponse<NilaiListResponse>>

    @POST("api/siswa/{id}/nilai")
    suspend fun createNilai(
        @Header("Authorization") token: String,
        @Path("id") siswaId: String,
        @Body request: CreateNilaiRequest
    ): Response<ApiResponse<NilaiApi>>

    // Profil Guru
    @GET("api/guru/profil")
    suspend fun getGuruProfil(
        @Header("Authorization") token: String
    ): Response<ApiResponse<GuruInfo>>

    @PATCH("api/guru/profil")
    suspend fun updateGuruProfil(
        @Header("Authorization") token: String,
        @Body request: UpdateGuruProfilRequest
    ): Response<ApiResponse<GuruInfo>>

    // Soal (Guru Only)
    @GET("api/guru/soal")
    suspend fun getSoalList(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("tingkat") tingkat: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<ApiResponse<SoalListResponse>>

    @POST("api/guru/soal")
    suspend fun createSoal(
        @Header("Authorization") token: String,
        @Body request: CreateSoalRequest
    ): Response<ApiResponse<SoalApi>>

    @GET("api/guru/soal/{id}")
    suspend fun getSoalDetail(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<SoalApi>>

    @PUT("api/guru/soal/{id}")
    suspend fun updateSoal(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateSoalRequest
    ): Response<ApiResponse<SoalApi>>

    @DELETE("api/guru/soal/{id}")
    suspend fun deleteSoal(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<Nothing>>

    // Materi (Guru)
    @GET("api/guru/materi")
    suspend fun getMateriList(
        @Header("Authorization") token: String,
        @Query("search") search: String? = null,
        @Query("tingkat") tingkat: String? = null,
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 100
    ): Response<ApiResponse<MateriListResponse>>

    @POST("api/guru/materi")
    suspend fun createMateri(
        @Header("Authorization") token: String,
        @Body request: CreateMateriRequest
    ): Response<ApiResponse<MateriApi>>

    @PUT("api/guru/materi/{id}")
    suspend fun updateMateri(
        @Header("Authorization") token: String,
        @Path("id") id: String,
        @Body request: UpdateMateriRequest
    ): Response<ApiResponse<MateriApi>>

    @DELETE("api/guru/materi/{id}")
    suspend fun deleteMateri(
        @Header("Authorization") token: String,
        @Path("id") id: String
    ): Response<ApiResponse<Nothing>>

    // Upload
    @Multipart
    @POST("api/upload")
    suspend fun uploadFile(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
        @Part("type") type: RequestBody
    ): Response<ApiResponse<UploadResponse>>
}
