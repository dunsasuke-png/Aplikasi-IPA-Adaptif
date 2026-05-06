package com.app.manfaattumbuhan.data.local

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private const val PREF_NAME = "ipa_adaptif_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ROLE = "user_role"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_NIM = "user_nim"
    private const val KEY_USER_KELAS = "user_kelas"
    private const val KEY_GURU_FOTO = "guru_foto"
    private const val KEY_SISWA_FOTO = "siswa_foto"
    private const val KEY_GURU_NIP = "guru_nip"
    private const val KEY_GURU_SEKOLAH = "guru_sekolah"
    private const val KEY_GURU_MAPEL = "guru_mapel"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveGuruLogin(token: String, id: String, nama: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, "Bearer $token")
            putString(KEY_USER_ROLE, "GURU")
            putString(KEY_USER_ID, id)
            putString(KEY_USER_NAME, nama)
            apply()
        }
    }

    fun saveSiswaLogin(token: String, id: String, nama: String, nim: String, kelas: String, fotoProfil: String? = null) {
        prefs.edit().apply {
            putString(KEY_TOKEN, "Bearer $token")
            putString(KEY_USER_ROLE, "SISWA")
            putString(KEY_USER_ID, id)
            putString(KEY_USER_NAME, nama)
            putString(KEY_USER_NIM, nim)
            putString(KEY_USER_KELAS, kelas)
            putString(KEY_SISWA_FOTO, fotoProfil ?: "")
            apply()
        }
    }

    fun getToken(): String = prefs.getString(KEY_TOKEN, "") ?: ""
    fun getUserRole(): String = prefs.getString(KEY_USER_ROLE, "") ?: ""
    fun getUserId(): String = prefs.getString(KEY_USER_ID, "") ?: ""
    fun getUserName(): String = prefs.getString(KEY_USER_NAME, "") ?: ""
    fun getUserNim(): String = prefs.getString(KEY_USER_NIM, "") ?: ""
    fun getUserKelas(): String = prefs.getString(KEY_USER_KELAS, "") ?: ""
    fun getGuruFoto(): String = prefs.getString(KEY_GURU_FOTO, "") ?: ""
    fun getSiswaFoto(): String = prefs.getString(KEY_SISWA_FOTO, "") ?: ""
    fun getGuruNip(): String = prefs.getString(KEY_GURU_NIP, "198507152010012009") ?: "198507152010012009"
    fun getGuruSekolah(): String = prefs.getString(KEY_GURU_SEKOLAH, "SLB Negeri Harapan") ?: "SLB Negeri Harapan"
    fun getGuruMapel(): String = prefs.getString(KEY_GURU_MAPEL, "IPA") ?: "IPA"

    fun saveGuruFoto(url: String) {
        prefs.edit().putString(KEY_GURU_FOTO, url).apply()
    }

    fun saveSiswaFoto(url: String) {
        prefs.edit().putString(KEY_SISWA_FOTO, url).apply()
    }

    fun saveGuruInfo(nip: String, sekolah: String, mapel: String) {
        prefs.edit().apply {
            putString(KEY_GURU_NIP, nip)
            putString(KEY_GURU_SEKOLAH, sekolah)
            putString(KEY_GURU_MAPEL, mapel)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = getToken().isNotBlank()

    fun clear() {
        val guruFoto = prefs.getString(KEY_GURU_FOTO, "") ?: ""
        val siswaFoto = prefs.getString(KEY_SISWA_FOTO, "") ?: ""
        val guruNip = prefs.getString(KEY_GURU_NIP, "") ?: ""
        val guruSekolah = prefs.getString(KEY_GURU_SEKOLAH, "") ?: ""
        val guruMapel = prefs.getString(KEY_GURU_MAPEL, "") ?: ""
        prefs.edit().clear().apply()
        prefs.edit().apply {
            putString(KEY_GURU_FOTO, guruFoto)
            putString(KEY_SISWA_FOTO, siswaFoto)
            putString(KEY_GURU_NIP, guruNip)
            putString(KEY_GURU_SEKOLAH, guruSekolah)
            putString(KEY_GURU_MAPEL, guruMapel)
            apply()
        }
    }
}
