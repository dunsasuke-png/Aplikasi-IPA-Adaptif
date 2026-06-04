package com.app.manfaattumbuhan.presentation.siswa.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentSiswaDashboardBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SiswaDashboardFragment : Fragment() {

    private var _binding: FragmentSiswaDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSiswaDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        syncProgressFromPrefs()

        val nama = TokenManager.getUserName()
        binding.tvGreeting.text = "Halo, ${nama.split(" ").firstOrNull() ?: nama}!"

        val userId = TokenManager.getUserId()
        val pretestDone = TokenManager.isPretestDone(userId)

        updatePretestCard(pretestDone)
        updateLatihanCard(pretestDone)

        binding.cardPretest.setOnClickListener {
            if (TokenManager.isPretestDone(TokenManager.getUserId())) {
                Toast.makeText(requireContext(), "Pre-test sudah selesai", Toast.LENGTH_SHORT).show()
            } else {
                val bundle = Bundle().apply { putString("tingkat", "Pre-test") }
                findNavController().navigate(R.id.action_dashboard_to_latihan, bundle)
            }
        }

        binding.cardLatihanSoal.setOnClickListener {
            val uid          = TokenManager.getUserId()
            val currentLevel = TokenManager.getActiveStudyLevel(uid)
            if (!TokenManager.isPretestDone(uid)) {
                Toast.makeText(requireContext(), "Selesaikan pre-test terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else if (!TokenManager.isMateriLevelDone(uid, currentLevel)) {
                Toast.makeText(requireContext(), "Selesaikan membaca materi ${currentLevel.replaceFirstChar { it.uppercase() }} terlebih dahulu", Toast.LENGTH_SHORT).show()
            } else {
                findNavController().navigate(R.id.action_dashboard_to_pilih_level)
            }
        }

        binding.cardRiwayatNilai.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_riwayat)
        }

        binding.btnPetunjuk.setOnClickListener {
            showPetunjukDialog()
        }
    }

    override fun onResume() {
        super.onResume()
        val userId = TokenManager.getUserId()
        val pretestDone = TokenManager.isPretestDone(userId)
        updatePretestCard(pretestDone)
        updateLatihanCard(pretestDone)
    }

    private fun updatePretestCard(pretestDone: Boolean) {
        if (pretestDone) {
            binding.cardPretest.alpha = 0.5f
            binding.tvPretestDesc.text = "Pre-test sudah selesai."
        } else {
            binding.cardPretest.alpha = 1f
            binding.tvPretestDesc.text = "Kerjakan pre-test untuk menentukan level kamu."
        }
    }

    private fun updateLatihanCard(pretestDone: Boolean) {
        val userId       = TokenManager.getUserId()
        val currentLevel = TokenManager.getActiveStudyLevel(userId)
        val materiDone   = TokenManager.isMateriLevelDone(userId, currentLevel)

        if (pretestDone && materiDone) {
            binding.cardLatihanSoal.alpha = 1f
            binding.tvLatihanDesc.text = "Uji pemahamanmu dengan kuis interaktif."
            binding.imgLatihanIcon.setImageResource(R.drawable.ic_arrow_right)
            binding.imgLatihanIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.green_primary)
        } else if (pretestDone) {
            binding.cardLatihanSoal.alpha = 0.5f
            binding.tvLatihanDesc.text = "Selesaikan membaca materi ${currentLevel.replaceFirstChar { it.uppercase() }} terlebih dahulu."
            binding.imgLatihanIcon.setImageResource(R.drawable.ic_lock)
            binding.imgLatihanIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray_text)
        } else {
            binding.cardLatihanSoal.alpha = 0.5f
            binding.tvLatihanDesc.text = "Selesaikan pre-test terlebih dahulu."
            binding.imgLatihanIcon.setImageResource(R.drawable.ic_lock)
            binding.imgLatihanIcon.imageTintList = ContextCompat.getColorStateList(requireContext(), R.color.gray_text)
        }
    }

    private fun syncProgressFromPrefs() {
        val userId = TokenManager.getUserId()
        if (userId.isBlank()) return

        val userIdInt = userId.hashCode()
        val savedCurrentLevel = TokenManager.getCurrentLevel(userId)
        val savedUnlocked = TokenManager.getUnlockedLevels(userId)
        val savedFuzzy = TokenManager.getFuzzyOutputValue(userId)

        if (!savedCurrentLevel.isNullOrBlank()) {
            StaticData.setCurrentLevel(userIdInt, savedCurrentLevel)
        }

        if (savedUnlocked.isNotEmpty()) {
            val target = StaticData.getUnlockedLevels(userIdInt)
            target.clear()
            target.addAll(savedUnlocked)
        }

        if (savedFuzzy > 0.0) {
            StaticData.setFuzzyOutputValue(userIdInt, savedFuzzy)
        }
    }

    private fun showPetunjukDialog() {
        val message = StringBuilder()
        message.appendLine("👋 Halo! Selamat datang di Aplikasi IPA Adaptif!")
        message.appendLine("Ikuti langkah-langkah belajar ini ya:\n")
        
        message.appendLine("📊 1. PRE-TEST (Ujian Awal)")
        message.appendLine("   🔸 Dikerjakan cukup 1x saja.")
        message.appendLine("   🔸 Hasilnya untuk menentukan level belajar awalmu.\n")
        
        message.appendLine("📖 2. BELAJAR MATERI")
        message.appendLine("   🔸 Materi terbuka sesuai dengan level kemampuanmu.")
        message.appendLine("   🔸 Tekan tombol \"Sudah Memahami Materi\" di halaman terakhir untuk membuka latihan soal level tersebut!\n")
        
        message.appendLine("🎮 3. LATIHAN SOAL")
        message.appendLine("   🔸 Latihan baru terbuka jika materi level terkait sudah dibaca.")
        message.appendLine("   🔸 Terdiri dari maksimal 20 pertanyaan.")
        message.appendLine("   🔸 Terdapat waktu hitung mundur (timer), jadi tetap fokus ya!\n")
        
        message.appendLine("🏆 4. RIWAYAT NILAI")
        message.appendLine("   🔸 Tempat melihat semua daftar nilai hasil latihanmu.\n")
        
        message.appendLine("👤 5. PROFIL")
        message.appendLine("   🔸 Kamu bisa ganti foto profil, nama, atau password di sini.")

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("📖 Panduan Belajar")
            .setMessage(message.toString())
            .setPositiveButton("Siap, Mengerti! 👍", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
