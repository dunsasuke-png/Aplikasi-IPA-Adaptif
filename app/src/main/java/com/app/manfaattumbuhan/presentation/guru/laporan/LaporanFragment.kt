package com.app.manfaattumbuhan.presentation.guru.laporan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.databinding.FragmentLaporanBinding
import com.app.manfaattumbuhan.presentation.adapter.LaporanAdapter
import com.app.manfaattumbuhan.presentation.adapter.RiwayatNilaiAdapter
import com.bumptech.glide.Glide

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

        val adapter = LaporanAdapter { item ->
            showRiwayatDialog(item)
        }
        binding.rvLaporan.layoutManager = LinearLayoutManager(context)
        binding.rvLaporan.adapter = adapter

        val fotoUrl = TokenManager.getGuruFoto()
        if (fotoUrl.isNotBlank()) {
            Glide.with(this)
                .load(fotoUrl)
                .placeholder(R.drawable.avatar_guru)
                .error(R.drawable.avatar_guru)
                .into(binding.imgProfile)
        }

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

    private fun showRiwayatDialog(item: LaporanItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_riwayat_nilai_siswa, null)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val tvSubtitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogSubtitle)
        val tvEmpty = dialogView.findViewById<android.widget.TextView>(R.id.tvEmptyState)
        val rvRiwayat = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRiwayatDialog)

        tvTitle.text = "Riwayat Pengerjaan"
        tvSubtitle.text = "${item.siswa.nama} • Kelas ${item.siswa.kelas}"

        val riwayatAdapter = RiwayatNilaiAdapter()
        rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        rvRiwayat.adapter = riwayatAdapter

        if (item.nilaiList.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
            rvRiwayat.visibility = View.GONE
        } else {
            tvEmpty.visibility = View.GONE
            rvRiwayat.visibility = View.VISIBLE
            riwayatAdapter.submitList(item.nilaiList)
        }

        AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Tutup", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
