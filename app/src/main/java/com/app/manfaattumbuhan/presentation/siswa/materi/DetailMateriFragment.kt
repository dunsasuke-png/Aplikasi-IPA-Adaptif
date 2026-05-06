package com.app.manfaattumbuhan.presentation.siswa.materi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.databinding.FragmentDetailMateriBinding

class DetailMateriFragment : Fragment() {

    private var _binding: FragmentDetailMateriBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMateriBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nama = arguments?.getString("tumbuhanNama") ?: ""
        val deskripsi = arguments?.getString("tumbuhanDeskripsi") ?: ""
        val manfaat = arguments?.getString("tumbuhanManfaat") ?: ""
        val imageRes = arguments?.getInt("tumbuhanImage") ?: 0

        binding.tvNamaTumbuhan.text = nama
        binding.tvDeskripsi.text = deskripsi
        binding.tvManfaat.text = manfaat
        if (imageRes != 0) {
            binding.imgTumbuhan.setImageResource(imageRes)
        }

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
