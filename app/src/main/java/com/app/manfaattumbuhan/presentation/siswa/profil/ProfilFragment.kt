package com.app.manfaattumbuhan.presentation.siswa.profil

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
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.FileUploadHelper
import com.app.manfaattumbuhan.data.remote.model.UpdateProfilRequest
import com.app.manfaattumbuhan.databinding.FragmentProfilBinding
import com.app.manfaattumbuhan.presentation.login.LoginActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class ProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiConfig.createService<ApiService>()
    private var uploadedFotoUrl: String? = null

    private lateinit var pickFotoLauncher: ActivityResultLauncher<String>
    private var dialogFotoPreview: ImageView? = null
    private var dialogFotoProgress: ProgressBar? = null
    private var dialogFotoStatus: TextView? = null

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
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())

        loadProfilData()

        binding.btnEditProfil.setOnClickListener {
            showEditProfilDialog()
        }

        binding.btnKeluar.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun loadProfilData() {
        binding.tvNama.text = TokenManager.getUserName()
        binding.tvKelas.text = TokenManager.getUserKelas()
        binding.tvSekolah.text = TokenManager.getUserNisn()

        val fotoUrl = TokenManager.getSiswaFoto()
        if (fotoUrl.isNotBlank() && fotoUrl != "null" && fotoUrl.startsWith("http")) {
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.avatar_siswa)
                .error(R.drawable.avatar_siswa)
                .into(binding.imgAvatar)
        } else {
            binding.imgAvatar.setImageResource(R.drawable.avatar_siswa)
        }
    }

    private fun showEditProfilDialog() {
        uploadedFotoUrl = null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profil_siswa, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etEditNama)
        val etPassword = dialogView.findViewById<EditText>(R.id.etEditPassword)
        val btnPilihFoto = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihFotoSiswa)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFotoSiswa)
        val progressFoto = dialogView.findViewById<ProgressBar>(R.id.progressFotoSiswa)
        val tvFotoStatus = dialogView.findViewById<TextView>(R.id.tvFotoSiswaStatus)

        dialogFotoPreview = imgPreview
        dialogFotoProgress = progressFoto
        dialogFotoStatus = tvFotoStatus

        val currentNama = TokenManager.getUserName()
        etNama.setText(currentNama)

        btnPilihFoto.setOnClickListener {
            pickFotoLauncher.launch("image/*")
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Profil")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val inputNama = etNama.text.toString().trim()
                val password = etPassword.text.toString().trim().ifBlank { null }
                val fotoProfil = uploadedFotoUrl

                val isNamaChanged = inputNama.isNotBlank() && inputNama != currentNama
                val isPasswordChanged = password != null
                val isFotoChanged = fotoProfil != null

                val tvError = dialogView.findViewById<TextView>(R.id.tvErrorStatus)
                tvError.visibility = View.GONE

                if (!isNamaChanged && !isPasswordChanged && !isFotoChanged) {
                    tvError.text = "⚠️ Tidak ada perubahan yang disimpan"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (password != null && password.length < 7) {
                    tvError.text = "⚠️ Password minimal 7 karakter"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                val updates = mutableListOf<String>()
                if (isNamaChanged) updates.add("Nama")
                if (isPasswordChanged) updates.add("Password")
                if (isFotoChanged) updates.add("Foto")

                val successMsg = when {
                    updates.size == 1 -> "${updates[0]} berhasil diperbarui"
                    updates.size == 2 -> "${updates[0]} dan ${updates[1]} berhasil diperbarui"
                    updates.size > 2 -> "Nama, Password, dan Foto berhasil diperbarui"
                    else -> "Profil berhasil diperbarui"
                }

                dialog.dismiss()
                simpanProfil(inputNama.ifBlank { null }, password, fotoProfil, successMsg)
            }
        }
        dialog.show()
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

    private fun simpanProfil(nama: String?, password: String?, fotoProfil: String?, successMsg: String) {
        val token = TokenManager.getToken()
        val userId = TokenManager.getUserId()
        if (token.isBlank() || userId.isBlank()) {
            Toast.makeText(requireContext(), "Sesi tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.updateProfilSiswa(
                    "eq.$userId",
                    UpdateProfilRequest(nama, password, fotoProfil)
                )
                if (response.isSuccessful) {
                    val updatedSiswa = response.body()?.firstOrNull()
                    if (updatedSiswa != null) {
                        TokenManager.saveSiswaLogin(
                            "",
                            updatedSiswa.id,
                            updatedSiswa.nama,
                            updatedSiswa.nisn,
                            updatedSiswa.kelas,
                            updatedSiswa.foto_profil
                        )
                        if (updatedSiswa.foto_profil != null) {
                            TokenManager.saveSiswaFoto(updatedSiswa.foto_profil)
                        }
                    }
                    loadProfilData()
                    uploadedFotoUrl = null
                    Toast.makeText(requireContext(), successMsg, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Gagal memperbarui profil: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
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
        Glide.get(requireContext()).clearMemory()
        binding.imgAvatar.setImageResource(R.drawable.avatar_siswa)
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
