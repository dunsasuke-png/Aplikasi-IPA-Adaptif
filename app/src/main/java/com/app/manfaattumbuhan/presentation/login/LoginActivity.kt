package com.app.manfaattumbuhan.presentation.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
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
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        TokenManager.init(this)
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
                    binding.etUsername.hint = "NISN"
                }
                UserRole.GURU -> {
                    binding.cardGuru.setCardBackgroundColor(getColor(R.color.green_light))
                    binding.cardGuru.strokeColor = getColor(R.color.green_primary)
                    binding.cardGuru.strokeWidth = 4
                    binding.cardSiswa.setCardBackgroundColor(getColor(R.color.white))
                    binding.cardSiswa.strokeColor = getColor(R.color.gray_border)
                    binding.cardSiswa.strokeWidth = 2
                    binding.layoutLoginForm.visibility = View.VISIBLE
                    binding.etUsername.hint = "Username"
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
                    TokenManager.saveGuruLogin(result.token, result.id, result.nama)
                    val intent = Intent(this, GuruActivity::class.java)
                    intent.putExtra("USER_NAME", result.nama)
                    intent.putExtra("USER_ID", result.id)
                    startActivity(intent)
                    finish()
                }
                is LoginResult.SiswaSuccess -> {
                    TokenManager.saveSiswaLogin(result.token, result.id, result.nama, result.nim, result.kelas, result.fotoProfil)
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
