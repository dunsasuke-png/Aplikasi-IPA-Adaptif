package com.app.manfaattumbuhan.presentation.guru.materi

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.core.content.ContextCompat
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
    private var currentVideoPreviewCard: com.google.android.material.card.MaterialCardView? = null
    private var currentVideoPreview: androidx.media3.ui.PlayerView? = null
    private var currentVideoPreviewLoading: ProgressBar? = null
    private var dialogExoPlayer: androidx.media3.exoplayer.ExoPlayer? = null

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
        setupFilterChips()
        setupPagination()
        observeData()

        if (binding.chipGroupFilter.checkedChipId == View.NO_ID) {
            binding.chipGroupFilter.check(R.id.chipMudah)
        }
        viewModel.loadMateri(getActiveChipLevel())
    }

    private fun setupRecyclerView() {
        adapter = MateriGuruAdapter(
            onEdit = { materi, displayNumber -> showEditDialog(materi, displayNumber) },
            onDelete = { materi, displayNumber -> showDeleteConfirmation(materi, displayNumber) }
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

        binding.etSearchMateri.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setSearchQuery(s.toString())
            }
        })
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            adapter.pageOffset = 0
            binding.rvMateri.scrollToPosition(0)
            val level = when (checkedIds[0]) {
                R.id.chipSedang -> "sedang"
                R.id.chipSulit  -> "sulit"
                else            -> "mudah"  // chipMudah default
            }
            viewModel.loadMateri(level, preservePage = 1)
            Log.d("KelolaMateri", "[setupFilterChips] Chip filter changed to: $level")
        }
    }

    /** Kembalikan tingkat yang sedang aktif di chip filter (untuk pre-isi spinner dialog) */
    private fun getActiveChipLevel(): String {
        val checkedId = binding.chipGroupFilter.checkedChipId
        return when (checkedId) {
            R.id.chipSedang -> "sedang"
            R.id.chipSulit  -> "sulit"
            else            -> "mudah"
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

    private fun setupPagination() {
        binding.btnPrevPage.setOnClickListener {
            val current = viewModel.currentPage.value ?: 1
            if (current > 1) viewModel.goToPage(current - 1)
        }

        binding.btnNextPage.setOnClickListener {
            val current = viewModel.currentPage.value ?: 1
            val total = viewModel.totalPages.value ?: 1
            if (current < total) viewModel.goToPage(current + 1)
        }
    }

    private fun updatePageNumbers(currentPage: Int, totalPages: Int) {
        binding.layoutPageNumbers.removeAllViews()
        for (i in 1..totalPages) {
            val btn = com.google.android.material.button.MaterialButton(
                requireContext(),
                null,
                com.google.android.material.R.attr.materialButtonOutlinedStyle
            ).apply {
                text = i.toString()
                textSize = 12f
                minimumWidth = 0
                minWidth = 0
                setPadding(0, 0, 0, 0)
                insetTop = 0
                insetBottom = 0
                val size = (36 * resources.displayMetrics.density).toInt()
                layoutParams = android.widget.LinearLayout.LayoutParams(size, size).apply {
                    marginStart = (4 * resources.displayMetrics.density).toInt()
                    marginEnd = (4 * resources.displayMetrics.density).toInt()
                }
                cornerRadius = (18 * resources.displayMetrics.density).toInt()
                if (i == currentPage) {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.green_primary))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    strokeWidth = 0
                } else {
                    setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.white))
                    setTextColor(ContextCompat.getColor(requireContext(), R.color.green_dark))
                    strokeColor = android.content.res.ColorStateList.valueOf(
                        ContextCompat.getColor(requireContext(), R.color.gray_border)
                    )
                    strokeWidth = (1 * resources.displayMetrics.density).toInt()
                }
                setOnClickListener { viewModel.goToPage(i) }
            }
            binding.layoutPageNumbers.addView(btn)
        }

        binding.btnPrevPage.isEnabled = currentPage > 1
        binding.btnNextPage.isEnabled = currentPage < totalPages
        binding.btnPrevPage.alpha = if (currentPage > 1) 1f else 0.4f
        binding.btnNextPage.alpha = if (currentPage < totalPages) 1f else 0.4f
    }

    private fun observeData() {
        viewModel.materiList.observe(viewLifecycleOwner) { list ->
            // pageOffset = global filter offset + per-page offset
            val currentPage   = viewModel.currentPage.value ?: 1
            val filterOffset  = viewModel.filterOffset.value ?: 0
            adapter.pageOffset = filterOffset + (currentPage - 1) * KelolaMateriViewModel.ITEMS_PER_PAGE
            adapter.submitList(list.toList())
            if (list.isEmpty()) {
                val query = binding.etSearchMateri.text.toString().trim()
                if (query.isNotBlank()) {
                    binding.tvEmptyState.text = "Materi dengan judul \"$query\" tidak ditemukan."
                } else {
                    binding.tvEmptyState.text = "Belum ada materi."
                }
                binding.tvEmptyState.visibility = View.VISIBLE
            } else {
                binding.tvEmptyState.visibility = View.GONE
            }
        }

        viewModel.totalItems.observe(viewLifecycleOwner) { total ->
            binding.tvMateriCount.text = "$total materi"
        }

        viewModel.totalPages.observe(viewLifecycleOwner) { totalPages ->
            binding.layoutPagination.visibility = if (totalPages > 1) View.VISIBLE else View.GONE
            updatePageNumbers(viewModel.currentPage.value ?: 1, totalPages)
        }

        viewModel.currentPage.observe(viewLifecycleOwner) { currentPage ->
            // pageOffset already handled by materiList observer — just update UI buttons
            val totalPages = viewModel.totalPages.value ?: 1
            if (totalPages >= 1) {
                updatePageNumbers(currentPage, totalPages)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.rvMateri.visibility = if (loading) View.INVISIBLE else View.VISIBLE
            if (loading) binding.tvEmptyState.visibility = View.GONE
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

    private fun releaseDialogPlayer() {
        dialogExoPlayer?.release()
        dialogExoPlayer = null
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun playVideoInDialog(uri: android.net.Uri? = null, url: String? = null) {
        releaseDialogPlayer()
        val playerView = currentVideoPreview ?: return
        val player = androidx.media3.exoplayer.ExoPlayer.Builder(requireContext()).build()
        dialogExoPlayer = player
        playerView.player = player
        val mediaItem = when {
            uri  != null -> androidx.media3.common.MediaItem.fromUri(uri)
            url  != null -> androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(url))
            else         -> return
        }
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = false   // jangan autoplay — tampilkan frame pertama
        currentVideoPreviewCard?.visibility = android.view.View.VISIBLE
    }

    private fun handleVideoSelected(uri: Uri) {
        playVideoInDialog(uri = uri)

        currentVideoButton?.visibility = View.GONE
        currentVideoProgress?.visibility = View.VISIBLE
        currentVideoStatus?.visibility = View.VISIBLE
        currentVideoStatus?.text = "Mengupload video..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = FileUploadHelper.uploadFile(requireContext(), uri, "video")
            currentVideoProgress?.visibility = View.GONE
            result.onSuccess { uploadResponse ->
                uploadedVideoUrl = uploadResponse.url
                currentVideoStatus?.text = "✅ Video berhasil diupload"
                currentVideoButton?.visibility = View.VISIBLE
                (currentVideoButton as? com.google.android.material.button.MaterialButton)?.text = "Ganti Video"
                currentVideoName?.visibility = View.VISIBLE
                currentVideoName?.text = uploadResponse.original_name
            }
            result.onFailure { error ->
                uploadedVideoUrl = null
                currentVideoStatus?.text = "Gagal upload: ${error.message}"
                currentVideoStatus?.setTextColor(resources.getColor(R.color.red_button, null))
                currentVideoButton?.visibility = View.VISIBLE
                currentVideoPreviewCard?.visibility = View.GONE
                releaseDialogPlayer()
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupVideoFields(dialogView: View) {
        val btnPilihVideo = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihVideo)
        val progressVideo = dialogView.findViewById<ProgressBar>(R.id.progressVideo)
        val tvVideoStatus = dialogView.findViewById<TextView>(R.id.tvVideoStatus)
        val tvVideoName   = dialogView.findViewById<TextView>(R.id.tvVideoName)
        val cardVideoPreview = dialogView.findViewById<com.google.android.material.card.MaterialCardView>(R.id.cardVideoPreview)
        val playerView    = dialogView.findViewById<androidx.media3.ui.PlayerView>(R.id.videoPreview)

        currentVideoProgress    = progressVideo
        currentVideoStatus      = tvVideoStatus
        currentVideoButton      = btnPilihVideo
        currentVideoName        = tvVideoName
        currentVideoPreviewCard = cardVideoPreview
        currentVideoPreview     = playerView

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
        val etNamaGambar = dialogView.findViewById<EditText>(R.id.etNamaGambar)
        val etManfaat = dialogView.findViewById<EditText>(R.id.etManfaatMateri)
        val spinnerTingkat = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.spinnerTingkat)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFoto)
        val btnPilihGambar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihGambar)
        val progressGambar = dialogView.findViewById<ProgressBar>(R.id.progressGambar)
        val tvGambarStatus = dialogView.findViewById<TextView>(R.id.tvGambarStatus)

        currentFotoPreview = imgPreview
        currentFotoProgress = progressGambar
        currentFotoStatus = tvGambarStatus
        currentFotoButton = btnPilihGambar

        // Setup spinner tingkat — gunakan setSimpleItems() bukan ArrayAdapter
        // PENTING: ArrayAdapter mengaktifkan filter berdasarkan teks saat ini (setText),
        // sehingga dropdown hanya menampilkan item yang cocok dan user tidak bisa pilih lain.
        // setSimpleItems() menonaktifkan filter — semua item selalu tampil.
        val tingkatItems = arrayOf("Mudah", "Sedang", "Sulit")
        spinnerTingkat.setSimpleItems(tingkatItems)
        val defaultTingkat = getActiveChipLevel().replaceFirstChar { it.uppercase() }
        spinnerTingkat.setText(defaultTingkat, false)
        Log.d("KelolaMateri", "[Tambah] Chip aktif = ${getActiveChipLevel()}, spinner init = $defaultTingkat")

        btnPilihGambar.setOnClickListener {
            pickGambarLauncher.launch("image/*")
        }

        setupVideoFields(dialogView)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Tambah Materi")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val tvError = dialogView.findViewById<TextView>(R.id.tvErrorStatus)
                tvError.visibility = View.GONE

                val nama = etNama.text.toString().trim()
                val namaGambar = etNamaGambar.text.toString().trim()
                val manfaat = etManfaat.text.toString().trim()
                val tingkat = spinnerTingkat.text.toString().lowercase()
                Log.d("KelolaMateri", "[Tambah] Simpan diklik: spinnerText='${spinnerTingkat.text}', tingkat='$tingkat'")

                if (nama.isBlank()) {
                    tvError.text = "⚠️ Judul materi harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (namaGambar.isBlank()) {
                    tvError.text = "⚠️ Nama gambar harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (manfaat.isBlank()) {
                    tvError.text = "⚠️ Isi materi harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (tingkat !in listOf("mudah", "sedang", "sulit")) {
                    tvError.text = "⚠️ Pilih tingkat kesulitan terlebih dahulu"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (uploadedGambarUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Gambar materi harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (uploadedVideoUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Video materi harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                dialog.dismiss()
                viewModel.createMateri(nama, namaGambar, manfaat, tingkat, uploadedGambarUrl, uploadedVideoUrl)
            }
        }
        dialog.setOnDismissListener {
            releaseDialogPlayer()
        }
        dialog.show()
    }

    private fun showEditDialog(materi: MateriApi, displayNumber: Int = 0) {
        uploadedGambarUrl = materi.gambar_url
        uploadedVideoUrl = materi.video_url

        val dialogView = layoutInflater.inflate(R.layout.dialog_materi, null)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaMateri)
        val etNamaGambar = dialogView.findViewById<EditText>(R.id.etNamaGambar)
        val etManfaat = dialogView.findViewById<EditText>(R.id.etManfaatMateri)
        val spinnerTingkat = dialogView.findViewById<com.google.android.material.textfield.MaterialAutoCompleteTextView>(R.id.spinnerTingkat)
        val imgPreview = dialogView.findViewById<ImageView>(R.id.imgPreviewFoto)
        val btnPilihGambar = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihGambar)
        val progressGambar = dialogView.findViewById<ProgressBar>(R.id.progressGambar)
        val tvGambarStatus = dialogView.findViewById<TextView>(R.id.tvGambarStatus)

        currentFotoPreview = imgPreview
        currentFotoProgress = progressGambar
        currentFotoStatus = tvGambarStatus
        currentFotoButton = btnPilihGambar

        etNama.setText(materi.nama)
        etNamaGambar.setText(materi.deskripsi.let { if (it == "-") "" else it })
        etManfaat.setText(materi.manfaat)

        // Setup spinner tingkat — gunakan setSimpleItems() bukan ArrayAdapter
        val tingkatItems = arrayOf("Mudah", "Sedang", "Sulit")
        spinnerTingkat.setSimpleItems(tingkatItems)
        val currentTingkat = materi.tingkat.replaceFirstChar { it.uppercase() }
        spinnerTingkat.setText(currentTingkat, false)
        Log.d("KelolaMateri", "[Edit] materi.tingkat='${materi.tingkat}', spinner init='$currentTingkat'")

        if (!materi.gambar_url.isNullOrBlank()) {
            imgPreview.visibility = View.VISIBLE
            Glide.with(this).load(materi.gambar_url).into(imgPreview)
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
            playVideoInDialog(url = materi.video_url)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Materi")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val tvError = dialogView.findViewById<TextView>(R.id.tvErrorStatus)
                tvError.visibility = View.GONE

                val nama = etNama.text.toString().trim()
                val namaGambar = etNamaGambar.text.toString().trim()
                val manfaat = etManfaat.text.toString().trim()
                val tingkat = spinnerTingkat.text.toString().lowercase()
                Log.d("KelolaMateri", "[Edit] Simpan diklik: spinnerText='${spinnerTingkat.text}', tingkat='$tingkat'")

                if (nama.isBlank()) {
                    tvError.text = "⚠️ Judul materi harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (namaGambar.isBlank()) {
                    tvError.text = "⚠️ Nama gambar harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (manfaat.isBlank()) {
                    tvError.text = "⚠️ Isi materi harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (tingkat !in listOf("mudah", "sedang", "sulit")) {
                    tvError.text = "⚠️ Pilih tingkat kesulitan terlebih dahulu"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (uploadedGambarUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Gambar materi harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }
                if (uploadedVideoUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Video materi harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                dialog.dismiss()
                viewModel.updateMateri(materi.id, nama, namaGambar, manfaat, tingkat, uploadedGambarUrl, uploadedVideoUrl, displayNumber)
            }
        }
        dialog.setOnDismissListener {
            releaseDialogPlayer()
        }
        dialog.show()
    }

    private fun showDeleteConfirmation(materi: MateriApi, displayNumber: Int = 0) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Materi")
            .setMessage("Yakin ingin menghapus materi No. $displayNumber?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteMateri(materi.id, displayNumber)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseDialogPlayer()
        _binding = null
    }
}
