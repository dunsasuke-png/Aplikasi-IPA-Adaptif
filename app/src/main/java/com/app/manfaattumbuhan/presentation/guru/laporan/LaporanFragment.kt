package com.app.manfaattumbuhan.presentation.guru.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentLaporanBinding
import com.app.manfaattumbuhan.presentation.adapter.LaporanAdapter

class LaporanFragment : Fragment() {

    private var _binding: FragmentLaporanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LaporanViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLaporanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())

        val adapter = LaporanAdapter()
        binding.rvLaporan.layoutManager = LinearLayoutManager(context)
        binding.rvLaporan.adapter = adapter

        binding.imgProfile.setOnClickListener {
            findNavController().navigate(R.id.action_laporan_to_profil)
        }

        viewModel.loadData()

        viewModel.laporanList.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvLaporan.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvLaporan.visibility = View.VISIBLE
                adapter.submitList(list)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
