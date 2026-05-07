package com.app.manfaattumbuhan.data.local

import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.domain.model.HasilBelajar
import com.app.manfaattumbuhan.domain.model.NilaiSiswa
import com.app.manfaattumbuhan.domain.model.Soal
import com.app.manfaattumbuhan.domain.model.Tumbuhan
import com.app.manfaattumbuhan.domain.model.User
import com.app.manfaattumbuhan.domain.model.UserRole

object StaticData {

    val users = mutableListOf(
        User(
            id = 1,
            nama = "Budi Santoso",
            username = "siswa",
            role = UserRole.SISWA,
            kelas = "5-A Sekolah Dasar",
            sekolah = "SLB Negeri Harapan",
            avatarRes = R.drawable.avatar_siswa
        ),
        User(
            id = 2,
            nama = "Andi Setiawan",
            username = "andi",
            role = UserRole.SISWA,
            kelas = "5-A Sekolah Dasar",
            sekolah = "SLB Negeri Harapan",
            avatarRes = R.drawable.avatar_siswa
        ),
        User(
            id = 3,
            nama = "Citra Lestari",
            username = "citra",
            role = UserRole.SISWA,
            kelas = "5-A Sekolah Dasar",
            sekolah = "SLB Negeri Harapan",
            avatarRes = R.drawable.avatar_siswa
        ),
        User(
            id = 4,
            nama = "Dian Pertiwi",
            username = "dian",
            role = UserRole.SISWA,
            kelas = "5-A Sekolah Dasar",
            sekolah = "SLB Negeri Harapan",
            avatarRes = R.drawable.avatar_siswa
        ),
        User(
            id = 100,
            nama = "Admin Guru",
            username = "Admin Guru",
            role = UserRole.GURU,
            kelas = "",
            sekolah = "SLB Negeri Harapan",
            avatarRes = R.drawable.avatar_guru
        )
    )

    val passwords = mutableMapOf(
        "siswa" to "siswa123",
        "andi" to "andi123",
        "citra" to "citra123",
        "dian" to "dian123",
        "Admin Guru" to "guru123"
    )

    val tumbuhanList = listOf<Tumbuhan>()

    val soalList = mutableListOf(
        Soal(
            id = 1,
            pertanyaan = "Apa fungsi utama akar pada tumbuhan merambat?",
            pilihan = listOf("Menyerap air", "Membuat makanan", "Menyimpan cadangan makanan", "Melindungi batang"),
            jawabanBenar = 0,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Sedang",
            imageRes = null
        ),
        Soal(
            id = 2,
            pertanyaan = "Sebutkan 3 bagian utama dari struktur daun.",
            pilihan = listOf("Akar, batang, daun", "Helai daun, tangkai daun, pelepah daun", "Bunga, buah, biji", "Kuncup, ranting, dahan"),
            jawabanBenar = 1,
            modul = "Struktur Dasar",
            tingkatKesulitan = "Mudah",
            imageRes = null
        ),
        Soal(
            id = 3,
            pertanyaan = "Bagian tumbuhan manakah yang ditunjukkan pada gambar di atas?",
            imageRes = R.drawable.img_sayuran,
            pilihan = listOf("Akar", "Daun", "Batang", "Bunga"),
            jawabanBenar = 1,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Sedang"
        ),
        Soal(
            id = 4,
            pertanyaan = "Tumbuhan padi termasuk ke dalam jenis tumbuhan?",
            pilihan = listOf("Dikotil", "Monokotil", "Paku", "Lumut"),
            jawabanBenar = 1,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Sedang"
        ),
        Soal(
            id = 5,
            pertanyaan = "Apa manfaat utama pohon jati?",
            pilihan = listOf("Untuk makanan", "Untuk obat", "Untuk bahan bangunan", "Untuk pupuk"),
            jawabanBenar = 2,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Mudah"
        ),
        Soal(
            id = 6,
            pertanyaan = "Kapas digunakan untuk membuat?",
            pilihan = listOf("Obat", "Pakaian", "Makanan", "Rumah"),
            jawabanBenar = 1,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Mudah"
        ),
        Soal(
            id = 7,
            pertanyaan = "Lidah buaya bermanfaat untuk?",
            pilihan = listOf("Bahan bangunan", "Makanan pokok", "Kesehatan kulit", "Bahan bakar"),
            jawabanBenar = 2,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Mudah"
        ),
        Soal(
            id = 8,
            pertanyaan = "Bagian tumbuhan yang berfungsi untuk fotosintesis adalah?",
            pilihan = listOf("Akar", "Batang", "Daun", "Bunga"),
            jawabanBenar = 2,
            modul = "Struktur Dasar",
            tingkatKesulitan = "Mudah"
        ),
        Soal(
            id = 9,
            pertanyaan = "Proses tumbuhan membuat makanan sendiri disebut?",
            pilihan = listOf("Respirasi", "Fotosintesis", "Transpirasi", "Fermentasi"),
            jawabanBenar = 1,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Sedang"
        ),
        Soal(
            id = 10,
            pertanyaan = "Tumbuhan yang dimanfaatkan untuk obat tradisional adalah?",
            pilihan = listOf("Padi", "Jati", "Kapas", "Lidah Buaya"),
            jawabanBenar = 3,
            modul = "Manfaat Tumbuhan",
            tingkatKesulitan = "Mudah"
        )
    )

