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
    private const val KEY_PRETEST_DONE_PREFIX = "pretest_done_"
    private const val KEY_UNLOCKED_LEVELS_PREFIX = "unlocked_levels_"
    private const val KEY_CURRENT_LEVEL_PREFIX = "current_level_"
    private const val KEY_FUZZY_OUTPUT_PREFIX = "fuzzy_output_"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveGuruLogin(
        token: String,
        id: String,
        nama: String,
        nip: String? = null,
        sekolah: String? = null,
        mapel: String? = null,
        fotoProfil: String? = null
    ) {
        prefs.edit().apply {
            putString(KEY_TOKEN, "Bearer $token")
            putString(KEY_USER_ROLE, "GURU")
            putString(KEY_USER_ID, id)
            putString(KEY_USER_NAME, nama)
            putString(KEY_GURU_NIP, nip ?: "")
            putString(KEY_GURU_SEKOLAH, sekolah ?: "")
            putString(KEY_GURU_MAPEL, mapel ?: "")
            putString(KEY_GURU_FOTO, fotoProfil ?: "")
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
    fun getGuruNip(): String = prefs.getString(KEY_GURU_NIP, "") ?: ""
    fun getGuruSekolah(): String = prefs.getString(KEY_GURU_SEKOLAH, "") ?: ""
    fun getGuruMapel(): String = prefs.getString(KEY_GURU_MAPEL, "") ?: ""

    fun saveGuruFoto(url: String) {
        prefs.edit().putString(KEY_GURU_FOTO, url).apply()
    }

    fun saveSiswaFoto(url: String) {
        prefs.edit().putString(KEY_SISWA_FOTO, url).apply()
    }

    fun saveGuruInfo(sekolah: String, mapel: String) {
        prefs.edit().apply {
            putString(KEY_GURU_SEKOLAH, sekolah)
            putString(KEY_GURU_MAPEL, mapel)
            apply()
        }
    }

    fun isLoggedIn(): Boolean = getToken().isNotBlank()

    fun clear() {

        prefs.edit().apply {
            remove(KEY_TOKEN)
            remove(KEY_USER_ROLE)
            remove(KEY_USER_ID)
            remove(KEY_USER_NAME)
            remove(KEY_USER_NIM)
            remove(KEY_USER_KELAS)
            apply()
        }
    }

    fun isPretestDone(userId: String): Boolean {
        if (userId.isBlank()) return false
        return prefs.getBoolean(KEY_PRETEST_DONE_PREFIX + userId, false)
    }

    fun setPretestDone(userId: String, done: Boolean) {
        if (userId.isBlank()) return
        prefs.edit().putBoolean(KEY_PRETEST_DONE_PREFIX + userId, done).apply()
    }

    fun saveUnlockedLevels(userId: String, levels: Set<String>) {
        if (userId.isBlank()) return
        prefs.edit().putString(KEY_UNLOCKED_LEVELS_PREFIX + userId, levels.joinToString(",")).apply()
    }

    fun getUnlockedLevels(userId: String): Set<String> {
        if (userId.isBlank()) return emptySet()
        val raw = prefs.getString(KEY_UNLOCKED_LEVELS_PREFIX + userId, "") ?: ""
        if (raw.isBlank()) return emptySet()
        return raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    fun saveCurrentLevel(userId: String, level: String) {
        if (userId.isBlank()) return
        prefs.edit().putString(KEY_CURRENT_LEVEL_PREFIX + userId, level).apply()
    }

    fun getCurrentLevel(userId: String): String? {
        if (userId.isBlank()) return null
        return prefs.getString(KEY_CURRENT_LEVEL_PREFIX + userId, null)
    }

    fun saveFuzzyOutputValue(userId: String, value: Double) {
        if (userId.isBlank()) return
        prefs.edit().putFloat(KEY_FUZZY_OUTPUT_PREFIX + userId, value.toFloat()).apply()
    }

    fun getFuzzyOutputValue(userId: String): Double {
        if (userId.isBlank()) return 0.0
        return prefs.getFloat(KEY_FUZZY_OUTPUT_PREFIX + userId, 0f).toDouble()
    }
}
