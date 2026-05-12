package com.app.manfaattumbuhan.presentation.siswa.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentSiswaDashboardBinding

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

        binding.cardLatihanSoal.setOnClickListener {
            val userId = TokenManager.getUserId()
            if (TokenManager.isPretestDone(userId)) {
                findNavController().navigate(R.id.action_dashboard_to_pilih_level)
            } else {
                val bundle = Bundle().apply { putString("tingkat", "Pre-test") }
                findNavController().navigate(R.id.action_dashboard_to_latihan, bundle)
            }
        }

        binding.cardRiwayatNilai.setOnClickListener {
            findNavController().navigate(R.id.action_dashboard_to_riwayat)
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
