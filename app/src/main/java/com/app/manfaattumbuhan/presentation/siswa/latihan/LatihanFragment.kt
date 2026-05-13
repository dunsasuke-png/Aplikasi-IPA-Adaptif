package com.app.manfaattumbuhan.presentation.siswa.latihan

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
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
    private var exoPlayer: ExoPlayer? = null

    private val timerHandler = Handler(Looper.getMainLooper())
    private var timerStartMillis: Long = 0L
    private var isTimerRunning = false
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isTimerRunning && _binding != null) {
                val elapsed = System.currentTimeMillis() - timerStartMillis
                val seconds = (elapsed / 1000).toInt()
                val minutes = seconds / 60
                val secs = seconds % 60
                binding.tvTimer.text = String.format("%02d:%02d", minutes, secs)
                timerHandler.postDelayed(this, 1000)
            }
        }
    }

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
        if (tingkat == "Pre-test" && TokenManager.isPretestDone(TokenManager.getUserId())) {
            findNavController().navigate(R.id.action_latihan_to_pilihLevel)
            return
        }

        binding.tvStartTingkat.text = "Tingkat: $tingkat"
        binding.layoutStartOverlay.visibility = View.VISIBLE
        binding.layoutQuizContent.visibility = View.GONE

        binding.btnMulai.setOnClickListener {
            binding.layoutStartOverlay.visibility = View.GONE
            binding.layoutQuizContent.visibility = View.VISIBLE
            startQuiz()
        }

        // Tombol kiri: kembali ke halaman sebelumnya
        binding.btnBelumSiap.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun startQuiz() {
        viewModel.loadSoalByTingkat(tingkat)

        binding.tvTingkatLabel.text = tingkat
        binding.tvTimer.text = "00:00"

        fun updateNextButtonLabel() {
            val isLast = viewModel.isLastQuestion()
            binding.btnSelanjutnya.text = if (isLast) {
                getString(R.string.latihan_btn_selesai)
            } else {
                getString(R.string.latihan_btn_selanjutnya)
            }
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
            if (!isTimerRunning) {
                startTimer()
            }

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
                binding.videoContainer.visibility = View.VISIBLE
                setupInlineVideo(soal.videoUrl)
            } else {
                binding.videoContainer.visibility = View.GONE
                releasePlayer()
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

            val saved = viewModel.selectedAnswer.value
            binding.radioGroup.setOnCheckedChangeListener(null)
            if (saved != null && saved >= 0 && saved < soal.pilihan.size) {
                binding.radioGroup.check(saved)
            } else {
                binding.radioGroup.clearCheck()
            }
            binding.radioGroup.setOnCheckedChangeListener { _, checkedId ->
                if (checkedId >= 0) {
                    viewModel.selectAnswer(checkedId)
                }
            }
        }

        viewModel.currentIndex.observe(viewLifecycleOwner) { index ->
            val total = viewModel.getTotalSoal()
            if (total > 0) {
                binding.tvSoalCounter.text = "Soal ${index + 1} dari $total"
            }
            updateNextButtonLabel()
        }

        viewModel.soalList.observe(viewLifecycleOwner) { list ->
            val index = viewModel.currentIndex.value ?: 0
            binding.tvSoalCounter.text = "Soal ${index + 1} dari ${list.size}"
            updateNextButtonLabel()
        }

        viewModel.progress.observe(viewLifecycleOwner) { progress ->
            binding.progressBar.progress = progress
            binding.tvProgress.text = "$progress%"
        }

        viewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                stopTimer()
                val score = viewModel.score.value ?: 0
                saveNilai(score)
                showResultDialog(score)
            }
        }

        binding.btnKembali.setOnClickListener {
            viewModel.previousSoal()
        }

        binding.btnSelanjutnya.setOnClickListener {
            // If last question and there are unanswered questions, block finishing
            if (viewModel.isLastQuestion() && viewModel.getUnansweredCount() > 0) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(getString(R.string.latihan_alert_belum_selesai_title))
                    .setMessage(getString(R.string.latihan_alert_belum_selesai_msg))
                    .setPositiveButton("OK", null)
                    .show()
                return@setOnClickListener
            }
            viewModel.nextSoal()
        }
    }

    private fun startTimer() {
        timerStartMillis = System.currentTimeMillis()
        isTimerRunning = true
        timerHandler.post(timerRunnable)
    }

    private fun stopTimer() {
        isTimerRunning = false
        timerHandler.removeCallbacks(timerRunnable)
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
            0.0
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

        if (userIdStr.isNotBlank() && assignedLevel == "Sulit") {
            TokenManager.setEverReachedSulit(userIdStr)
        }

        if (userIdStr.isNotBlank()) {
            if (tingkat == "Pre-test") {
                TokenManager.setPretestDone(userIdStr, true)
            }
            TokenManager.saveCurrentLevel(userIdStr, assignedLevel)
            TokenManager.saveUnlockedLevels(userIdStr, StaticData.getUnlockedLevels(userIdInt))
            TokenManager.saveFuzzyOutputValue(userIdStr, fuzzyValue)
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
            .setCancelable(false)

        if (tingkat == "Pre-test") {
            dialogBuilder.setPositiveButton("Ke Beranda") { _, _ ->
                findNavController().navigate(R.id.action_latihan_to_dashboard)
            }
        } else {
            dialogBuilder.setPositiveButton("Lihat Tingkat Kesulitan") { _, _ ->
                findNavController().navigate(R.id.action_latihan_to_pilihLevel)
            }
            dialogBuilder.setNegativeButton("Kembali") { _, _ ->
                findNavController().navigateUp()
            }
        }

        dialogBuilder.show()
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun setupInlineVideo(videoUrl: String) {
        releasePlayer()
        val player = ExoPlayer.Builder(requireContext()).build()
        exoPlayer = player
        binding.videoPlayerInline.player = player

        val mediaItem = MediaItem.fromUri(Uri.parse(videoUrl))
        player.setMediaItem(mediaItem)
        player.prepare()
        player.playWhenReady = false
    }

    private fun releasePlayer() {
        exoPlayer?.release()
        exoPlayer = null
    }

    override fun onDestroyView() {
        stopTimer()
        releasePlayer()
        super.onDestroyView()
        _binding = null
    }
}
