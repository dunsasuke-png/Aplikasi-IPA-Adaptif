package com.app.manfaattumbuhan.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.text.InputType
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.ActivityLoginBinding
import com.app.manfaattumbuhan.domain.model.UserRole
import com.app.manfaattumbuhan.presentation.guru.GuruActivity
import com.app.manfaattumbuhan.presentation.siswa.SiswaActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        TokenManager.init(this)

        if (TokenManager.isLoggedIn()) {
            val role = TokenManager.getUserRole()
            val intent = if (role == "GURU") {
                Intent(this, GuruActivity::class.java)
            } else {
                Intent(this, SiswaActivity::class.java)
            }
            intent.putExtra("USER_NAME", TokenManager.getUserName())
            intent.putExtra("USER_ID", TokenManager.getUserId())
            startActivity(intent)
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]

        setupRoleSelection()
        setupLoginButton()
        observeViewModel()
    }

    private fun setupRoleSelection() {
        binding.cardSiswa.setOnClickListener {
            viewModel.selectRole(UserRole.SISWA)
        }

        binding.cardGuru.setOnClickListener {
            viewModel.selectRole(UserRole.GURU)
        }

        viewModel.selectedRole.observe(this) { role ->
            when (role) {
                UserRole.SISWA -> {
                    binding.cardSiswa.setCardBackgroundColor(getColor(R.color.green_light))
                    binding.cardSiswa.strokeColor = getColor(R.color.green_primary)
                    binding.cardSiswa.strokeWidth = 4
                    binding.cardGuru.setCardBackgroundColor(getColor(R.color.white))
                    binding.cardGuru.strokeColor = getColor(R.color.gray_border)
                    binding.cardGuru.strokeWidth = 2
                    binding.layoutLoginForm.visibility = View.VISIBLE
                    binding.tilUsername.hint = getString(R.string.nisn)
                    binding.etUsername.inputType = InputType.TYPE_CLASS_NUMBER
                    binding.etUsername.hint = null
                    binding.etUsername.text?.clear()
                }
                UserRole.GURU -> {
                    binding.cardGuru.setCardBackgroundColor(getColor(R.color.green_light))
                    binding.cardGuru.strokeColor = getColor(R.color.green_primary)
                    binding.cardGuru.strokeWidth = 4
                    binding.cardSiswa.setCardBackgroundColor(getColor(R.color.white))
                    binding.cardSiswa.strokeColor = getColor(R.color.gray_border)
                    binding.cardSiswa.strokeWidth = 2
                    binding.layoutLoginForm.visibility = View.VISIBLE
                    binding.tilUsername.hint = getString(R.string.username)
                    binding.etUsername.inputType = InputType.TYPE_CLASS_TEXT
                    binding.etUsername.hint = null
                    binding.etUsername.text?.clear()
                }
                else -> {}
            }
        }
    }

    private fun setupLoginButton() {
        binding.btnLogin.setOnClickListener {
            val identifier = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            viewModel.login(identifier, password)
        }
    }

    private fun observeViewModel() {
        viewModel.isLoading.observe(this) { loading ->
            binding.btnLogin.isEnabled = !loading
            binding.btnLogin.text = if (loading) "Memuat..." else "Masuk"
        }

        viewModel.loginResult.observe(this) { result ->
            when (result) {
                is LoginResult.GuruSuccess -> {
                    TokenManager.saveGuruLogin(
                        token = result.token,
                        id = result.id,
                        nama = result.nama,
                        nip = result.nip,
                        sekolah = result.sekolah,
                        mapel = result.mapel,
                        fotoProfil = result.fotoProfil
                    )
                    Toast.makeText(this, "Login berhasil! Selamat datang, ${result.nama}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, GuruActivity::class.java)
                    intent.putExtra("USER_NAME", result.nama)
                    intent.putExtra("USER_ID", result.id)
                    startActivity(intent)
                    finish()
                }
                is LoginResult.SiswaSuccess -> {
                    TokenManager.saveSiswaLogin(result.token, result.id, result.nama, result.nisn, result.kelas, result.fotoProfil)
                    Toast.makeText(this, "Login berhasil! Selamat datang, ${result.nama}", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, SiswaActivity::class.java)
                    intent.putExtra("USER_NAME", result.nama)
                    intent.putExtra("USER_ID", result.id)
                    startActivity(intent)
                    finish()
                }
                is LoginResult.Error -> {
                    Toast.makeText(this, result.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
