package com.app.manfaattumbuhan.presentation.login

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.LoginGuruRequest
import com.app.manfaattumbuhan.data.remote.model.LoginSiswaRequest
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
        val response = apiService.loginGuru(LoginGuruRequest(nama, password))
        if (response.isSuccessful && response.body()?.success == true) {
            val data = response.body()!!.data!!
            _loginResult.postValue(
                LoginResult.GuruSuccess(
                    token = data.token,
                    id = data.guru.id,
                    nama = data.guru.nama,
                    nip = data.guru.nip,
                    sekolah = data.guru.sekolah,
                    mapel = data.guru.mapel,
                    fotoProfil = data.guru.foto_profil
                )
            )
        } else {
            val errorMsg = "Username atau password salah"
            _loginResult.postValue(LoginResult.Error(errorMsg))
        }
    }

    private suspend fun loginSiswa(nisn: String, password: String) {
        val response = apiService.loginSiswa(LoginSiswaRequest(nisn, password))
        if (response.isSuccessful && response.body()?.success == true) {
            val data = response.body()!!.data!!
            _loginResult.postValue(
                LoginResult.SiswaSuccess(
                    token = data.token,
                    id = data.siswa.id,
                    nama = data.siswa.nama,
                    nisn = data.siswa.nisn,
                    kelas = data.siswa.kelas,
                    fotoProfil = data.siswa.foto_profil
                )
            )
        } else {
            val errorMsg = "NISN atau password salah"
            _loginResult.postValue(LoginResult.Error(errorMsg))
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
