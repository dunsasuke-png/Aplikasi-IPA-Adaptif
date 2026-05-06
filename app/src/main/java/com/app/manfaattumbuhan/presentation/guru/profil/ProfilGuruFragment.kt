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
import com.app.manfaattumbuhan.data.remote.FileUploadHelper
import com.app.manfaattumbuhan.databinding.FragmentProfilGuruBinding
import com.app.manfaattumbuhan.presentation.login.LoginActivity
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class ProfilGuruFragment : Fragment() {

    private var _binding: FragmentProfilGuruBinding? = null
    private val binding get() = _binding!!

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

        loadProfilData()

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnEditProfil.setOnClickListener {
            showEditProfilDialog()
        }

        binding.btnLogout.setOnClickListener {
            TokenManager.clear()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun loadProfilData() {
        binding.tvNamaGuru.text = TokenManager.getUserName()

        val fotoUrl = TokenManager.getGuruFoto()
        if (fotoUrl.isNotBlank()) {
            Glide.with(this).load(fotoUrl).placeholder(R.drawable.avatar_guru).into(binding.imgAvatarGuru)
        }
    }

    private fun showEditProfilDialog() {
        uploadedFotoUrl = null
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profil_guru, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etEditNamaGuru)
        val btnPilihFoto = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihFotoGuru)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFotoGuru)
        val progressFoto = dialogView.findViewById<ProgressBar>(R.id.progressFotoGuru)
        val tvFotoStatus = dialogView.findViewById<TextView>(R.id.tvFotoGuruStatus)

        dialogFotoPreview = imgPreview
        dialogFotoProgress = progressFoto
        dialogFotoStatus = tvFotoStatus

        etNama.setText(TokenManager.getUserName())

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

                val token = TokenManager.getToken()
                val userId = TokenManager.getUserId()
                TokenManager.saveGuruLogin(token.removePrefix("Bearer "), userId, nama)

                if (uploadedFotoUrl != null) {
                    TokenManager.saveGuruFoto(uploadedFotoUrl!!)
                }

                loadProfilData()
                Toast.makeText(requireContext(), "Profil berhasil diperbarui", Toast.LENGTH_SHORT).show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