    val hasilBelajarList = listOf(
        HasilBelajar(1, "Budi Santoso", 85.4, 12, 15, 4.2),
        HasilBelajar(2, "Andi Setiawan", 78.0, 10, 15, 2.5),
        HasilBelajar(3, "Citra Lestari", 92.0, 14, 15, 5.0),
        HasilBelajar(4, "Dian Pertiwi", 88.5, 13, 15, 3.8)
    )

    // Unlocked levels per user: userId -> set of unlocked difficulty levels
    val unlockedLevels = mutableMapOf<Int, MutableSet<String>>()

    // Current assigned level per user
    val currentLevels = mutableMapOf<Int, String>()

    // Fuzzy output value per user (used as "tingkat kesulitan sebelumnya" input)
    val fuzzyOutputValues = mutableMapOf<Int, Double>()

    // Riwayat nilai siswa
    val nilaiSiswaList = mutableListOf<NilaiSiswa>()

    var currentUser: User? = null

    fun getUnlockedLevels(userId: Int): MutableSet<String> {
        return unlockedLevels.getOrPut(userId) { mutableSetOf("Pre-test") }
    }

    fun unlockLevel(userId: Int, level: String) {
        getUnlockedLevels(userId).add(level)
    }

    fun setCurrentLevel(userId: Int, level: String) {
        currentLevels[userId] = level
    }

    fun getCurrentLevel(userId: Int): String? {
        return currentLevels[userId]
    }

    fun setFuzzyOutputValue(userId: Int, value: Double) {
        fuzzyOutputValues[userId] = value
    }

    fun getFuzzyOutputValue(userId: Int): Double {
        return fuzzyOutputValues[userId] ?: 0.0
    }

    fun addNilaiSiswa(nilai: NilaiSiswa) {
        nilaiSiswaList.add(nilai)
    }

    fun getNilaiByUserId(userId: String): List<NilaiSiswa> {
        return nilaiSiswaList.filter { it.siswaId == userId }
    }

    fun getAllNilai(): List<NilaiSiswa> {
        return nilaiSiswaList.toList()
    }

    fun addUser(user: User, password: String) {
        users.add(user)
        passwords[user.username] = password
    }

    fun updateUser(user: User) {
        val index = users.indexOfFirst { it.id == user.id }
        if (index != -1) {
            users[index] = user
        }
    }

    fun deleteUser(userId: Int) {
        val user = users.find { it.id == userId }
        if (user != null) {
            users.removeAll { it.id == userId }
            passwords.remove(user.username)
        }
    }
}
