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
    private var fullLaporanList = emptyList<LaporanItem>()
    private var currentPage = 1
    private val pageSize = 6
    private lateinit var mainAdapter: LaporanAdapter

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

        mainAdapter = LaporanAdapter { item ->
            showRiwayatDialog(item)
        }
        binding.rvLaporan.layoutManager = LinearLayoutManager(context)
        binding.rvLaporan.adapter = mainAdapter

        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updateLaporanUI()
            }
        }

        binding.btnNextPage.setOnClickListener {
            val maxPage = (fullLaporanList.size + pageSize - 1) / pageSize
            if (currentPage < maxPage) {
                currentPage++
                updateLaporanUI()
            }
        }

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
            fullLaporanList = list
            updateLaporanUI()
        }

        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }
    }

    private fun updateLaporanUI() {
        if (fullLaporanList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvLaporan.visibility = View.GONE
            binding.layoutPagination.visibility = View.GONE
            return
        }

        binding.tvEmptyState.visibility = View.GONE
        binding.rvLaporan.visibility = View.VISIBLE
        binding.layoutPagination.visibility = View.VISIBLE

        val maxPage = (fullLaporanList.size + pageSize - 1) / pageSize
        if (currentPage > maxPage && maxPage > 0) currentPage = maxPage

        val start = (currentPage - 1) * pageSize
        val end = (start + pageSize).coerceAtMost(fullLaporanList.size)
        val pagedList = fullLaporanList.subList(start, end)

        mainAdapter.submitList(pagedList)
        binding.tvPageIndicator.text = "Halaman $currentPage dari $maxPage"

        binding.btnPrevPage.isEnabled = currentPage > 1
        binding.btnNextPage.isEnabled = currentPage < maxPage
        binding.btnPrevPage.alpha = if (currentPage > 1) 1.0f else 0.3f
        binding.btnNextPage.alpha = if (currentPage < maxPage) 1.0f else 0.3f
    }

    private fun showRiwayatDialog(item: LaporanItem) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_riwayat_nilai_siswa, null)
        val tvTitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogTitle)
        val tvSubtitle = dialogView.findViewById<android.widget.TextView>(R.id.tvDialogSubtitle)
        val tvEmpty = dialogView.findViewById<android.widget.TextView>(R.id.tvEmptyState)
        val rvRiwayat = dialogView.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvRiwayatDialog)
        
        val btnPrev = dialogView.findViewById<android.widget.ImageButton>(R.id.btnPrevPageDialog)
        val btnNext = dialogView.findViewById<android.widget.ImageButton>(R.id.btnNextPageDialog)
        val tvIndicator = dialogView.findViewById<android.widget.TextView>(R.id.tvPageIndicatorDialog)
        val layoutPagination = dialogView.findViewById<android.view.View>(R.id.layoutPaginationDialog)

        tvTitle.text = "Riwayat Pengerjaan"
        tvSubtitle.text = "${item.siswa.nama} • Kelas ${item.siswa.kelas}"

        val riwayatAdapter = RiwayatNilaiAdapter()
        rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        rvRiwayat.adapter = riwayatAdapter

        var dialogCurrentPage = 1
        val dialogPageSize = 6
        val sortedList = item.nilaiList.sortedByDescending { it.created_at }

        fun updateDialogUI() {
            if (sortedList.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                rvRiwayat.visibility = View.GONE
                layoutPagination?.visibility = View.GONE
                return
            }

            tvEmpty.visibility = View.GONE
            rvRiwayat.visibility = View.VISIBLE
            layoutPagination?.visibility = View.VISIBLE

            val maxPage = (sortedList.size + dialogPageSize - 1) / dialogPageSize
            val start = (dialogCurrentPage - 1) * dialogPageSize
            val end = (start + dialogPageSize).coerceAtMost(sortedList.size)
            val pagedList = sortedList.subList(start, end)

            riwayatAdapter.submitList(pagedList)
            tvIndicator?.text = "Halaman $dialogCurrentPage dari $maxPage"

            btnPrev?.isEnabled = dialogCurrentPage > 1
            btnNext?.isEnabled = dialogCurrentPage < maxPage
            btnPrev?.alpha = if (dialogCurrentPage > 1) 1.0f else 0.3f
            btnNext?.alpha = if (dialogCurrentPage < maxPage) 1.0f else 0.3f
        }

        btnPrev?.setOnClickListener {
            if (dialogCurrentPage > 1) {
                dialogCurrentPage--
                updateDialogUI()
            }
        }

        btnNext?.setOnClickListener {
            val maxPage = (sortedList.size + dialogPageSize - 1) / dialogPageSize
            if (dialogCurrentPage < maxPage) {
                dialogCurrentPage++
                updateDialogUI()
            }
        }

        updateDialogUI()

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setPositiveButton("Tutup", null)
            .create()
        
        dialog.show()
        
        dialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.95).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
