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
                val response = apiService.getNilaiList(token, userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val apiNilai = response.body()!!.data!!.nilai
                    val combined = mergeNilai(apiNilai, localNilai)
                    showList(combined)
                } else {
                    showList(localNilai)
                }
            } catch (_: Exception) {
                showList(localNilai)
            }
        }
    }

    private fun mergeNilai(apiList: List<NilaiApi>, localList: List<NilaiApi>): List<NilaiApi> {
        val apiKeys = apiList.map { api ->
            val normalized = normalizeSoalId(api.soal_id)
            if (normalized == "pretest") {
                "pretest"
            } else {
                normalized + api.nilai.toString() + (api.created_at?.take(10) ?: "")
            }
        }.toSet()

        val uniqueLocal = localList.filter { local ->
            val normalized = normalizeSoalId(local.soal_id)
            val key = if (normalized == "pretest") {
                "pretest"
            } else {
                normalized + local.nilai.toString() + (local.created_at?.take(10) ?: "")
            }
            key !in apiKeys
        }
        return (apiList + uniqueLocal)
    }

    private fun normalizeSoalId(soalId: String): String {
        val lowered = soalId.lowercase(Locale.ROOT)
        return lowered.replace("latihan-", "").replace("pre-test", "pretest")
    }

    private fun showList(list: List<NilaiApi>) {
        fullNilaiList = list.sortedByDescending { it.created_at }
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

        val maxPage = (fullNilaiList.size + pageSize - 1) / pageSize
        if (currentPage > maxPage && maxPage > 0) currentPage = maxPage

        val start = (currentPage - 1) * pageSize
        val end = (start + pageSize).coerceAtMost(fullNilaiList.size)
        val pagedList = fullNilaiList.subList(start, end)

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
