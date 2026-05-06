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

    val tumbuhanList = listOf(
        Tumbuhan(
            id = 1,
            nama = "Apa Itu Tumbuhan?",
            deskripsi = "Mengenal tumbuhan dan bagian-bagiannya",
            manfaat = "Tumbuhan adalah makhluk hidup.\n\nTumbuhan bisa hidup di darat dan di air.\n\nTumbuhan memiliki bagian-bagian, seperti: akar, batang, dan daun.\n\nTumbuhan biasanya berwarna hijau.\n\nNama-Nama Bagian Tumbuhan:\n• Akar — bagian yang ada di dalam tanah\n• Batang — bagian yang tegak\n• Daun — bagian yang berwarna hijau\n• Bunga — bagian yang berwarna-warni\n• Buah — bagian yang bisa dimakan",
            imageRes = R.drawable.img_padi
        ),
        Tumbuhan(
            id = 2,
            nama = "Bahan Makanan",
            deskripsi = "Tumbuhan sebagai bahan makanan",
            manfaat = "Manfaat Tumbuhan untuk Manusia\n\n1. Sebagai Bahan Makanan\n\n• Padi → bijinya untuk dimasak jadi nasi\n• Pisang → buahnya untuk dimakan\n• Singkong → umbinya untuk direbus/digoreng\n• Jagung → bijinya bisa dibuat tepung",
            imageRes = R.drawable.img_padi
        ),
        Tumbuhan(
            id = 3,
            nama = "Bahan Obat",
            deskripsi = "Tumbuhan sebagai bahan obat",
            manfaat = "2. Sebagai Bahan Obat\n\n• Jahe → untuk obat masuk angin\n• Kunyit → untuk obat luka\n• Lidah buaya → untuk obat luka bakar\n• Daun sirih → untuk obat sariawan",
            imageRes = R.drawable.img_lidah_buaya
        ),
        Tumbuhan(
            id = 4,
            nama = "Bahan Bangunan",
            deskripsi = "Tumbuhan sebagai bahan bangunan",
            manfaat = "3. Sebagai Bahan Bangunan\n\n• Jati → kayunya untuk membuat rumah dan meja kursi\n• Bambu → batangnya untuk membuat pagar",
            imageRes = R.drawable.img_jati
        ),
        Tumbuhan(
            id = 5,
            nama = "Bahan Sandang",
            deskripsi = "Tumbuhan sebagai bahan sandang/pakaian",
            manfaat = "4. Sebagai Bahan Sandang\n\n• Kapas → untuk dibuat menjadi benang dan kain\n• Pandan → daunnya untuk dianyam menjadi tas/tikar",
            imageRes = R.drawable.img_kapas
        ),
        Tumbuhan(
            id = 6,
            nama = "Pewarna Alami",
            deskripsi = "Tumbuhan sebagai pewarna alami",
            manfaat = "5. Sebagai Pewarna Alami\n\n• Kunyit → menghasilkan warna kuning\n• Buah naga → menghasilkan warna merah\n• Wortel → menghasilkan warna oren",
            imageRes = R.drawable.img_sayuran
        ),
        Tumbuhan(
            id = 7,
            nama = "Bahan Industri",
            deskripsi = "Tumbuhan sebagai bahan industri",
            manfaat = "6. Sebagai Bahan Industri\n\n• Kelapa sawit → dibuat menjadi minyak goreng\n• Pohon karet → getahnya dibuat menjadi ban mobil/motor",
            imageRes = R.drawable.img_padi
        ),
        Tumbuhan(
            id = 8,
            nama = "Pohon Kelapa",
            deskripsi = "Contoh khusus tumbuhan yang sangat bermanfaat",
            manfaat = "Contoh Khusus: Pohon Kelapa\n\nMari kita pelajari satu contoh tumbuhan yang sangat bermanfaat: Pohon Kelapa\n\nBagian-bagian Pohon Kelapa:\n1. Buah Kelapa\n2. Batang Kelapa\n3. Daun Kelapa\n\nManfaat Buah Kelapa:\n• Air kelapa → diminum sebagai minuman segar\n• Daging kelapa → dibuat santan untuk masak\n• Tempurung kelapa → dibuat gelas, mangkuk, dan hiasan\n\nManfaat Batang Kelapa:\n• Kayu kelapa → dibuat meja, dan kursi\n\nManfaat Daun Kelapa:\n• Lidi → dibuat sapu\n• Daun muda → dibuat ketupat",
            imageRes = R.drawable.img_padi
        )
    )

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
