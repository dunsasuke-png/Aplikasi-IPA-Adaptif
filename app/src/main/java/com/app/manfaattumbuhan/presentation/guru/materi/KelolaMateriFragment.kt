package com.app.manfaattumbuhan.presentation.guru.materi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.FileUploadHelper
import com.app.manfaattumbuhan.data.remote.model.MateriApi
import com.app.manfaattumbuhan.databinding.FragmentKelolaMateriBinding
import com.app.manfaattumbuhan.presentation.adapter.MateriGuruAdapter
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch

class KelolaMateriFragment : Fragment() {

    private var _binding: FragmentKelolaMateriBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KelolaMateriViewModel
    private lateinit var adapter: MateriGuruAdapter

    private var uploadedGambarUrl: String? = null
    private var uploadedVideoUrl: String? = null
    private var currentFotoPreview: ImageView? = null
    private var currentFotoProgress: ProgressBar? = null
    private var currentFotoStatus: TextView? = null
    private var currentFotoButton: View? = null
    private var currentVideoProgress: ProgressBar? = null
    private var currentVideoStatus: TextView? = null
    private var currentVideoButton: View? = null
    private var currentVideoName: TextView? = null

    private lateinit var pickGambarLauncher: ActivityResultLauncher<String>
    private lateinit var pickVideoLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickGambarLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleGambarSelected(it) }
        }

        pickVideoLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { handleVideoSelected(it) }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaMateriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        viewModel = ViewModelProvider(this)[KelolaMateriViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        observeData()
        viewModel.loadMateri()
    }

    private fun setupRecyclerView() {
        adapter = MateriGuruAdapter(
            onEdit = { materi -> showEditDialog(materi) },
            onDelete = { materi -> showDeleteConfirmation(materi) }
        )
        binding.rvMateri.layoutManager = LinearLayoutManager(requireContext())
        binding.rvMateri.adapter = adapter
    }

    private fun setupListeners() {
        loadGuruPhoto()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_materi_guru_to_profil)
        }

        binding.btnTambahMateri.setOnClickListener {
            showCreateDialog()
        }
    }

    private fun loadGuruPhoto() {
        val fotoUrl = TokenManager.getGuruFoto()
        if (fotoUrl.isNotBlank()) {
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.avatar_guru)
                .error(R.drawable.avatar_guru)
                .into(binding.imgProfile)
        }
    }

    private fun observeData() {
        viewModel.materiList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvMateriCount.text = "${list.size} materi"
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGambarSelected(uri: android.net.Uri) {
        currentFotoPreview?.let { preview ->
            preview.visibility = View.VISIBLE
            Glide.with(this).load(uri).into(preview)
        }
        currentFotoButton?.visibility = View.GONE
        currentFotoProgress?.visibility = View.VISIBLE
        currentFotoStatus?.visibility = View.VISIBLE
        currentFotoStatus?.text = "Mengupload gambar..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = FileUploadHelper.uploadFile(requireContext(), uri, "foto")
            currentFotoProgress?.visibility = View.GONE
            result.onSuccess { uploadResponse ->
                uploadedGambarUrl = uploadResponse.url
                currentFotoStatus?.text = "Gambar berhasil diupload"
                currentFotoButton?.visibility = View.VISIBLE
                (currentFotoButton as? com.google.android.material.button.MaterialButton)?.text = "Ganti Gambar"
            }
            result.onFailure { error ->
                uploadedGambarUrl = null
                currentFotoStatus?.text = "Gagal upload: ${error.message}"
                currentFotoButton?.visibility = View.VISIBLE
                currentFotoPreview?.visibility = View.GONE
            }
        }
    }

    private fun handleVideoSelected(uri: Uri) {
        currentVideoButton?.visibility = View.GONE
        currentVideoProgress?.visibility = View.VISIBLE
        currentVideoStatus?.visibility = View.VISIBLE
        currentVideoStatus?.text = "Mengupload video..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = FileUploadHelper.uploadFile(requireContext(), uri, "video")
            currentVideoProgress?.visibility = View.GONE
            result.onSuccess { uploadResponse ->
                uploadedVideoUrl = uploadResponse.url
                currentVideoStatus?.text = "Video berhasil diupload"
                currentVideoButton?.visibility = View.VISIBLE
                (currentVideoButton as? com.google.android.material.button.MaterialButton)?.text = "Ganti Video"
                currentVideoName?.visibility = View.VISIBLE
                currentVideoName?.text = uploadResponse.original_name
            }
            result.onFailure { error ->
                uploadedVideoUrl = null
                currentVideoStatus?.text = "Gagal upload: ${error.message}"
                currentVideoButton?.visibility = View.VISIBLE
            }
        }
    }

    private fun setupVideoFields(dialogView: View) {
        val btnPilihVideo = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihVideo)
        val progressVideo = dialogView.findViewById<ProgressBar>(R.id.progressVideo)
        val tvVideoStatus = dialogView.findViewById<TextView>(R.id.tvVideoStatus)
        val tvVideoName = dialogView.findViewById<TextView>(R.id.tvVideoName)

        currentVideoProgress = progressVideo
        currentVideoStatus = tvVideoStatus
        currentVideoButton = btnPilihVideo
        currentVideoName = tvVideoName

        btnPilihVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            pickVideoLauncher.launch(intent)
        }
    }

    private fun showCreateDialog() {
        uploadedGambarUrl = null
        uploadedVideoUrl = null

        val dialogView = layoutInflater.inflate(R.layout.dialog_materi, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaMateri)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsiMateri)
        val etManfaat = dialogView.findViewById<EditText>(R.id.etManfaatMateri)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFoto)
        val btnPilihGambar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihGambar)
        val progressGambar = dialogView.findViewById<ProgressBar>(R.id.progressGambar)
        val tvGambarStatus = dialogView.findViewById<TextView>(R.id.tvGambarStatus)

        currentFotoPreview = imgPreview
        currentFotoProgress = progressGambar
        currentFotoStatus = tvGambarStatus
        currentFotoButton = btnPilihGambar

        btnPilihGambar.setOnClickListener {
            pickGambarLauncher.launch("image/*")
        }

        setupVideoFields(dialogView)

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Materi")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val manfaat = etManfaat.text.toString().trim()
                val currentList = viewModel.materiList.value ?: emptyList()
                val urutan = (currentList.maxOfOrNull { it.urutan } ?: 0) + 1

                if (nama.isBlank() || deskripsi.isBlank() || manfaat.isBlank()) {
                    Toast.makeText(requireContext(), "Judul, deskripsi, dan isi materi harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.createMateri(nama, deskripsi, manfaat, uploadedGambarUrl, uploadedVideoUrl, urutan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(materi: MateriApi) {
        uploadedGambarUrl = materi.gambar_url
        uploadedVideoUrl = materi.video_url

        val dialogView = layoutInflater.inflate(R.layout.dialog_materi, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaMateri)
        val etDeskripsi = dialogView.findViewById<EditText>(R.id.etDeskripsiMateri)
        val etManfaat = dialogView.findViewById<EditText>(R.id.etManfaatMateri)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFoto)
        val btnPilihGambar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihGambar)
        val progressGambar = dialogView.findViewById<ProgressBar>(R.id.progressGambar)
        val tvGambarStatus = dialogView.findViewById<TextView>(R.id.tvGambarStatus)

        currentFotoPreview = imgPreview
        currentFotoProgress = progressGambar
        currentFotoStatus = tvGambarStatus
        currentFotoButton = btnPilihGambar

        etNama.setText(materi.nama)
        etDeskripsi.setText(materi.deskripsi)
        etManfaat.setText(materi.manfaat)

        if (!materi.gambar_url.isNullOrBlank()) {
            imgPreview.visibility = View.VISIBLE
            Glide.with(this)
                .load(materi.gambar_url)
                .into(imgPreview)
            btnPilihGambar.text = "Ganti Gambar"
        }

        btnPilihGambar.setOnClickListener {
            pickGambarLauncher.launch("image/*")
        }

        setupVideoFields(dialogView)
        if (!materi.video_url.isNullOrBlank()) {
            currentVideoName?.visibility = View.VISIBLE
            currentVideoName?.text = "Video sudah ada"
            (currentVideoButton as? com.google.android.material.button.MaterialButton)?.text = "Ganti Video"
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Materi")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = etNama.text.toString().trim()
                val deskripsi = etDeskripsi.text.toString().trim()
                val manfaat = etManfaat.text.toString().trim()

                if (nama.isBlank() || deskripsi.isBlank() || manfaat.isBlank()) {
                    Toast.makeText(requireContext(), "Judul, deskripsi, dan isi materi harus diisi", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.updateMateri(materi.id, nama, deskripsi, manfaat, uploadedGambarUrl, uploadedVideoUrl, materi.urutan)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmation(materi: MateriApi) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Materi")
            .setMessage("Yakin ingin menghapus materi \"${materi.nama}\"?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteMateri(materi.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
