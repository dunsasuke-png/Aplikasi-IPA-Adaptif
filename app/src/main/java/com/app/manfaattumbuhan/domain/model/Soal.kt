package com.app.manfaattumbuhan.domain.model

data class Soal(
    val id: Int,
    val pertanyaan: String,
    val imageRes: Int? = null,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val pilihan: List<String>,
    val jawabanBenar: Int,
    val modul: String = "",
    val tingkatKesulitan: String = "Sedang",
    val apiId: String? = null
)
