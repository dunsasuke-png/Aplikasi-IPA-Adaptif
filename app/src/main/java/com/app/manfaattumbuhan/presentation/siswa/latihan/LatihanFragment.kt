package com.app.manfaattumbuhan.presentation.siswa.latihan

import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.RadioButton
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.domain.fuzzy.FuzzyMamdani
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.CreateNilaiRequest
import com.app.manfaattumbuhan.databinding.FragmentLatihanBinding
import com.app.manfaattumbuhan.domain.model.NilaiSiswa
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LatihanFragment : Fragment() {

    private var _binding: FragmentLatihanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LatihanViewModel by viewModels { LatihanViewModelFactory() }
    private var tingkat: String = "Pre-test"
    private val apiService = ApiConfig.createService<ApiService>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLatihanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        tingkat = arguments?.getString("tingkat") ?: "Pre-test"
        viewModel.loadSoalByTingkat(tingkat)

        // Set tingkat label
        binding.tvTingkatLabel.text = tingkat
        binding.tvMotivasi.text = when (tingkat) {
            "Pre-test" -> "Sedikit lagi!"
            "Mudah" -> "Level Mudah"
            "Sedang" -> "Level Sedang"
            "Sulit" -> "Level Sulit"
            else -> "Ayo semangat!"
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.btnSelanjutnya.isEnabled = !loading
            binding.btnKembali.isEnabled = !loading
            if (loading) {
                binding.tvPertanyaan.text = "Memuat soal..."
                binding.radioGroup.removeAllViews()
                binding.imgSoal.visibility = View.GONE
            }
        }

        viewModel.loadError.observe(viewLifecycleOwner) { error ->
            error?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                binding.tvPertanyaan.text = it
            }
        }

        viewModel.currentSoal.observe(viewLifecycleOwner) { soal ->
            if (soal.pertanyaan.isNotBlank()) {
                binding.tvPertanyaan.visibility = View.VISIBLE
                binding.tvPertanyaan.text = soal.pertanyaan
            } else {
                binding.tvPertanyaan.visibility = View.GONE
            }

            if (soal.imageUrl != null && soal.imageUrl.isNotBlank()) {
                binding.imgSoal.visibility = View.VISIBLE
                Glide.with(this)
                    .load(soal.imageUrl)
                    .placeholder(R.drawable.bg_rounded_gray)
                    .error(R.drawable.bg_rounded_gray)
                    .into(binding.imgSoal)
            } else if (soal.imageRes != null) {
                binding.imgSoal.visibility = View.VISIBLE
                binding.imgSoal.setImageResource(soal.imageRes)
            } else {
                binding.imgSoal.visibility = View.GONE
            }

            if (soal.videoUrl != null && soal.videoUrl.isNotBlank()) {
                binding.btnPlayVideo.visibility = View.VISIBLE
                binding.btnPlayVideo.setOnClickListener {
                    showVideoPopup(soal.videoUrl)
                }
            } else {
                binding.btnPlayVideo.visibility = View.GONE
            }

            binding.radioGroup.removeAllViews()
            soal.pilihan.forEachIndexed { index, pilihan ->
                val radioButton = RadioButton(requireContext()).apply {
                    id = index
                    text = pilihan
                    textSize = 16f
                    setPadding(16, 16, 16, 16)
                }
                binding.radioGroup.addView(radioButton)
            }

            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                viewModel.selectAnswer(checkedId)
            }
        }

        viewModel.currentIndex.observe(viewLifecycleOwner) { index ->
            val total = viewModel.getTotalSoal()
            binding.tvSoalCounter.text = "Soal ${index + 1} dari $total"
            binding.tvMotivasi.text = if (index < total / 2) "Ayo semangat!" else "Sedikit lagi!"
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgress.text = "$progress%"
        }

        viewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                val score = viewModel.score.value ?: 0
                saveNilai(score)
                showResultDialog(score)
            }
        }

        binding.btnKembali.setOnClickListener {
            viewModel.previousSoal()
        }

        binding.btnSelanjutnya.setOnClickListener {
            viewModel.nextSoal()
        }

        binding.btnClose.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun saveNilai(score: Int) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
        val userId = TokenManager.getUserId()
        val userName = TokenManager.getUserName()

        if (userId.isNotBlank()) {
            val nilai = NilaiSiswa(
                id = System.currentTimeMillis().toInt(),
                siswaId = userId,
                namaSiswa = userName,
                tingkat = tingkat,
                nilai = score,
                totalSoal = viewModel.getTotalSoal(),
                benar = viewModel.getCorrectCount(),
                tanggal = dateFormat.format(Date())
            )
            StaticData.addNilaiSiswa(nilai)
        }

        val token = TokenManager.getToken()
        if (token.isNotBlank() && userId.isNotBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    apiService.createNilai(
                        token, userId,
                        CreateNilaiRequest(
                            soal_id = "latihan-$tingkat",
                            nilai = score.toDouble(),
                            catatan = "Benar ${viewModel.getCorrectCount()} dari ${viewModel.getTotalSoal()} - Level $tingkat"
                        )
                    )
                } catch (_: Exception) {}
            }
        }
    }

    private fun showResultDialog(score: Int) {
        val userIdStr = TokenManager.getUserId()
        val userIdInt = userIdStr.hashCode()

        // Fuzzy inputs
        val ketepatan = viewModel.getKetepatanPersen()
        val kecepatanDetik = viewModel.getAverageTimePerSoal()
        val tingkatSebelumnya = if (tingkat == "Pre-test") {
            0.0 // Pre-test: default Mudah = 0
        } else {
            StaticData.getFuzzyOutputValue(userIdInt)
        }

        // Calculate fuzzy output
        val fuzzyResult = FuzzyMamdani.calculate(ketepatan, kecepatanDetik, tingkatSebelumnya)
        val assignedLevel = fuzzyResult.outputLevel
        val fuzzyValue = fuzzyResult.outputValue

        // Save fuzzy output for next iteration
        StaticData.setFuzzyOutputValue(userIdInt, fuzzyValue)
        StaticData.setCurrentLevel(userIdInt, assignedLevel)
        StaticData.unlockLevel(userIdInt, assignedLevel)
        if (assignedLevel == "Sedang" || assignedLevel == "Sulit") {
            StaticData.unlockLevel(userIdInt, "Mudah")
        }
        if (assignedLevel == "Sulit") {
            StaticData.unlockLevel(userIdInt, "Sedang")
        }

        // Format details
        val totalTime = viewModel.getTotalTimeSeconds()
        val title = if (tingkat == "Pre-test") "Hasil Pre-test" else "Hasil Latihan - $tingkat"
        val activeRulesCount = fuzzyResult.details.activeRules.size

        val message = StringBuilder()
        message.appendLine("Nilai: $score (Benar ${viewModel.getCorrectCount()}/${viewModel.getTotalSoal()})")
        message.appendLine("Waktu: ${String.format("%.0f", totalTime)} detik (rata-rata ${String.format("%.1f", kecepatanDetik)} detik/soal)")
        message.appendLine()
        message.appendLine("-- Analisis Fuzzy Mamdani --")
        message.appendLine("Ketepatan: ${String.format("%.0f", ketepatan)}%")
        message.appendLine("Kecepatan: ${String.format("%.1f", kecepatanDetik)} detik/soal")
        message.appendLine("Tingkat sebelumnya: ${String.format("%.1f", tingkatSebelumnya)}")
        message.appendLine("Rules aktif: $activeRulesCount")
        message.appendLine("Output defuzzifikasi: ${String.format("%.2f", fuzzyValue)}")
        message.appendLine()
        message.appendLine("Sistem menempatkan kamu di level: $assignedLevel")

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(message.toString())
            .setPositiveButton("Lanjut ke Level $assignedLevel") { _, _ ->
                val bundle = Bundle().apply { putString("tingkat", assignedLevel) }
                findNavController().navigate(R.id.action_pilihLevel_to_latihan, bundle)
            }
            .setCancelable(false)

        if (tingkat != "Pre-test") {
            dialogBuilder.setNegativeButton("Kembali") { _, _ ->
                findNavController().navigateUp()
            }
        }

        dialogBuilder.show()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
