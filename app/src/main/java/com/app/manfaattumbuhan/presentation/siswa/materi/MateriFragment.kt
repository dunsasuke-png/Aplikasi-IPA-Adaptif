package com.app.manfaattumbuhan.presentation.siswa.materi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.repository.TumbuhanRepositoryImpl
import com.app.manfaattumbuhan.databinding.FragmentMateriBinding
import com.app.manfaattumbuhan.domain.usecase.GetTumbuhanUseCase
import com.app.manfaattumbuhan.presentation.adapter.TumbuhanAdapter

class MateriFragment : Fragment() {

    private var _binding: FragmentMateriBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: TumbuhanAdapter
    private val viewModel: MateriViewModel by viewModels {
        MateriViewModelFactory(GetTumbuhanUseCase(TumbuhanRepositoryImpl()))
    }

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

        adapter = TumbuhanAdapter { tumbuhan ->
            val bundle = Bundle().apply {
                putInt("tumbuhanId", tumbuhan.id)
                putString("tumbuhanNama", tumbuhan.nama)
                putString("tumbuhanDeskripsi", tumbuhan.deskripsi)
                putString("tumbuhanManfaat", tumbuhan.manfaat)
                putInt("tumbuhanImage", tumbuhan.imageRes)
                putString("tumbuhanGambarUrl", tumbuhan.gambarUrl ?: "")
                putString("tumbuhanVideoUrl", tumbuhan.videoUrl ?: "")
            }
            findNavController().navigate(R.id.action_materi_to_detail, bundle)
        }

        binding.rvTumbuhan.layoutManager = GridLayoutManager(context, 2)
        binding.rvTumbuhan.adapter = adapter

        viewModel.loadTumbuhan()
        viewModel.tumbuhanList.observe(viewLifecycleOwner) { list ->
            adapter.lockedIndices = getLockedIndices(list.size)
            adapter.submitList(list)
        }
    }

    override fun onResume() {
        super.onResume()
        val list = viewModel.tumbuhanList.value
        if (list != null) {
            adapter.lockedIndices = getLockedIndices(list.size)
            adapter.notifyDataSetChanged()
        }
    }

    private fun getLockedIndices(totalMateri: Int): Set<Int> {
        val userId = TokenManager.getUserId()
        val pretestDone = TokenManager.isPretestDone(userId)
        if (!pretestDone) return (0 until totalMateri).toSet()

        val everSulit = TokenManager.hasEverReachedSulit(userId)
        if (everSulit) return emptySet()

        val currentLevel = TokenManager.getCurrentLevel(userId)
        val maxUnlocked = when (currentLevel) {
            "Mudah" -> 2
            "Sedang" -> 4
            "Sulit" -> 6
            else -> 0
        }

        val locked = mutableSetOf<Int>()
        for (i in 0 until totalMateri) {
            val materiIndex = i + 1
            val alreadyStudied = TokenManager.isMateriStudied(userId, materiIndex)
            if (i >= maxUnlocked && !alreadyStudied) locked.add(i)
        }
        return locked
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
