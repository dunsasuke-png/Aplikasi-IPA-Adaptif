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
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentDetailMateriBinding
import com.bumptech.glide.Glide

class DetailMateriFragment : Fragment() {

    private var _binding: FragmentDetailMateriBinding? = null
    private val binding get() = _binding!!
    private var exoPlayer: ExoPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMateriBinding.inflate(inflater, container, false)
        return binding.root
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())

        val materiId = arguments?.getInt("tumbuhanId") ?: 0
        val nama = arguments?.getString("tumbuhanNama") ?: ""
        val deskripsi = arguments?.getString("tumbuhanDeskripsi") ?: ""
        val manfaat = arguments?.getString("tumbuhanManfaat") ?: ""
        val imageRes = arguments?.getInt("tumbuhanImage") ?: 0
        val gambarUrl = arguments?.getString("tumbuhanGambarUrl") ?: ""
        val videoUrl = arguments?.getString("tumbuhanVideoUrl") ?: ""

        binding.tvNamaTumbuhan.text = nama
        binding.tvDeskripsi.text = deskripsi
        binding.tvManfaat.text = manfaat

        if (gambarUrl.isNotBlank()) {
            Glide.with(this)
                .load(gambarUrl)
                .placeholder(R.drawable.img_padi)
                .error(R.drawable.img_padi)
                .into(binding.imgTumbuhan)
        } else if (imageRes != 0) {
            binding.imgTumbuhan.setImageResource(imageRes)
        }

        if (videoUrl.isNotBlank()) {
            binding.videoContainer.visibility = View.VISIBLE
            val player = ExoPlayer.Builder(requireContext()).build()
            exoPlayer = player
            binding.videoPlayerInline.player = player
            player.setMediaItem(MediaItem.fromUri(Uri.parse(videoUrl)))
            player.prepare()
            player.playWhenReady = false
        }

        val userId = TokenManager.getUserId()
        val alreadyStudied = TokenManager.isMateriStudied(userId, materiId)
        updateStudiedButton(alreadyStudied)

        binding.btnSudahBelajar.setOnClickListener {
            TokenManager.setMateriStudied(userId, materiId)
            updateStudiedButton(true)
            Toast.makeText(requireContext(), "Materi \"$nama\" sudah dipelajari!", Toast.LENGTH_SHORT).show()
        }

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun updateStudiedButton(studied: Boolean) {
        if (studied) {
            binding.btnSudahBelajar.text = "Materi Sudah Dipelajari"
            binding.btnSudahBelajar.isEnabled = false
            binding.btnSudahBelajar.alpha = 0.6f
        } else {
            binding.btnSudahBelajar.text = "Saya Sudah Mempelajari Materi"
            binding.btnSudahBelajar.isEnabled = true
            binding.btnSudahBelajar.alpha = 1f
        }
    }

    override fun onDestroyView() {
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroyView()
        _binding = null
    }
}
