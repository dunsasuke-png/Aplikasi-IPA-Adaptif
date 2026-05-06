package com.app.manfaattumbuhan.presentation.siswa.riwayat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.ApiConfig
import com.app.manfaattumbuhan.data.remote.ApiService
import com.app.manfaattumbuhan.databinding.FragmentRiwayatNilaiBinding
import com.app.manfaattumbuhan.presentation.adapter.RiwayatNilaiAdapter
import kotlinx.coroutines.launch

class RiwayatNilaiFragment : Fragment() {

    private var _binding: FragmentRiwayatNilaiBinding? = null
    private val binding get() = _binding!!
    private val apiService = ApiConfig.createService<ApiService>()

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

        val adapter = RiwayatNilaiAdapter()
        binding.rvRiwayat.layoutManager = LinearLayoutManager(context)
        binding.rvRiwayat.adapter = adapter

        binding.btnKembali.setOnClickListener {
            findNavController().navigateUp()
        }

        loadData(adapter)
    }

    private fun loadData(adapter: RiwayatNilaiAdapter) {
        val token = TokenManager.getToken()
        val userId = TokenManager.getUserId()

        if (token.isBlank() || userId.isBlank()) return

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getNilaiList(token, userId)
                if (response.isSuccessful && response.body()?.success == true) {
                    val nilaiList = response.body()!!.data!!.nilai
                    if (nilaiList.isEmpty()) {
                        binding.tvEmptyState.visibility = View.VISIBLE
                        binding.rvRiwayat.visibility = View.GONE
                    } else {
                        binding.tvEmptyState.visibility = View.GONE
                        binding.rvRiwayat.visibility = View.VISIBLE
                        adapter.submitList(nilaiList)
                    }
                } else {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvRiwayat.visibility = View.GONE
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
