package com.app.manfaattumbuhan.presentation.guru.soal

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import android.app.Dialog
import android.view.Window
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
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
import com.app.manfaattumbuhan.data.remote.model.SoalApi
import com.app.manfaattumbuhan.databinding.FragmentKelolaSoalBinding
import com.app.manfaattumbuhan.presentation.adapter.SoalGuruAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

class KelolaSoalFragment : Fragment() {

    private var _binding: FragmentKelolaSoalBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KelolaSoalViewModel
    private lateinit var adapter: SoalGuruAdapter

    private var uploadedFotoUrl: String? = null
    private var uploadedVideoUrl: String? = null

    private var currentFotoPreview: ImageView? = null
    private var currentFotoProgress: ProgressBar? = null
    private var currentFotoStatus: TextView? = null
    private var currentFotoButton: View? = null
    private var currentVideoProgress: ProgressBar? = null
    private var currentVideoStatus: TextView? = null
    private var currentVideoButton: View? = null
    private var currentVideoPreviewUrl: String? = null
    private var currentBtnLihatVideo: com.google.android.material.button.MaterialButton? = null
    private var currentVideoPreviewCard: com.google.android.material.card.MaterialCardView? = null
    private var currentVideoPreview: androidx.media3.ui.PlayerView? = null
    private var dialogExoPlayer: androidx.media3.exoplayer.ExoPlayer? = null

    private lateinit var pickFotoLauncher: ActivityResultLauncher<String>
    private lateinit var pickVideoLauncher: ActivityResultLauncher<Intent>

    private val tingkatOptions = listOf("Pre-test", "Mudah", "Sedang", "Sulit")
    private val tingkatValues = listOf("pretest", "mudah", "sedang", "sulit")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pickFotoLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            uri?.let { handleFotoSelected(it) }
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
        _binding = FragmentKelolaSoalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        viewModel = ViewModelProvider(this, KelolaSoalViewModelFactory())[KelolaSoalViewModel::class.java]

