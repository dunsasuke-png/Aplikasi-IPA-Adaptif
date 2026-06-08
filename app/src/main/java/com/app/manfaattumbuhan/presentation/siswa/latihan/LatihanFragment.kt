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
            binding.tvProgress.text = "Progres $progress%"
        }

        viewModel.isFinished.observe(viewLifecycleOwner) { finished ->
            if (finished) {
                stopTimer()
                val score = viewModel.score.value ?: 0
                handleFinishQuiz(score)
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

    private fun handleFinishQuiz(score: Int) {
        val userIdStr = TokenManager.getUserId()
        val userIdInt = userIdStr.hashCode()

        // Fuzzy inputs
        val ketepatan = viewModel.getKetepatanPersen()
        val kecepatanDetik = viewModel.getTotalTimeSeconds()
        val tingkatSebelumnya = if (tingkat == "Pre-test") {
            0.0
        } else {
            StaticData.getFuzzyOutputValue(userIdInt)
        }

        // Calculate fuzzy output
        val fuzzyResult = FuzzyMamdani.calculate(ketepatan, kecepatanDetik, tingkatSebelumnya)
        val assignedLevel = fuzzyResult.outputLevel
        val fuzzyValue = fuzzyResult.outputValue

        saveNilaiToDbAndLocal(score, assignedLevel)
        showResultDialog(score, ketepatan, tingkatSebelumnya, assignedLevel, fuzzyValue)
    }

    private fun saveNilaiToDbAndLocal(score: Int, assignedLevel: String) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id"))
        val userId = TokenManager.getUserId()
        val userName = TokenManager.getUserName()

        val jumlahBenar = viewModel.getCorrectCount()
        val jumlahSoal = viewModel.getTotalSoal()
        val waktuPengerjaanDetik = viewModel.getTotalTimeSeconds().toInt()

        if (userId.isNotBlank()) {
            val nilai = NilaiSiswa(
                id = System.currentTimeMillis().toInt(),
                siswaId = userId,
                namaSiswa = userName,
                tingkat = tingkat,
                nilai = score,
                totalSoal = jumlahSoal,
                benar = jumlahBenar,
                tanggal = dateFormat.format(Date())
            )
            StaticData.addNilaiSiswa(nilai)
        }

        if (userId.isNotBlank()) {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    val response = apiService.createNilai(
                        CreateNilaiRequest(
                            siswa_id = userId,
                            soal_id = "latihan-$tingkat",
                            nilai = score.toDouble(),
                            catatan = "Benar $jumlahBenar dari $jumlahSoal - Level $tingkat",
                            jumlah_benar = jumlahBenar,
                            jumlah_soal = jumlahSoal,
                            waktu_pengerjaan = waktuPengerjaanDetik,
                            kesulitan_sebelumnya = tingkat,
                            kesulitan_selanjutnya = assignedLevel
                        )
                    )
                    if (!response.isSuccessful) {
                        android.util.Log.e("LatihanFragment", "createNilai gagal: ${response.code()} - ${response.errorBody()?.string()}")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("LatihanFragment", "createNilai error: ${e.message}")
                }
            }
        }
    }

    private fun showResultDialog(score: Int, ketepatan: Double, tingkatSebelumnya: Double, assignedLevel: String, fuzzyValue: Double) {
        val userIdStr = TokenManager.getUserId()
        val userIdInt = userIdStr.hashCode()

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

            // Unlock materi permanen berdasarkan penempatan level
            val maxMateriByLevel = when (assignedLevel) {
                "Mudah"  -> 8
                "Sedang" -> 16
                "Sulit"  -> 24
                else     -> 0
            }
            TokenManager.applyUnlockMateriNeverDecrease(userIdStr, maxMateriByLevel)

            // Tandai: materi level yang baru terbuka harus dibaca dulu
            // (jangan hapus yang sudah done sebelumnya — prinsip tidak turun)
            // Tidak perlu reset karena setMateriLevelDone sudah one-way
        }

        // Format details
        val totalTime = viewModel.getTotalTimeSeconds()
        val minutes = totalTime.toInt() / 60
        val seconds = totalTime.toInt() % 60
        val timeFormatted = if (minutes > 0) {
            "$minutes menit $seconds detik"
        } else {
            "$seconds detik"
        }

        val title = if (tingkat == "Pre-test") "Hasil Pre-test" else "Hasil Latihan - $tingkat"

        val levelColor = when (assignedLevel) {
            "Mudah"  -> "#2E7D32" // Green
            "Sedang" -> "#E65100" // Orange
            "Sulit"  -> "#C62828" // Red
            else     -> "#000000"
        }

        var messageHtml = if (tingkat == "Pre-test") {
            """
                Nilai: $score (Benar ${viewModel.getCorrectCount()}/${viewModel.getTotalSoal()})<br/>
                Waktu: $timeFormatted<br/>
                <br/>
                Sistem menempatkan kamu di level: <b><font color="$levelColor">$assignedLevel</font></b>
            """.trimIndent()
        } else {
            """
                Nilai: $score (Benar ${viewModel.getCorrectCount()}/${viewModel.getTotalSoal()})<br/>
                Waktu: $timeFormatted<br/>
                <br/>
                Ketepatan: ${String.format(Locale.US, "%.0f", ketepatan)}%<br/>
                Tingkat sebelumnya: ${String.format(Locale.US, "%.1f", tingkatSebelumnya)}<br/>
                <br/>
                Sistem menempatkan kamu di level: <b><font color="$levelColor">$assignedLevel</font></b>
            """.trimIndent()
        }

        if (tingkat == "Sulit" && assignedLevel == "Sulit") {
            messageHtml += "<br/><br/>🎉 <b>Selamat!</b> Kamu telah menguasai level tertinggi dengan sangat baik!"
        }

        val formattedMessage = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            android.text.Html.fromHtml(messageHtml, android.text.Html.FROM_HTML_MODE_LEGACY)
        } else {
            @Suppress("DEPRECATION")
            android.text.Html.fromHtml(messageHtml)
        }

        val dialogBuilder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(title)
            .setMessage(formattedMessage)
            .setCancelable(false)

        if (tingkat == "Sulit" && assignedLevel == "Sulit") {
            dialogBuilder.setPositiveButton("Ke Beranda") { _, _ ->
                findNavController().popBackStack(R.id.siswaDashboardFragment, false)
            }
        } else {
            dialogBuilder.setPositiveButton("Lanjut ke Materi") { _, _ ->
                findNavController().popBackStack(R.id.siswaDashboardFragment, false)
                val bottomNav = requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottomNavSiswa)
                if (bottomNav != null) {
                    bottomNav.selectedItemId = R.id.materiFragment
                } else {
                    findNavController().navigate(R.id.action_latihan_to_materi)
                }
            }
            dialogBuilder.setNegativeButton("Ke Beranda") { _, _ ->
                findNavController().popBackStack(R.id.siswaDashboardFragment, false)
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
        player.playWhenReady = true
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
