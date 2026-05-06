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

    fun saveSiswaLogin(token: String, id: String, nama: String, nim: String, kelas: String) {
        prefs.edit().apply {
            putString(KEY_TOKEN, "Bearer $token")
            putString(KEY_USER_ROLE, "SISWA")
            putString(KEY_USER_ID, id)
            putString(KEY_USER_NAME, nama)
            putString(KEY_USER_NIM, nim)
            putString(KEY_USER_KELAS, kelas)
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

    fun saveGuruFoto(url: String) {
        prefs.edit().putString(KEY_GURU_FOTO, url).apply()
    }

    fun isLoggedIn(): Boolean = getToken().isNotBlank()

    fun clear() {
        prefs.edit().clear().apply()
    }
}
