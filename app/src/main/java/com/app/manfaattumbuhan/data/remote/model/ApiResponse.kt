package com.app.manfaattumbuhan.data.remote.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class Pagination(
    val page: Int,
    val limit: Int,
    val total: Int?,
    val total_pages: Int
)

// Auth
data class LoginGuruRequest(
    val nama: String,
    val password: String
)

data class LoginGuruResponse(
    val token: String,
    val guru: GuruInfo
)

data class GuruInfo(
    val id: String,
    val nama: String,
    val nip: String? = null,
    val sekolah: String? = null,
    val mapel: String? = null,
    val foto_profil: String? = null
)

data class LoginSiswaRequest(
    @SerializedName("nim")
    val nisn: String,
    val password: String
)

data class LoginSiswaResponse(
    val token: String,
    val siswa: SiswaInfo
)

// Siswa
data class SiswaInfo(
    val id: String,
    @SerializedName("nim")
    val nisn: String,
    val nama: String,
    val kelas: String,
    val status: String,
    val foto_profil: String?
)

data class CreateSiswaRequest(
    @SerializedName("nim")
    val nisn: String,
    val nama: String,
    val kelas: String,
    val password: String,
    val status: String = "aktif"
)

data class UpdateSiswaRequest(
    @SerializedName("nim")
    val nisn: String? = null,
    val nama: String? = null,
    val kelas: String? = null,
    val password: String? = null,
    val status: String? = null
)

data class UpdateProfilRequest(
    val nama: String? = null,
    val password: String? = null,
    val foto_profil: String? = null
)

data class UpdateGuruProfilRequest(
    val nama: String? = null,
    val password: String? = null,
    val nip: String? = null,
    val sekolah: String? = null,
    val mapel: String? = null,
    val foto_profil: String? = null
)

data class SiswaListResponse(
    val siswa: List<SiswaInfo>,
    val pagination: Pagination
)

// Soal
data class SoalApi(
    val id: String,
    val judul: String,
    val deskripsi: String,
    val nomor: Int,
    val video_url: String?,
    val foto_url: String?,
    val tingkat: String? = "pretest",
    val guru_id: String,
    val created_at: String?,
    val updated_at: String?
)

data class CreateSoalRequest(
    val judul: String,
    val deskripsi: String,
    val video_url: String? = null,
    val foto_url: String? = null,
    val tingkat: String = "pretest"
)

data class UpdateSoalRequest(
    val judul: String? = null,
    val deskripsi: String? = null,
    val video_url: String? = null,
    val foto_url: String? = null,
    val tingkat: String? = null
)

data class SoalListResponse(
    val soal: List<SoalApi>,
    val pagination: Pagination
)

// Nilai
data class NilaiApi(
    val id: String,
    val siswa_id: String,
    val soal_id: String,
    val nilai: Double,
    val catatan: String?,
    val created_at: String?,
    val soal: SoalReference?
)

data class SoalReference(
    val judul: String
)

data class CreateNilaiRequest(
    val soal_id: String,
    val nilai: Double,
    val catatan: String? = null
)

data class NilaiListResponse(
    val nilai: List<NilaiApi>,
    val pagination: Pagination
)

// Materi
data class MateriApi(
    val id: String,
    val nama: String,
    val deskripsi: String,
    val manfaat: String,
    val gambar_url: String?,
    val video_url: String?,
    val urutan: Int,
    val tingkat: String = "mudah",   // mudah | sedang | sulit
    val guru_id: String,
    val created_at: String?,
    val updated_at: String?
)

data class CreateMateriRequest(
    val nama: String,
    val deskripsi: String,
    val manfaat: String,
    val gambar_url: String? = null,
    val video_url: String? = null,
    val urutan: Int = 0,
    val tingkat: String = "mudah"
)

data class UpdateMateriRequest(
    val nama: String? = null,
    val deskripsi: String? = null,
    val manfaat: String? = null,
    val gambar_url: String? = null,
    val video_url: String? = null,
    val urutan: Int? = null,
    val tingkat: String? = null
)

data class MateriListResponse(
    val materi: List<MateriApi>,
    val pagination: Pagination
)

// Upload
data class UploadResponse(
    val url: String,
    val filename: String,
    val type: String,
    val original_name: String,
    val size: Long
)