        setupRecyclerView()
        setupListeners()
        setupFilterChips()
        setupPagination()
        observeData()
        viewModel.loadSoal(null)
    }

    private fun setupRecyclerView() {
        adapter = SoalGuruAdapter(
            onEdit = { soal, displayNumber -> showEditDialog(soal, displayNumber) },
            onDelete = { soal, displayNumber -> showDeleteConfirmation(soal, displayNumber) }
        )
        binding.rvSoal.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSoal.adapter = adapter
    }

    private fun setupListeners() {
        loadGuruPhoto()

        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_soal_to_profil)
        }

        binding.btnBuatSoal.setOnClickListener {
            showCreateDialog()
        }
    }

    private fun setupFilterChips() {
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener

            // Reset pagination state in UI immediately to avoid stale offset while new data is loading.
            adapter.pageOffset = 0
            binding.rvSoal.scrollToPosition(0)

            val tingkat = when (checkedIds[0]) {
                R.id.chipPretest -> "pretest"
                R.id.chipMudah -> "mudah"
                R.id.chipSedang -> "sedang"
                R.id.chipSulit -> "sulit"
                else -> null // "Semua"
            }
            viewModel.loadSoal(tingkat)
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
        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            val currentPage = viewModel.currentPage.value ?: 1
            adapter.pageOffset = (currentPage - 1) * KelolaSoalViewModel.ITEMS_PER_PAGE

            // Submit a new list instance to force RecyclerView rebind reliably.
            adapter.submitList(list.toList())
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.totalItems.observe(viewLifecycleOwner) { total ->
            binding.tvSoalCount.text = "$total soal"
        }

        viewModel.totalPages.observe(viewLifecycleOwner) { totalPages ->
            binding.layoutPagination.visibility = if (totalPages > 1) View.VISIBLE else View.GONE
            updatePageNumbers(viewModel.currentPage.value ?: 1, totalPages)
        }

        viewModel.currentPage.observe(viewLifecycleOwner) { currentPage ->
            val totalPages = viewModel.totalPages.value ?: 1
            if (totalPages > 1) {
                updatePageNumbers(currentPage, totalPages)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        viewModel.message.observe(viewLifecycleOwner) { msg ->
            if (msg.isNotBlank()) {
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupJawabanSpinner(dropdown: MaterialAutoCompleteTextView, selectedIndex: Int = 0) {
        val options = listOf("A", "B", "C", "D")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, options)
        dropdown.setAdapter(spinnerAdapter)
        if (selectedIndex in options.indices) {
            // setText(text, filter) with false prevents filtering and keeps the dropdown closed
            dropdown.setText(options[selectedIndex], false)
        }
    }

    private fun setupTingkatSpinner(dropdown: MaterialAutoCompleteTextView, selectedTingkat: String = "pretest") {
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, tingkatOptions)
        dropdown.setAdapter(spinnerAdapter)
        val index = tingkatValues.indexOf(selectedTingkat)
        if (index >= 0 && index < tingkatOptions.size) {
            dropdown.setText(tingkatOptions[index], false)
        }
    }

    private fun buildDeskripsiJson(pilihan: List<String>, jawabanBenar: Int): String {
        val json = JSONObject()
        val pilihanArray = JSONArray()
        pilihan.forEach { pilihanArray.put(it) }
        json.put("pilihan", pilihanArray)
        json.put("jawabanBenar", jawabanBenar)
        return json.toString()
    }

    private fun parseDeskripsiJson(deskripsi: String): Pair<List<String>, Int>? {
        return try {
            val json = JSONObject(deskripsi)
            val pilihanArray = json.getJSONArray("pilihan")
            val pilihan = mutableListOf<String>()
            for (i in 0 until pilihanArray.length()) {
                pilihan.add(pilihanArray.getString(i))
            }
            val jawabanBenar = json.getInt("jawabanBenar")
            Pair(pilihan, jawabanBenar)
        } catch (e: Exception) {
            null
        }
    }

    private fun handleFotoSelected(uri: Uri) {
        currentFotoPreview?.let { preview ->
            preview.visibility = View.VISIBLE
            Glide.with(this).load(uri).into(preview)
        }
        currentFotoButton?.visibility = View.GONE
        currentFotoProgress?.visibility = View.VISIBLE
        currentFotoStatus?.visibility = View.VISIBLE
        currentFotoStatus?.text = "Mengupload foto..."

        viewLifecycleOwner.lifecycleScope.launch {
            val result = FileUploadHelper.uploadFile(requireContext(), uri, "foto")
            currentFotoProgress?.visibility = View.GONE
            result.onSuccess { uploadResponse ->
                uploadedFotoUrl = uploadResponse.url
                currentFotoStatus?.text = "Foto berhasil diupload"
                currentFotoButton?.visibility = View.VISIBLE
                (currentFotoButton as? com.google.android.material.button.MaterialButton)?.text = "Ganti Foto"
            }
            result.onFailure { error ->
                uploadedFotoUrl = null
                currentFotoStatus?.text = "Gagal upload: ${error.message}"
                currentFotoStatus?.setTextColor(resources.getColor(R.color.red_button, null))
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
            uri != null -> androidx.media3.common.MediaItem.fromUri(uri)
            url != null -> androidx.media3.common.MediaItem.fromUri(android.net.Uri.parse(url))
            else        -> return
        }
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = false
        currentVideoPreviewCard?.visibility = View.VISIBLE
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
                currentVideoPreviewUrl = uploadResponse.url
                currentVideoStatus?.text = "✅ Video berhasil diupload"
                currentVideoButton?.visibility = View.VISIBLE
                (currentVideoButton as? com.google.android.material.button.MaterialButton)?.text = "Ganti Video"
                currentBtnLihatVideo?.visibility = View.VISIBLE
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
    private fun showVideoPopup(videoUrl: String) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_video_player)

        val playerView = dialog.findViewById<PlayerView>(R.id.videoPlayer)
        val videoLoading = dialog.findViewById<ProgressBar>(R.id.videoLoading)
        val btnTutup = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnTutupVideo)

        val exoPlayer = ExoPlayer.Builder(requireContext()).build()
        playerView.player = exoPlayer

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        exoPlayer.setMediaItem(mediaItem)

        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                when (playbackState) {
                    Player.STATE_BUFFERING -> videoLoading.visibility = View.VISIBLE
                    Player.STATE_READY -> videoLoading.visibility = View.GONE
                    Player.STATE_ENDED -> videoLoading.visibility = View.GONE
                    Player.STATE_IDLE -> videoLoading.visibility = View.GONE
                }
            }
        })

        exoPlayer.prepare()
        exoPlayer.playWhenReady = true

        btnTutup.setOnClickListener {
            exoPlayer.release()
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            exoPlayer.release()
        }

        dialog.show()
    }

    private fun showCreateDialog() {
        uploadedFotoUrl = null
        uploadedVideoUrl = null

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_soal, null)
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val btnPilihFoto = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihFoto)
        val btnPilihVideo = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihVideo)
        val imgPreviewFoto = dialogView.findViewById<ImageView>(R.id.imgPreviewFoto)
        val progressFoto = dialogView.findViewById<ProgressBar>(R.id.progressFoto)
        val tvFotoStatus = dialogView.findViewById<TextView>(R.id.tvFotoStatus)
        val progressVideo = dialogView.findViewById<ProgressBar>(R.id.progressVideo)
        val tvVideoStatus = dialogView.findViewById<TextView>(R.id.tvVideoStatus)
        val btnLihatVideo = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLihatVideo)
        val spinnerTingkat = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerTingkat)
        val etPilihanA = dialogView.findViewById<EditText>(R.id.etPilihanA)
        val etPilihanB = dialogView.findViewById<EditText>(R.id.etPilihanB)
        val etPilihanC = dialogView.findViewById<EditText>(R.id.etPilihanC)
        val etPilihanD = dialogView.findViewById<EditText>(R.id.etPilihanD)
        val spinnerJawaban = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerJawaban)

        currentFotoPreview = imgPreviewFoto
        currentFotoProgress = progressFoto
        currentFotoStatus = tvFotoStatus
        currentFotoButton = btnPilihFoto
        currentVideoProgress = progressVideo
        currentVideoStatus = tvVideoStatus
        currentVideoButton = btnPilihVideo
        currentBtnLihatVideo = btnLihatVideo
        currentVideoPreviewUrl = null
        currentVideoPreviewCard = dialogView.findViewById(R.id.cardVideoPreview)
        currentVideoPreview     = dialogView.findViewById(R.id.videoPreview)

        btnLihatVideo.setOnClickListener {
            currentVideoPreviewUrl?.let { url -> showVideoPopup(url) }
        }

        val checkedIds = binding.chipGroupFilter.checkedChipIds
        val activeTingkat = if (checkedIds.isNotEmpty()) {
            when (checkedIds[0]) {
                R.id.chipMudah -> "mudah"
                R.id.chipSedang -> "sedang"
                R.id.chipSulit -> "sulit"
                else -> "pretest"
            }
        } else {
            "pretest"
        }
        setupTingkatSpinner(spinnerTingkat, activeTingkat)
        setupJawabanSpinner(spinnerJawaban)

        btnPilihFoto.setOnClickListener {
            pickFotoLauncher.launch("image/*")
        }

        btnPilihVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            pickVideoLauncher.launch(intent)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Buat Soal Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val tvError = dialogView.findViewById<TextView>(R.id.tvErrorStatus)
                tvError.visibility = View.GONE

                val judul = etJudul.text.toString().trim()
                val pilihanA = etPilihanA.text.toString().trim()
                val pilihanB = etPilihanB.text.toString().trim()
                val pilihanC = etPilihanC.text.toString().trim()
                val pilihanD = etPilihanD.text.toString().trim()

                val jawabanText = spinnerJawaban.text.toString()
                val jawabanBenar = listOf("A", "B", "C", "D").indexOf(jawabanText).takeIf { it >= 0 } ?: 0

                val tingkatText = spinnerTingkat.text.toString()
                val tingkatIndex = tingkatOptions.indexOf(tingkatText).takeIf { it >= 0 } ?: 0
                val tingkat = tingkatValues[tingkatIndex]

                if (judul.isBlank()) {
                    tvError.text = "⚠️ Judul/Pertanyaan soal harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (pilihanA.isBlank() || pilihanB.isBlank() || pilihanC.isBlank() || pilihanD.isBlank()) {
                    tvError.text = "⚠️ Semua pilihan jawaban (A, B, C, D) harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (uploadedFotoUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Foto soal harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (uploadedVideoUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Video soal harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                dialog.dismiss()
                val pilihan = listOf(pilihanA, pilihanB, pilihanC, pilihanD)
                val deskripsi = buildDeskripsiJson(pilihan, jawabanBenar)
                viewModel.addSoal(judul, deskripsi, uploadedFotoUrl, uploadedVideoUrl, tingkat)
            }
        }
        dialog.setOnDismissListener {
            releaseDialogPlayer()
        }
        dialog.show()
    }

    private fun showEditDialog(soal: SoalApi, displayNumber: Int = 0) {
        uploadedFotoUrl = soal.foto_url
        uploadedVideoUrl = soal.video_url

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_soal, null)
        val etJudul = dialogView.findViewById<EditText>(R.id.etJudul)
        val btnPilihFoto = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihFoto)
        val btnPilihVideo = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnPilihVideo)
        val imgPreviewFoto = dialogView.findViewById<ImageView>(R.id.imgPreviewFoto)
        val progressFoto = dialogView.findViewById<ProgressBar>(R.id.progressFoto)
        val tvFotoStatus = dialogView.findViewById<TextView>(R.id.tvFotoStatus)
        val progressVideo = dialogView.findViewById<ProgressBar>(R.id.progressVideo)
        val tvVideoStatus = dialogView.findViewById<TextView>(R.id.tvVideoStatus)
        val btnLihatVideoEdit = dialogView.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnLihatVideo)
        val spinnerTingkat = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerTingkat)
        val etPilihanA = dialogView.findViewById<EditText>(R.id.etPilihanA)
        val etPilihanB = dialogView.findViewById<EditText>(R.id.etPilihanB)
        val etPilihanC = dialogView.findViewById<EditText>(R.id.etPilihanC)
        val etPilihanD = dialogView.findViewById<EditText>(R.id.etPilihanD)
        val spinnerJawaban = dialogView.findViewById<MaterialAutoCompleteTextView>(R.id.spinnerJawaban)

        currentFotoPreview = imgPreviewFoto
        currentFotoProgress = progressFoto
        currentFotoStatus = tvFotoStatus
        currentFotoButton = btnPilihFoto
        currentVideoProgress = progressVideo
        currentVideoStatus = tvVideoStatus
        currentVideoButton = btnPilihVideo
        currentBtnLihatVideo = btnLihatVideoEdit
        currentVideoPreviewUrl = soal.video_url
        currentVideoPreviewCard = dialogView.findViewById(R.id.cardVideoPreview)
        currentVideoPreview     = dialogView.findViewById(R.id.videoPreview)

        btnLihatVideoEdit.setOnClickListener {
            currentVideoPreviewUrl?.let { url -> showVideoPopup(url) }
        }

        etJudul.setText(soal.judul)
        setupTingkatSpinner(spinnerTingkat, soal.tingkat ?: "pretest")

        if (!soal.foto_url.isNullOrBlank()) {
            imgPreviewFoto.visibility = View.VISIBLE
            Glide.with(this).load(soal.foto_url).into(imgPreviewFoto)
            btnPilihFoto.text = "Ganti Foto"
            tvFotoStatus.visibility = View.VISIBLE
            tvFotoStatus.text = "Foto sudah ada"
        }

        if (!soal.video_url.isNullOrBlank()) {
            btnLihatVideoEdit.visibility = View.VISIBLE
            btnPilihVideo.text = "Ganti Video"
            tvVideoStatus.visibility = View.VISIBLE
            tvVideoStatus.text = "Video sudah ada"

            // Tampilkan preview video inline
            playVideoInDialog(url = soal.video_url)
        }

        val parsed = parseDeskripsiJson(soal.deskripsi)
        if (parsed != null) {
            val (pilihan, jawabanBenar) = parsed
            if (pilihan.size > 0) etPilihanA.setText(pilihan[0])
            if (pilihan.size > 1) etPilihanB.setText(pilihan[1])
            if (pilihan.size > 2) etPilihanC.setText(pilihan[2])
            if (pilihan.size > 3) etPilihanD.setText(pilihan[3])
            setupJawabanSpinner(spinnerJawaban, jawabanBenar)
        } else {
            setupJawabanSpinner(spinnerJawaban)
        }

        btnPilihFoto.setOnClickListener {
            pickFotoLauncher.launch("image/*")
        }

        btnPilihVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            pickVideoLauncher.launch(intent)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Soal")
            .setView(dialogView)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val tvError = dialogView.findViewById<TextView>(R.id.tvErrorStatus)
                tvError.visibility = View.GONE

                val judul = etJudul.text.toString().trim()
                val pilihanA = etPilihanA.text.toString().trim()
                val pilihanB = etPilihanB.text.toString().trim()
                val pilihanC = etPilihanC.text.toString().trim()
                val pilihanD = etPilihanD.text.toString().trim()

                val jawabanText = spinnerJawaban.text.toString()
                val jawabanBenar = listOf("A", "B", "C", "D").indexOf(jawabanText).takeIf { it >= 0 } ?: 0

                val tingkatText = spinnerTingkat.text.toString()
                val tingkatIndex = tingkatOptions.indexOf(tingkatText).takeIf { it >= 0 } ?: 0
                val tingkat = tingkatValues[tingkatIndex]

                if (judul.isBlank()) {
                    tvError.text = "⚠️ Judul/Pertanyaan soal harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (pilihanA.isBlank() || pilihanB.isBlank() || pilihanC.isBlank() || pilihanD.isBlank()) {
                    tvError.text = "⚠️ Semua pilihan jawaban (A, B, C, D) harus diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (uploadedFotoUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Foto soal harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (uploadedVideoUrl.isNullOrBlank()) {
                    tvError.text = "⚠️ Video soal harus diupload"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                val pilihan = listOf(pilihanA, pilihanB, pilihanC, pilihanD)
                val deskripsi = buildDeskripsiJson(pilihan, jawabanBenar)

                val isJudulChanged = judul != soal.judul
                val isDeskripsiChanged = deskripsi != soal.deskripsi
                val isTingkatChanged = tingkat != soal.tingkat
                val isFotoChanged = uploadedFotoUrl != soal.foto_url
                val isVideoChanged = uploadedVideoUrl != soal.video_url

                if (!isJudulChanged && !isDeskripsiChanged && !isTingkatChanged && !isFotoChanged && !isVideoChanged) {
                    tvError.text = "⚠️ Tidak ada perubahan yang disimpan"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                dialog.dismiss()
                viewModel.updateSoal(soal.id, judul, deskripsi, uploadedFotoUrl, uploadedVideoUrl, tingkat, displayNumber)
            }
        }
        dialog.setOnDismissListener {
            releaseDialogPlayer()
        }
        dialog.show()
    }

    private fun showDeleteConfirmation(soal: SoalApi, displayNumber: Int = 0) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Soal")
            .setMessage("Yakin ingin menghapus soal No. $displayNumber?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteSoal(soal.id, displayNumber) }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseDialogPlayer()
        _binding = null
    }
}
