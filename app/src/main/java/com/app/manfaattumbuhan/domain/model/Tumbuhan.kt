package com.app.manfaattumbuhan.domain.model

data class Tumbuhan(
    val id: Int = 0,
    val nama: String,
    val deskripsi: String,
    val manfaat: String,
    val imageRes: Int = 0,
    val gambarUrl: String? = null,
    val videoUrl: String? = null,
    val apiId: String? = null,
    val urutan: Int = 0,
    val tingkat: String? = null     // "mudah" | "sedang" | "sulit"
)
