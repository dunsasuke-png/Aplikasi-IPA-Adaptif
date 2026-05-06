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
        updateLevelCards()

        val userId = TokenManager.getUserId().hashCode()
        val currentLevel = StaticData.getCurrentLevel(userId)

        binding.cardPretest.setOnClickListener {
            navigateToLatihan("Pre-test")
        }

        binding.cardMudah.setOnClickListener {
            if (currentLevel == "Mudah") {
                navigateToLatihan("Mudah")
            }
        }

        binding.cardSedang.setOnClickListener {
            if (currentLevel == "Sedang") {
                navigateToLatihan("Sedang")
            }
        }

        binding.cardSulit.setOnClickListener {
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
        updateLevelCards()
    }

    private fun updateLevelCards() {
        val userId = TokenManager.getUserId().hashCode()
        val currentLevel = StaticData.getCurrentLevel(userId)
        val unlocked = StaticData.getUnlockedLevels(userId)

        binding.cardPretest.visibility = if (currentLevel == null) View.VISIBLE else View.GONE

        updateCard(
            isUnlocked = unlocked.contains("Mudah"),
            isCurrent = currentLevel == "Mudah",
            cardView = binding.cardMudah,
            titleView = binding.tvMudahTitle,
            descView = binding.tvMudahDesc,
            iconView = binding.icMudahLock,
            arrowView = binding.icMudahArrow
        )

        updateCard(
            isUnlocked = unlocked.contains("Sedang"),
            isCurrent = currentLevel == "Sedang",
            cardView = binding.cardSedang,
            titleView = binding.tvSedangTitle,
            descView = binding.tvSedangDesc,
            iconView = binding.icSedangLock,
            arrowView = binding.icSedangArrow
        )

        updateCard(
            isUnlocked = unlocked.contains("Sulit"),
            isCurrent = currentLevel == "Sulit",
            cardView = binding.cardSulit,
            titleView = binding.tvSulitTitle,
            descView = binding.tvSulitDesc,
            iconView = binding.icSulitLock,
            arrowView = binding.icSulitArrow
        )
    }

    private fun updateCard(
        isUnlocked: Boolean,
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
        } else if (isUnlocked) {
            cardView.setCardBackgroundColor(requireContext().getColor(R.color.green_light))
            cardView.strokeColor = requireContext().getColor(R.color.green_primary)
            titleView.setTextColor(requireContext().getColor(R.color.green_dark))
            descView.text = "Sudah terbuka"
            iconView.setImageResource(R.drawable.ic_quiz)
            iconView.setColorFilter(requireContext().getColor(R.color.green_primary))
            arrowView.setImageResource(R.drawable.ic_arrow_right)
            arrowView.setColorFilter(requireContext().getColor(R.color.green_primary))
        }
    }

    private fun navigateToLatihan(tingkat: String) {
        val bundle = Bundle().apply {
            putString("tingkat", tingkat)
        }
        findNavController().navigate(R.id.action_pilihLevel_to_latihan, bundle)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
