package com.app.manfaattumbuhan.presentation.siswa.materi

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
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

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        exoPlayer?.release()
        exoPlayer = null
        super.onDestroyView()
        _binding = null
    }
}
