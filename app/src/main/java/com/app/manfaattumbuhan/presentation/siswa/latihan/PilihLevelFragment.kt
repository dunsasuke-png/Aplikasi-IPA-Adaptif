package com.app.manfaattumbuhan.presentation.siswa.latihan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentPilihLevelBinding

class PilihLevelFragment : Fragment() {

    private var _binding: FragmentPilihLevelBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPilihLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        syncProgressFromPrefs()
        updateLevelCards()

        binding.cardPretest.setOnClickListener {
            val userIdStr = TokenManager.getUserId()
            if (!TokenManager.isPretestDone(userIdStr)) {
                navigateToLatihan("Pre-test")
            }
        }

        binding.cardMudah.setOnClickListener {
            val userIdStr = TokenManager.getUserId()
            val userId = userIdStr.hashCode()
            val currentLevel = StaticData.getCurrentLevel(userId)
            if (currentLevel == "Mudah") {
                navigateToLatihan("Mudah")
            }
        }

        binding.cardSedang.setOnClickListener {
            val userIdStr = TokenManager.getUserId()
            val userId = userIdStr.hashCode()
            val currentLevel = StaticData.getCurrentLevel(userId)
            if (currentLevel == "Sedang") {
                navigateToLatihan("Sedang")
            }
        }

        binding.cardSulit.setOnClickListener {
            val userIdStr = TokenManager.getUserId()
            val userId = userIdStr.hashCode()
            val currentLevel = StaticData.getCurrentLevel(userId)
            if (currentLevel == "Sulit") {
                navigateToLatihan("Sulit")
            }
        }

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onResume() {
        super.onResume()
        syncProgressFromPrefs()  // ← sync ulang setiap kali halaman muncul kembali
        updateLevelCards()
    }

    private fun updateLevelCards() {
        val userIdStr = TokenManager.getUserId()
        val userId = userIdStr.hashCode()
        val currentLevel = StaticData.getCurrentLevel(userId)
        val pretestDone = TokenManager.isPretestDone(userIdStr)

        binding.cardPretest.visibility = if (!pretestDone && currentLevel == null) View.VISIBLE else View.GONE

        updateCard(
            isCurrent = currentLevel == "Mudah",
            cardView = binding.cardMudah,
            titleView = binding.tvMudahTitle,
            descView = binding.tvMudahDesc,
            iconView = binding.icMudahLock,
            arrowView = binding.icMudahArrow
        )

        updateCard(
            isCurrent = currentLevel == "Sedang",
            cardView = binding.cardSedang,
            titleView = binding.tvSedangTitle,
            descView = binding.tvSedangDesc,
            iconView = binding.icSedangLock,
            arrowView = binding.icSedangArrow
        )

        updateCard(
            isCurrent = currentLevel == "Sulit",
            cardView = binding.cardSulit,
            titleView = binding.tvSulitTitle,
            descView = binding.tvSulitDesc,
            iconView = binding.icSulitLock,
            arrowView = binding.icSulitArrow
        )
    }

    private fun updateCard(
        isCurrent: Boolean,
        cardView: com.google.android.material.card.MaterialCardView,
        titleView: android.widget.TextView,
        descView: android.widget.TextView,
        iconView: android.widget.ImageView,
        arrowView: android.widget.ImageView
    ) {
        if (isCurrent) {
            cardView.setCardBackgroundColor(requireContext().getColor(R.color.green_primary))
            cardView.strokeColor = requireContext().getColor(R.color.green_dark)
            titleView.setTextColor(requireContext().getColor(R.color.white))
            descView.setTextColor(requireContext().getColor(R.color.white))
            descView.text = "Level kamu saat ini - Klik untuk mulai"
            iconView.setImageResource(R.drawable.ic_quiz)
            iconView.setColorFilter(requireContext().getColor(R.color.white))
            arrowView.setImageResource(R.drawable.ic_arrow_right)
            arrowView.setColorFilter(requireContext().getColor(R.color.white))
            cardView.isEnabled = true
            cardView.isClickable = true
        } else {
            cardView.setCardBackgroundColor(requireContext().getColor(R.color.white))
            cardView.strokeColor = requireContext().getColor(R.color.gray_border)
            titleView.setTextColor(requireContext().getColor(R.color.gray_text))
            descView.setTextColor(requireContext().getColor(R.color.gray_text))
            descView.text = "Terkunci"
            iconView.setImageResource(R.drawable.ic_lock)
            iconView.setColorFilter(requireContext().getColor(R.color.gray_text))
            arrowView.setImageResource(R.drawable.ic_lock)
            arrowView.setColorFilter(requireContext().getColor(R.color.gray_text))
            cardView.isEnabled = false
            cardView.isClickable = false
        }
    }

    private fun navigateToLatihan(tingkat: String) {
        val bundle = Bundle().apply {
            putString("tingkat", tingkat)
        }
        findNavController().navigate(R.id.action_pilihLevel_to_latihan, bundle)
    }

    private fun syncProgressFromPrefs() {
        val userIdStr = TokenManager.getUserId()
        if (userIdStr.isBlank()) return

        val userId = userIdStr.hashCode()
        val savedCurrentLevel = TokenManager.getCurrentLevel(userIdStr)
        val savedUnlocked = TokenManager.getUnlockedLevels(userIdStr)
        val savedFuzzy = TokenManager.getFuzzyOutputValue(userIdStr)

        if (!savedCurrentLevel.isNullOrBlank()) {
            StaticData.setCurrentLevel(userId, savedCurrentLevel)
        }

        if (savedUnlocked.isNotEmpty()) {
            val target = StaticData.getUnlockedLevels(userId)
            target.clear()
            target.addAll(savedUnlocked)
        }

        if (savedFuzzy > 0.0) {
            StaticData.setFuzzyOutputValue(userId, savedFuzzy)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
