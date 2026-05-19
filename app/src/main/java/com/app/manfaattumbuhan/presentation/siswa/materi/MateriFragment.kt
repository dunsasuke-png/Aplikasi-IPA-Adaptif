package com.app.manfaattumbuhan.presentation.siswa.materi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.repository.TumbuhanRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentMateriBinding
import com.app.manfaattumbuhan.domain.usecase.GetTumbuhanUseCase
import com.app.manfaattumbuhan.presentation.adapter.TumbuhanAdapter
import androidx.fragment.app.activityViewModels

class MateriFragment : Fragment() {

    private var _binding: FragmentMateriBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: TumbuhanAdapter

    private val viewModel: MateriViewModel by activityViewModels {
        MateriViewModelFactory(GetTumbuhanUseCase(TumbuhanRepositoryImpl()))
    }

    private var currentLevel = "mudah"

    // ──────────────────────────────────────────────────────────────────────
    // Unlock Level Logic
    // Aturan:
    //   Pretest → Mudah  : hanya Mudah yang terbuka
    //   Pretest → Sedang : Mudah + Sedang terbuka
    //   Pretest → Sulit  : semua terbuka
    //   Setelah soal naik ke Sulit: semua terbuka (dan tidak pernah turun)
    // ──────────────────────────────────────────────────────────────────────

    private fun isLevelUnlocked(level: String): Boolean {
        val userId = TokenManager.getUserId()
        // Belum pretest → tidak ada yang terbuka (seharusnya tidak sampai sini)
        if (!TokenManager.isPretestDone(userId)) return false

        // Jika pernah sampai Sulit → semua terbuka selamanya
        if (TokenManager.hasEverReachedSulit(userId)) return true

        val unlockedLevels = TokenManager.getUnlockedLevels(userId)
        return when (level) {
            "mudah"  -> unlockedLevels.contains("Mudah") || unlockedLevels.isNotEmpty()
            "sedang" -> unlockedLevels.contains("Sedang") || unlockedLevels.contains("Sulit")
            "sulit"  -> unlockedLevels.contains("Sulit")
            else     -> false
        }
    }


    // ──────────────────────────────────────────────────────────────────────

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMateriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TokenManager.init(requireContext())

        // ── LOCK: belum pretest → tampilkan terkunci ───────────────────────
        val userId = TokenManager.getUserId()
        if (!TokenManager.isPretestDone(userId)) {
            binding.tvLevelTitle.text    = "🔒 Materi Terkunci"
            binding.tvPageIndicator.text = "Selesaikan Pre-test terlebih dahulu"
            binding.rvTumbuhan.visibility = View.GONE
            binding.btnPrevPage.isEnabled = false
            binding.btnNextPage.isEnabled = false
            binding.tvEmptyMateri.visibility = View.VISIBLE
            binding.tvEmptyMateri.text = "🔒 Kerjakan Pre-test terlebih dahulu\nuntuk membuka materi pembelajaran."
            return
        }
        // ────────────────────────────────────────────────────────────────────

        // Mulai dari mudah

        // Setup adapter
        adapter = TumbuhanAdapter { tumbuhan ->
            val bundle = Bundle().apply {
                putInt("tumbuhanId",           tumbuhan.id)
                putString("tumbuhanNama",      tumbuhan.nama)
                putString("tumbuhanDeskripsi", tumbuhan.deskripsi)
                putString("tumbuhanManfaat",   tumbuhan.manfaat)
                putInt("tumbuhanImage",        tumbuhan.imageRes)
                putString("tumbuhanGambarUrl", tumbuhan.gambarUrl ?: "")
                putString("tumbuhanVideoUrl",  tumbuhan.videoUrl  ?: "")
                putString("currentLevel",      currentLevel)   // ← kirim level saat ini
            }
            findNavController().navigate(R.id.action_materi_to_detail, bundle)
        }

        binding.rvTumbuhan.layoutManager = GridLayoutManager(context, 2)
        binding.rvTumbuhan.adapter = adapter

        // Navigasi antar level
        binding.btnPrevPage.setOnClickListener {
            val prev = when (currentLevel) {
                "sedang" -> "mudah"
                "sulit"  -> "sedang"
                else     -> return@setOnClickListener
            }
            currentLevel = prev
            loadCurrentLevel()
        }

        binding.btnNextPage.setOnClickListener {
            val next = when (currentLevel) {
                "mudah"  -> "sedang"
                "sedang" -> "sulit"
                else     -> return@setOnClickListener
            }
            if (!isLevelUnlocked(next)) {
                Toast.makeText(requireContext(),
                    "Level ${next.replaceFirstChar { it.uppercase() }} belum terbuka.",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentLevel = next
            loadCurrentLevel()
        }


        // Observe
        viewModel.isLoading.observe(viewLifecycleOwner) { loading ->
            binding.progressBar.visibility  = if (loading) View.VISIBLE  else View.GONE
            binding.rvTumbuhan.visibility   = if (loading) View.INVISIBLE else View.VISIBLE
            if (loading) binding.tvEmptyMateri.visibility = View.GONE
        }

        viewModel.tumbuhanList.observe(viewLifecycleOwner) { list ->
            adapter.lockedIndices = emptySet()  // semua item di level ini terbuka
            adapter.submitList(list)
            adapter.notifyDataSetChanged()
            binding.tvEmptyMateri.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            updateNavAndSelesaiButton()
        }

        loadCurrentLevel()
    }


    private fun loadCurrentLevel() {
        val uid        = TokenManager.getUserId()
        val levelLabel = currentLevel.replaceFirstChar { it.uppercase() }
        val levelIndex = when (currentLevel) { "sedang" -> 2; "sulit" -> 3; else -> 1 }
        val unlocked   = TokenManager.getUnlockedLevels(uid)
        val maxLevel   = when {
            TokenManager.hasEverReachedSulit(uid) || unlocked.contains("Sulit")  -> 3
            unlocked.contains("Sedang") -> 2
            else -> 1
        }

        binding.tvLevelTitle.text    = "Tingkat: $levelLabel"
        binding.tvPageIndicator.text = "Halaman $levelIndex dari $maxLevel"

        updateNavAndSelesaiButton()
        viewModel.loadTumbuhan(currentLevel)
    }


    private fun updateNavAndSelesaiButton() {
        val hasPrev = currentLevel != "mudah"
        val hasNext = when (currentLevel) {
            "mudah"  -> isLevelUnlocked("sedang")
            "sedang" -> isLevelUnlocked("sulit")
            else     -> false
        }
        binding.btnPrevPage.isEnabled = hasPrev
        binding.btnNextPage.isEnabled = hasNext
        binding.btnPrevPage.alpha = if (hasPrev) 1f else 0.3f
        binding.btnNextPage.alpha = if (hasNext) 1f else 0.3f
    }

    override fun onResume() {
        super.onResume()
        updateNavAndSelesaiButton()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
