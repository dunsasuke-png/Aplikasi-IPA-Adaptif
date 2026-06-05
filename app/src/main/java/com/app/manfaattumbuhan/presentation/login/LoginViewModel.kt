package com.app.manfaattumbuhan.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.RpcLoginGuruRequest
import com.app.manfaattumbuhan.data.remote.model.RpcLoginSiswaRequest
import com.app.manfaattumbuhan.domain.model.UserRole
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {

    private val apiService = ApiConfig.createService<ApiService>()

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    private val _selectedRole = MutableLiveData<UserRole>()
    val selectedRole: LiveData<UserRole> = _selectedRole

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    fun selectRole(role: UserRole) {
        _selectedRole.value = role
    }

    fun login(identifier: String, password: String) {
        val role = _selectedRole.value
        if (role == null) {
            _loginResult.value = LoginResult.Error("Pilih peran terlebih dahulu")
            return
        }

        if (identifier.isBlank() || password.isBlank()) {
            val msg = if (role == com.app.manfaattumbuhan.domain.model.UserRole.GURU)
                "Username dan password harus diisi"
            else
                "NISN dan password harus diisi"
            _loginResult.value = LoginResult.Error(msg)
            return
        }

        _isLoading.value = true

        viewModelScope.launch {
            try {
                when (role) {
                    UserRole.GURU -> loginGuru(identifier, password)
                    UserRole.SISWA -> loginSiswa(identifier, password)
                }
            } catch (e: Exception) {
                _loginResult.postValue(LoginResult.Error("Gagal terhubung ke server: ${e.message}"))
            } finally {
                _isLoading.postValue(false)
            }
        }
    }

    private suspend fun loginGuru(nama: String, password: String) {
        // Supabase RPC: verify bcrypt password via PostgreSQL function
        val response = apiService.loginGuru(
            RpcLoginGuruRequest(p_nama = nama, p_password = password)
        )
        if (response.isSuccessful) {
            val guru = response.body()?.firstOrNull()
            if (guru != null) {
                _loginResult.postValue(
                    LoginResult.GuruSuccess(
                        token = "",
                        id = guru.id,
                        nama = guru.nama,
                        nip = guru.nip,
                        sekolah = guru.sekolah,
                        mapel = guru.mapel,
                        fotoProfil = guru.foto_profil
                    )
                )
            } else {
                _loginResult.postValue(LoginResult.Error("Username atau password salah"))
            }
        } else {
            _loginResult.postValue(LoginResult.Error("Gagal login: ${response.code()}"))
        }
    }

    private suspend fun loginSiswa(nisn: String, password: String) {
        // Supabase RPC: verify bcrypt password via PostgreSQL function
        val response = apiService.loginSiswa(
            RpcLoginSiswaRequest(p_nisn = nisn, p_password = password)
        )
        if (response.isSuccessful) {
            val siswa = response.body()?.firstOrNull()
            if (siswa != null) {
                _loginResult.postValue(
                    LoginResult.SiswaSuccess(
                        token = "",
                        id = siswa.id,
                        nama = siswa.nama,
                        nisn = siswa.nisn,
                        kelas = siswa.kelas,
                        fotoProfil = siswa.foto_profil
                    )
                )
            } else {
                _loginResult.postValue(LoginResult.Error("NISN atau password salah"))
            }
        } else {
            _loginResult.postValue(LoginResult.Error("Gagal login: ${response.code()}"))
        }
    }
}

sealed class LoginResult {
    data class GuruSuccess(
        val token: String,
        val id: String,
        val nama: String,
        val nip: String? = null,
        val sekolah: String? = null,
        val mapel: String? = null,
        val fotoProfil: String? = null
    ) : LoginResult()
    data class SiswaSuccess(val token: String, val id: String, val nama: String, val nisn: String, val kelas: String, val fotoProfil: String? = null) : LoginResult()
    data class Error(val message: String) : LoginResult()
}
