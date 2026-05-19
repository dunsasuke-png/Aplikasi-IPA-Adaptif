package com.app.manfaattumbuhan.presentation.siswa.materi

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.activityViewModels
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentDetailMateriBinding
import com.bumptech.glide.Glide

class DetailMateriFragment : Fragment() {

    private var _binding: FragmentDetailMateriBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null
    private var tingkatFromArg: String = "mudah"   // level saat ini, diisi dari args

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMateriBinding.inflate(inflater, container, false)
        return binding.root
    }

    private val viewModel: MateriViewModel by activityViewModels()

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        TokenManager.init(requireContext())

        // Initial load from arguments if selectedTumbuhan is null, or just let ViewModel handle it
        val initialId  = arguments?.getInt("tumbuhanId") ?: 0
        tingkatFromArg = arguments?.getString("currentLevel") ?: "mudah"

        if (viewModel.selectedTumbuhan.value == null || viewModel.selectedTumbuhan.value?.id != initialId) {
            viewModel.tumbuhanList.value?.find { it.id == initialId }?.let {
                viewModel.selectTumbuhan(it)
            }
        }

        viewModel.selectedTumbuhan.observe(viewLifecycleOwner) { tumbuhan ->
            if (tumbuhan == null) return@observe
            bindMateriData(tumbuhan)
        }

        binding.btnSudahBelajar.setOnClickListener {
            val userId = TokenManager.getUserId()
            if (userId.isBlank()) {
                android.widget.Toast.makeText(requireContext(), "Sesi tidak valid, coba login ulang.", android.widget.Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            // Simpan status selesai
            TokenManager.setMateriLevelDone(userId, tingkatFromArg)
            updateStudiedButton(true)

            // Navigasi ke halaman Pilih Level Soal
            findNavController().navigate(R.id.action_detail_to_pilih_level)
        }

        binding.btnSebelumnya.setOnClickListener {
            val currentList = viewModel.tumbuhanList.value ?: return@setOnClickListener
            val currentMateri = viewModel.selectedTumbuhan.value ?: return@setOnClickListener
            val currentIndex = currentList.indexOfFirst { it.id == currentMateri.id }

            if (currentIndex > 0) {
                viewModel.selectTumbuhan(currentList[currentIndex - 1])
            } else {
                Toast.makeText(requireContext(), "Ini adalah materi pertama.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnSelanjutnya.setOnClickListener {
            val currentList = viewModel.tumbuhanList.value ?: return@setOnClickListener
            val currentMateri = viewModel.selectedTumbuhan.value ?: return@setOnClickListener
            val currentIndex = currentList.indexOfFirst { it.id == currentMateri.id }
            
            if (currentIndex != -1 && currentIndex < currentList.size - 1) {
                // Check if next materi is unlocked
                val nextMateri = currentList[currentIndex + 1]
                val maxUnlocked = TokenManager.getMaxUnlockedMateri(TokenManager.getUserId())
                val everSulit = TokenManager.hasEverReachedSulit(TokenManager.getUserId())
                
                // If the next materi is locked, show a message.
                // We'll roughly assume if next index is >= maxUnlocked, it's locked.
                // Actually, MateriFragment does more complex logic. Let's simplify:
                // If it's already studied, it's open. Otherwise, check index vs maxUnlocked.
                val alreadyStudiedNext = TokenManager.isMateriStudied(TokenManager.getUserId(), nextMateri.id)
                if (alreadyStudiedNext || everSulit || (currentIndex + 1) < maxUnlocked) {
                    viewModel.selectTumbuhan(nextMateri)
                } else {
                    Toast.makeText(requireContext(), "Selesaikan kuis untuk membuka materi selanjutnya!", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Ini adalah materi terakhir.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    private fun bindMateriData(tumbuhan: com.app.manfaattumbuhan.domain.model.Tumbuhan) {
        binding.tvNamaTumbuhan.text = tumbuhan.nama
        binding.tvManfaat.text = tumbuhan.manfaat

        if (!tumbuhan.gambarUrl.isNullOrBlank()) {
            Glide.with(this)
                .load(tumbuhan.gambarUrl)
                .placeholder(R.drawable.img_padi)
                .error(R.drawable.img_padi)
                .into(binding.imgTumbuhan)
        } else if (tumbuhan.imageRes != 0) {
            binding.imgTumbuhan.setImageResource(tumbuhan.imageRes)
        }

        exoPlayer?.release()
        exoPlayer = null
        if (!tumbuhan.videoUrl.isNullOrBlank()) {
            binding.videoContainer.visibility = View.VISIBLE
            val player = ExoPlayer.Builder(requireContext()).build()
            exoPlayer = player
            binding.videoPlayerInline.player = player
            // Disable all seeking — play/pause only
            binding.videoPlayerInline.setShowRewindButton(false)
            binding.videoPlayerInline.setShowFastForwardButton(false)
            binding.videoPlayerInline.setShowNextButton(false)
            binding.videoPlayerInline.setShowPreviousButton(false)
            binding.videoPlayerInline.setShowShuffleButton(false)
            binding.videoPlayerInline.setShowSubtitleButton(false)
            binding.videoPlayerInline.setShowMultiWindowTimeBar(false)
            player.setMediaItem(MediaItem.fromUri(Uri.parse(tumbuhan.videoUrl!!)))
            player.prepare()
            player.playWhenReady = false
        } else {
            binding.videoContainer.visibility = View.GONE
        }

        val userId     = TokenManager.getUserId()
        val alreadyDone = TokenManager.isMateriLevelDone(userId, tingkatFromArg)
        updateStudiedButton(alreadyDone)

        // Update progress & tombol navigasi
        val list = viewModel.tumbuhanList.value ?: emptyList()
        val currentIndex = list.indexOfFirst { it.id == tumbuhan.id }
        if (currentIndex != -1) {
            binding.tvProgressMateri.text = "Halaman ${currentIndex + 1} dari ${list.size}"
            binding.btnSebelumnya.visibility = if (currentIndex == 0) View.INVISIBLE else View.VISIBLE
        }

        // "Sudah Memahami Materi" hanya di materi TERAKHIR level ini
        val isLastInLevel = list.lastOrNull()?.id == tumbuhan.id
        binding.btnSudahBelajar.visibility = if (isLastInLevel) View.VISIBLE else View.GONE

        // "Selanjutnya" disembunyikan di materi terakhir
        binding.btnSelanjutnya.visibility = if (isLastInLevel) View.GONE else View.VISIBLE
    }

    private fun updateStudiedButton(done: Boolean) {
        if (done) {
            binding.btnSudahBelajar.text      = "✓ Sudah Memahami Materi"
            binding.btnSudahBelajar.isEnabled = false
            binding.btnSudahBelajar.alpha     = 0.6f
        } else {
            binding.btnSudahBelajar.text      = "Sudah Memahami Materi"
            binding.btnSudahBelajar.isEnabled = true
            binding.btnSudahBelajar.alpha     = 1f
        }
    }

    override fun onDestroyView() {
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroyView()
        _binding = null
    }
}
