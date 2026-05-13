package com.app.manfaattumbuhan.domain.model

data class HasilBelajar(
    val siswaId: Int,
    val namaSiswa: String,
    val nilaiRataRata: Double,
    val tugasSelesai: Int,
    val totalTugas: Int,
    val peningkatan: Double
)
