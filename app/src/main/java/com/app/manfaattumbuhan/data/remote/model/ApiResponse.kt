package com.app.manfaattumbuhan.data.remote.model

import com.google.gson.annotations.SerializedName

// ==================== GURU ====================

data class GuruInfo(
    val id: String,
    val nama: String,
    val nip: String? = null,
    val sekolah: String? = null,
    val mapel: String? = null,
    val foto_profil: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class UpdateGuruProfilRequest(
    val nama: String? = null,
    val password: String? = null,
    val nip: String? = null,
    val sekolah: String? = null,
    val mapel: String? = null,
    val foto_profil: String? = null
)

// ==================== SISWA ====================

data class SiswaInfo(
    val id: String,
    val nisn: String,
    val nama: String,
    val kelas: String,
    val status: String,
    val foto_profil: String? = null,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class CreateSiswaRequest(
    val nisn: String,
    val nama: String,
    val kelas: String,
    val password: String,
    val status: String = "aktif"
)

data class UpdateSiswaRequest(
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

// ==================== MATERI ====================

data class MateriApi(
    val id: String,
    val nama: String,
    val deskripsi: String,
    val manfaat: String,
    val gambar_url: String? = null,
    val video_url: String? = null,
    val urutan: Int = 0,
    val tingkat: String = "mudah",
    val guru_id: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class CreateMateriRequest(
    val nama: String,
    val deskripsi: String,
    val manfaat: String,
    val gambar_url: String? = null,
    val video_url: String? = null,
    val urutan: Int = 0,
    val tingkat: String = "mudah",
    val guru_id: String
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

// ==================== SOAL ====================

data class SoalApi(
    val id: String,
    val judul: String,
    val deskripsi: String,
    val video_url: String? = null,
    val foto_url: String? = null,
    val tingkat: String = "pretest",
    val guru_id: String,
    val created_at: String? = null,
    val updated_at: String? = null
)

data class CreateSoalRequest(
    val judul: String,
    val deskripsi: String,
    val video_url: String? = null,
    val foto_url: String? = null,
    val tingkat: String = "pretest",
    val guru_id: String
)

data class UpdateSoalRequest(
    val judul: String? = null,
    val deskripsi: String? = null,
    val video_url: String? = null,
    val foto_url: String? = null,
    val tingkat: String? = null
)

// ==================== NILAI ====================

data class NilaiApi(
    val id: String,
    val siswa_id: String,
    val soal_id: String? = null,
    val nilai: Double,
    val catatan: String? = null,
    val created_at: String? = null,
    val soal: SoalReference? = null,   // dari select=*,soal(judul)
    val jumlah_benar: Int? = null,
    val jumlah_soal: Int? = null,
    val waktu_pengerjaan: Int? = null,
    val kesulitan_sebelumnya: String? = null,
    val kesulitan_selanjutnya: String? = null
)

data class SoalReference(
    val judul: String
)

data class CreateNilaiRequest(
    val siswa_id: String,
    val soal_id: String? = null,
    val nilai: Double,
    val catatan: String? = null,
    val jumlah_benar: Int? = null,
    val jumlah_soal: Int? = null,
    val waktu_pengerjaan: Int? = null,
    val kesulitan_sebelumnya: String? = null,
    val kesulitan_selanjutnya: String? = null
)

// ==================== SUPABASE AUTH via RPC ====================
// Login pakai RPC function karena password di-hash bcrypt
// Nama param harus match nama param di PostgreSQL function (p_nama, p_nisn, p_password)

data class RpcLoginGuruRequest(
    val p_nama: String,
    val p_password: String
)

data class RpcLoginSiswaRequest(
    val p_nisn: String,
    val p_password: String
)

// Response login = GuruInfo / SiswaInfo langsung (dari List<> index 0)
data class LoginGuruResponse(
    val token: String = "",
    val guru: GuruInfo
)

data class LoginSiswaResponse(
    val token: String = "",
    val siswa: SiswaInfo
)

// ==================== UPLOAD ====================

data class UploadResponse(
    val url: String,
    val filename: String,
    val type: String,
    val original_name: String? = null,
    val size: Long? = null
)
