package com.app.manfaattumbuhan.domain.model

data class User(
    val id: Int,
    val nama: String,
    val username: String,
    val role: UserRole,
    val kelas: String = "",
    val sekolah: String = "",
    val avatarRes: Int = 0
)

enum class UserRole {
    SISWA, GURU
}
