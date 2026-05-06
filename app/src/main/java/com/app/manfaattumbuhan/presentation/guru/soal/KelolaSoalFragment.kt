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
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.activity.result.ActivityResultLauncher
import android.app.Dialog
import android.view.Window
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
import com.app.manfaattumbuhan.data.remote.model.SoalApi
import com.app.manfaattumbuhan.databinding.FragmentKelolaSoalBinding
import com.app.manfaattumbuhan.presentation.adapter.SoalGuruAdapter
import com.bumptech.glide.Glide
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

    private lateinit var pickFotoLauncher: ActivityResultLauncher<String>
    private lateinit var pickVideoLauncher: ActivityResultLauncher<Intent>

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
        observeData()
        viewModel.loadSoal()
    }

    private fun setupRecyclerView() {
        adapter = SoalGuruAdapter(
            onEdit = { soal -> showEditDialog(soal) },
            onDelete = { soal -> showDeleteConfirmation(soal) }
        )
        binding.rvSoal.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSoal.adapter = adapter
    }

    private fun setupListeners() {
        loadGuruPhoto()

        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_soal_to_profil)
        }

        binding.btnBuatSoal.setOnClickListener {
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
        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvSoalCount.text = "${list.size} soal"
            binding.tvEmptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun setupSpinner(spinner: Spinner, selectedIndex: Int = 0) {
        val options = listOf("A", "B", "C", "D")
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, options)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter
        if (selectedIndex in options.indices) {
            spinner.setSelection(selectedIndex)
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
                currentVideoPreviewUrl = uploadResponse.url
                currentVideoStatus?.text = "Video berhasil diupload"
                currentVideoButton?.visibility = View.VISIBLE
                (currentVideoButton as? com.google.android.material.button.MaterialButton)?.text = "Ganti Video"
                currentBtnLihatVideo?.visibility = View.VISIBLE
            }
            result.onFailure { error ->
                uploadedVideoUrl = null
                currentVideoStatus?.text = "Gagal upload: ${error.message}"
                currentVideoStatus?.setTextColor(resources.getColor(R.color.red_button, null))
                currentVideoButton?.visibility = View.VISIBLE
            }
        }
    }

    private fun showVideoPopup(videoUrl: String) {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_video_player)

        val videoPlayer = dialog.findViewById<VideoView>(R.id.videoPlayer)
        val btnTutup = dialog.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnTutupVideo)

        videoPlayer.setVideoURI(Uri.parse(videoUrl))
        val mediaController = android.widget.MediaController(requireContext())
        mediaController.setAnchorView(videoPlayer)
        videoPlayer.setMediaController(mediaController)
        videoPlayer.start()

        btnTutup.setOnClickListener {
            videoPlayer.stopPlayback()
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            videoPlayer.stopPlayback()
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
        val etPilihanA = dialogView.findViewById<EditText>(R.id.etPilihanA)
        val etPilihanB = dialogView.findViewById<EditText>(R.id.etPilihanB)
        val etPilihanC = dialogView.findViewById<EditText>(R.id.etPilihanC)
        val etPilihanD = dialogView.findViewById<EditText>(R.id.etPilihanD)
        val spinnerJawaban = dialogView.findViewById<Spinner>(R.id.spinnerJawaban)

        currentFotoPreview = imgPreviewFoto
        currentFotoProgress = progressFoto
        currentFotoStatus = tvFotoStatus
        currentFotoButton = btnPilihFoto
        currentVideoProgress = progressVideo
        currentVideoStatus = tvVideoStatus
        currentVideoButton = btnPilihVideo
        currentBtnLihatVideo = btnLihatVideo
        currentVideoPreviewUrl = null

        btnLihatVideo.setOnClickListener {
            currentVideoPreviewUrl?.let { url -> showVideoPopup(url) }
        }

        setupSpinner(spinnerJawaban)

        btnPilihFoto.setOnClickListener {
            pickFotoLauncher.launch("image/*")
        }

        btnPilihVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            pickVideoLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Buat Soal Baru")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val judul = etJudul.text.toString().trim()
                val pilihanA = etPilihanA.text.toString().trim()
                val pilihanB = etPilihanB.text.toString().trim()
                val pilihanC = etPilihanC.text.toString().trim()
                val pilihanD = etPilihanD.text.toString().trim()
                val jawabanBenar = spinnerJawaban.selectedItemPosition

                val pilihan = listOf(pilihanA, pilihanB, pilihanC, pilihanD).filter { it.isNotBlank() }
                if (pilihan.size < 2) {
                    Toast.makeText(requireContext(), "Minimal 2 pilihan jawaban", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val deskripsi = buildDeskripsiJson(pilihan, jawabanBenar)
                viewModel.addSoal(judul, deskripsi, uploadedFotoUrl, uploadedVideoUrl)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(soal: SoalApi) {
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
        val etPilihanA = dialogView.findViewById<EditText>(R.id.etPilihanA)
        val etPilihanB = dialogView.findViewById<EditText>(R.id.etPilihanB)
        val etPilihanC = dialogView.findViewById<EditText>(R.id.etPilihanC)
        val etPilihanD = dialogView.findViewById<EditText>(R.id.etPilihanD)
        val spinnerJawaban = dialogView.findViewById<Spinner>(R.id.spinnerJawaban)

        currentFotoPreview = imgPreviewFoto
        currentFotoProgress = progressFoto
        currentFotoStatus = tvFotoStatus
        currentFotoButton = btnPilihFoto
        currentVideoProgress = progressVideo
        currentVideoStatus = tvVideoStatus
        currentVideoButton = btnPilihVideo
        currentBtnLihatVideo = btnLihatVideoEdit
        currentVideoPreviewUrl = soal.video_url

        btnLihatVideoEdit.setOnClickListener {
            currentVideoPreviewUrl?.let { url -> showVideoPopup(url) }
        }

        etJudul.setText(soal.judul)

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
        }

        val parsed = parseDeskripsiJson(soal.deskripsi)
        if (parsed != null) {
            val (pilihan, jawabanBenar) = parsed
            if (pilihan.size > 0) etPilihanA.setText(pilihan[0])
            if (pilihan.size > 1) etPilihanB.setText(pilihan[1])
            if (pilihan.size > 2) etPilihanC.setText(pilihan[2])
            if (pilihan.size > 3) etPilihanD.setText(pilihan[3])
            setupSpinner(spinnerJawaban, jawabanBenar)
        } else {
            setupSpinner(spinnerJawaban)
        }

        btnPilihFoto.setOnClickListener {
            pickFotoLauncher.launch("image/*")
        }

        btnPilihVideo.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            intent.type = "video/*"
            pickVideoLauncher.launch(intent)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Soal")
            .setView(dialogView)
            .setPositiveButton("Simpan") { _, _ ->
                val judul = etJudul.text.toString().trim()
                val pilihanA = etPilihanA.text.toString().trim()
                val pilihanB = etPilihanB.text.toString().trim()
                val pilihanC = etPilihanC.text.toString().trim()
                val pilihanD = etPilihanD.text.toString().trim()
                val jawabanBenar = spinnerJawaban.selectedItemPosition

                val pilihan = listOf(pilihanA, pilihanB, pilihanC, pilihanD).filter { it.isNotBlank() }
                if (pilihan.size < 2) {
                    Toast.makeText(requireContext(), "Minimal 2 pilihan jawaban", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val deskripsi = buildDeskripsiJson(pilihan, jawabanBenar)
                viewModel.updateSoal(soal.id, judul, deskripsi, uploadedFotoUrl, uploadedVideoUrl)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteConfirmation(soal: SoalApi) {
        AlertDialog.Builder(requireContext())
            .setTitle("Hapus Soal")
            .setMessage("Yakin ingin menghapus soal \"${soal.judul}\"?")
            .setPositiveButton("Hapus") { _, _ -> viewModel.deleteSoal(soal.id) }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
