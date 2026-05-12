package com.app.manfaattumbuhan.presentation.guru.profil

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.FileUploadHelper
import com.app.manfaattumbuhan.data.remote.model.UpdateGuruProfilRequest
import com.app.manfaattumbuhan.databinding.FragmentProfilGuruBinding
import com.app.manfaattumbuhan.presentation.login.LoginActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class ProfilGuruFragment : Fragment() {

    private var _binding: FragmentProfilGuruBinding? = null
    private val binding get() = _binding!!

    private val apiService = ApiConfig.createService<ApiService>()

    private lateinit var pickFotoLauncher: ActivityResultLauncher<String>
    private var dialogFotoPreview: ImageView? = null
    private var dialogFotoProgress: ProgressBar? = null
    private var dialogFotoStatus: TextView? = null
    private var uploadedFotoUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickFotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleFotoSelected(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfilGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())

        loadProfilFromApi()

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditProfil.setOnClickListener {
            showEditProfilDialog()
        }

        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadProfilFromApi() {
        loadProfilData()

        val token = TokenManager.getToken()
        if (token.isBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getGuruProfil(token)
                if (response.isSuccessful && response.body()?.success == true) {
                    val guru = response.body()!!.data!!
                    TokenManager.saveGuruInfo(
                        nip = guru.nip ?: "",
                        sekolah = guru.sekolah ?: "",
                        mapel = guru.mapel ?: ""
                    )
                    if (!guru.foto_profil.isNullOrBlank()) {
                        TokenManager.saveGuruFoto(guru.foto_profil)
                    }
                    val currentToken = TokenManager.getToken()
                    val userId = TokenManager.getUserId()
                    TokenManager.saveGuruLogin(
                        token = currentToken.removePrefix("Bearer "),
                        id = userId,
                        nama = guru.nama,
                        nip = guru.nip,
                        sekolah = guru.sekolah,
                        mapel = guru.mapel,
                        fotoProfil = guru.foto_profil
                    )
                    loadProfilData()
                }
            } catch (_: Exception) {
                // fallback to local data
            }
        }
    }

    private fun loadProfilData() {
        binding.tvNamaGuru.text = TokenManager.getUserName()
        binding.tvNipValue.text = TokenManager.getGuruNip().ifBlank { "-" }
        binding.tvSekolahValue.text = TokenManager.getGuruSekolah().ifBlank { "-" }
        binding.tvMapelValue.text = TokenManager.getGuruMapel().ifBlank { "-" }

        val fotoUrl = TokenManager.getGuruFoto()
        if (fotoUrl.isNotBlank() && fotoUrl != "null" && fotoUrl.startsWith("http")) {
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.avatar_guru)
                .error(R.drawable.avatar_guru)
                .into(binding.imgAvatarGuru)
        } else {
            binding.imgAvatarGuru.setImageResource(R.drawable.avatar_guru)
        }
    }

    private fun showEditProfilDialog() {
        uploadedFotoUrl = null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profil_guru, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etEditNamaGuru)
        val etNip = dialogView.findViewById<EditText>(R.id.etEditNipGuru)
        val etSekolah = dialogView.findViewById<EditText>(R.id.etEditSekolahGuru)
        val etMapel = dialogView.findViewById<EditText>(R.id.etEditMapelGuru)
        val etPassword = dialogView.findViewById<EditText>(R.id.etEditPasswordGuru)
        val etPasswordConfirm = dialogView.findViewById<EditText>(R.id.etEditPasswordConfirmGuru)
        val btnPilihFoto = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihFotoGuru)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFotoGuru)
        val progressFoto = dialogView.findViewById<ProgressBar>(R.id.progressFotoGuru)
        val tvFotoStatus = dialogView.findViewById<TextView>(R.id.tvFotoGuruStatus)

        dialogFotoPreview = imgPreview
        dialogFotoProgress = progressFoto
        dialogFotoStatus = tvFotoStatus

        etNama.setText(TokenManager.getUserName())
        etNip.setText(TokenManager.getGuruNip())
        etSekolah.setText(TokenManager.getGuruSekolah())
        etMapel.setText(TokenManager.getGuruMapel())

        val existingFoto = TokenManager.getGuruFoto()
        if (existingFoto.isNotBlank()) {
            imgPreview.visibility = View.VISIBLE
            Glide.with(this).load(existingFoto).into(imgPreview)
            btnPilihFoto.text = "Ganti Foto"
        }

        btnPilihFoto.setOnClickListener {
            pickFotoLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profil Guru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                if (nama.isBlank()) {
                    Toast.makeText(requireContext(), "Nama tidak boleh kosong", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val password = etPassword.text.toString().trim()
                val passwordConfirm = etPasswordConfirm.text.toString().trim()

                if (password.isNotBlank() && password != passwordConfirm) {
                    Toast.makeText(requireContext(), "Password tidak cocok", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                if (password.isNotBlank() && password.length < 6) {
                    Toast.makeText(requireContext(), "Password minimal 6 karakter", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val nip = etNip.text.toString().trim()
                val sekolah = etSekolah.text.toString().trim()
                val mapel = etMapel.text.toString().trim()
                val fotoUrl = uploadedFotoUrl

                updateProfilViaApi(nama, nip, sekolah, mapel, password, fotoUrl)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun updateProfilViaApi(
        nama: String,
        nip: String,
        sekolah: String,
        mapel: String,
        password: String,
        fotoUrl: String?
    ) {
        val token = TokenManager.getToken()
        val userId = TokenManager.getUserId()

        val request = UpdateGuruProfilRequest(
            nama = nama,
            password = if (password.isNotBlank()) password else null,
            nip = nip.ifBlank { null },
            sekolah = sekolah.ifBlank { null },
            mapel = mapel.ifBlank { null },
            foto_profil = fotoUrl
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.updateGuruProfil(token, request)
                if (response.isSuccessful && response.body()?.success == true) {
                    val guru = response.body()!!.data!!
                    TokenManager.saveGuruLogin(
                        token = token.removePrefix("Bearer "),
                        id = userId,
                        nama = guru.nama,
                        nip = guru.nip,
                        sekolah = guru.sekolah,
                        mapel = guru.mapel,
                        fotoProfil = guru.foto_profil
                    )
                    loadProfilData()
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    saveProfilLocally(nama, nip, sekolah, mapel, fotoUrl, token, userId)
                }
            } catch (_: Exception) {
                saveProfilLocally(nama, nip, sekolah, mapel, fotoUrl, token, userId)
            }
        }
    }

    private fun saveProfilLocally(
        nama: String,
        nip: String,
        sekolah: String,
        mapel: String,
        fotoUrl: String?,
        token: String,
        userId: String
    ) {
        TokenManager.saveGuruLogin(
            token = token.removePrefix("Bearer "),
            id = userId,
            nama = nama,
            nip = nip,
            sekolah = sekolah,
            mapel = mapel,
            fotoProfil = fotoUrl ?: TokenManager.getGuruFoto()
        )
        TokenManager.saveGuruInfo(nip, sekolah, mapel)
        if (fotoUrl != null) {
            TokenManager.saveGuruFoto(fotoUrl)
        }
        loadProfilData()
        Toast.makeText(requireContext(), "Profil berhasil diperbarui (lokal)", Toast.LENGTH_SHORT).show()
    }

    private fun handleFotoSelected(uri: Uri) {
        dialogFotoPreview?.let { preview ->
            preview.visibility = View.VISIBLE
            Glide.with(this).load(uri).into(preview)
        }
        dialogFotoProgress?.visibility = View.VISIBLE
        dialogFotoStatus?.visibility = View.VISIBLE
        dialogFotoStatus?.text = "Mengupload foto..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = FileUploadHelper.uploadFile(requireContext(), uri, "foto")
            dialogFotoProgress?.visibility = View.GONE
            result.onSuccess { uploadResponse ->
                uploadedFotoUrl = uploadResponse.url
                dialogFotoStatus?.text = "Foto berhasil diupload"
            }
            result.onFailure { error ->
                uploadedFotoUrl = null
                dialogFotoStatus?.text = "Gagal upload: ${error.message}"
                dialogFotoStatus?.setTextColor(resources.getColor(R.color.red_button, null))
                dialogFotoPreview?.visibility = View.GONE
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Keluar")
            .setMessage("Apakah Anda yakin ingin keluar?")
            .setNegativeButton("Batal", null)
            .setPositiveButton("Ya") { _, _ ->
                performLogout()
            }
            .show()
    }

    private fun performLogout() {
        TokenManager.clear()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
