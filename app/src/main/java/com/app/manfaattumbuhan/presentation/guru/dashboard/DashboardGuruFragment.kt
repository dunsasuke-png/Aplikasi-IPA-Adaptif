package com.app.manfaattumbuhan.presentation.guru.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentDashboardGuruBinding
import com.bumptech.glide.Glide

class DashboardGuruFragment : Fragment() {

    private var _binding: FragmentDashboardGuruBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardGuruBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        loadGuruPhoto()

        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_profil)
        }

        binding.cardKelolaMateri.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_materi)
        }

        binding.cardKelolaSoal.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_soal)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
