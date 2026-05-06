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
            Glide.get(requireContext()).clearMemory()
            binding.imgAvatar.setImageResource(R.drawable.avatar_siswa)
            TokenManager.clear()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadProfilData() {
        binding.tvNama.text = TokenManager.getUserName()
        binding.tvKelas.text = TokenManager.getUserKelas()
        binding.tvSekolah.text = "NISN: ${TokenManager.getUserNim()}"
        binding.tvStatusBadge.text = "SISWA AKTIF"

        val fotoUrl = TokenManager.getSiswaFoto()
        if (fotoUrl.isNotBlank()) {
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

        etNama.setText(TokenManager.getUserName())

        btnPilihFoto.setOnClickListener {
            pickFotoLauncher.launch("image/*")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profil")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim().ifBlank { null }
                val password = etPassword.text.toString().trim().ifBlank { null }
                val fotoProfil = uploadedFotoUrl

                if (nama == null && password == null && fotoProfil == null) {
                    Toast.makeText(requireContext(), "Tidak ada perubahan", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                simpanProfil(nama, password, fotoProfil)
            }
            .setNegativeButton("Batal", null)
            .show()
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

    private fun simpanProfil(nama: String?, password: String?, fotoProfil: String?) {
        val token = TokenManager.getToken()
        val userId = TokenManager.getUserId()
        if (token.isBlank() || userId.isBlank()) {
            Toast.makeText(requireContext(), "Sesi tidak valid", Toast.LENGTH_SHORT).show()
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.updateProfil(
                    token, userId,
                    UpdateProfilRequest(nama, password, fotoProfil)
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val updatedSiswa = response.body()!!.data!!
                    TokenManager.saveSiswaLogin(
                        token.removePrefix("Bearer "),
                        updatedSiswa.id,
                        updatedSiswa.nama,
                        updatedSiswa.nim,
                        updatedSiswa.kelas,
                        updatedSiswa.foto_profil
                    )
                    if (updatedSiswa.foto_profil != null) {
                        TokenManager.saveSiswaFoto(updatedSiswa.foto_profil)
                    }
                    loadProfilData()
                    uploadedFotoUrl = null
                    Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        response.body()?.message ?: "Gagal memperbarui profil",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
