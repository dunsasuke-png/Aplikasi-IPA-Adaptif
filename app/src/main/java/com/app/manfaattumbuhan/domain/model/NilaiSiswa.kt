package com.app.manfaattumbuhan.domain.model

data class NilaiSiswa(
    val id: Int,
    val siswaId: String,
    val namaSiswa: String,
    val tingkat: String,
    val nilai: Int,
    val totalSoal: Int,
    val benar: Int,
    val tanggal: String
)
