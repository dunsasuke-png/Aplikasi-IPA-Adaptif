package com.app.manfaattumbuhan.presentation.siswa.riwayat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.data.local.StaticData
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.data.remote.model.NilaiApi
import com.app.manfaattumbuhan.data.remote.model.SoalReference
import com.app.manfaattumbuhan.databinding.FragmentRiwayatNilaiBinding
import com.app.manfaattumbuhan.presentation.adapter.RiwayatNilaiAdapter
import kotlinx.coroutines.launch
import java.util.*

class RiwayatNilaiFragment : Fragment() {

    private var _binding: FragmentRiwayatNilaiBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiConfig.createService<ApiService>()
    private var fullNilaiList = emptyList<NilaiApi>()
    private var currentPage = 1
    private val pageSize = 6
    private lateinit var adapter: RiwayatNilaiAdapter
    private var isSortAscending = false
    private var isSortedByNilai = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRiwayatNilaiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())

        adapter = RiwayatNilaiAdapter()
        binding.rvRiwayat.layoutManager = LinearLayoutManager(context)
        binding.rvRiwayat.adapter = adapter

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updateUI()
            }
        }

        binding.btnNextPage.setOnClickListener {
            val maxPage = (fullNilaiList.size + pageSize - 1) / pageSize
            if (currentPage < maxPage) {
                currentPage++
                updateUI()
            }
        }

        binding.btnSort.setOnClickListener {
            isSortedByNilai = true
            isSortAscending = !isSortAscending
            val sortMsg = if (isSortAscending) "Urut berdasarkan: Nilai Terendah" else "Urut berdasarkan: Nilai Tertinggi"
            android.widget.Toast.makeText(requireContext(), sortMsg, android.widget.Toast.LENGTH_SHORT).show()
            currentPage = 1
            updateUI()
        }

        loadData()
    }

    private fun loadData() {
        val token = TokenManager.getToken()
        val userId = TokenManager.getUserId()

        if (userId.isBlank()) return

        val localNilai = StaticData.getNilaiByUserId(userId).map { nilai ->
            NilaiApi(
                id = "local-${nilai.id}",
                siswa_id = nilai.siswaId,
                soal_id = "latihan-${nilai.tingkat}",
                nilai = nilai.nilai.toDouble(),
                catatan = "Benar ${nilai.benar} dari ${nilai.totalSoal} - Level ${nilai.tingkat}",
                created_at = nilai.tanggal,
                soal = SoalReference(judul = "Latihan ${nilai.tingkat}")
            )
        }

        if (token.isBlank()) {
            showList(localNilai)
            return
        }

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getNilaiList("eq.$userId")
                if (response.isSuccessful) {
                    val apiNilai = response.body() ?: emptyList()
                    // Jika API berhasil dan ada data → tampilkan API saja (tidak merge)
                    // Ini mencegah duplikat antara data Supabase dan cache lokal
                    if (apiNilai.isNotEmpty()) {
                        showList(apiNilai)
                    } else {
                        // API kosong → fallback ke local
                        showList(localNilai)
                    }
                } else {
                    showList(localNilai)
                }
            } catch (_: Exception) {
                showList(localNilai)
            }
        }
    }

    private fun normalizeSoalId(soalId: String): String {
        val lowered = soalId.lowercase(Locale.ROOT)
        return lowered.replace("latihan-", "").replace("pre-test", "pretest")
    }

    private fun showList(list: List<NilaiApi>) {
        fullNilaiList = list
        currentPage = 1
        updateUI()
    }

    private fun updateUI() {
        if (fullNilaiList.isEmpty()) {
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvRiwayat.visibility = View.GONE
            binding.layoutPagination.visibility = View.GONE
            return
        }

        binding.tvEmptyState.visibility = View.GONE
        binding.rvRiwayat.visibility = View.VISIBLE
        binding.layoutPagination.visibility = View.VISIBLE

        val sortedList = if (isSortedByNilai) {
            if (isSortAscending) {
                fullNilaiList.sortedBy { it.nilai }
            } else {
                fullNilaiList.sortedByDescending { it.nilai }
            }
        } else {
            fullNilaiList.sortedByDescending { it.created_at }
        }

        val maxPage = (sortedList.size + pageSize - 1) / pageSize
        if (currentPage > maxPage && maxPage > 0) currentPage = maxPage

        val start = (currentPage - 1) * pageSize
        val end = (start + pageSize).coerceAtMost(sortedList.size)
        val pagedList = sortedList.subList(start, end)

        adapter.submitList(pagedList)
        binding.tvPageIndicator.text = "Halaman $currentPage dari $maxPage"

        binding.btnPrevPage.isEnabled = currentPage > 1
        binding.btnNextPage.isEnabled = currentPage < maxPage
        binding.btnPrevPage.alpha = if (currentPage > 1) 1.0f else 0.3f
        binding.btnNextPage.alpha = if (currentPage < maxPage) 1.0f else 0.3f
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
