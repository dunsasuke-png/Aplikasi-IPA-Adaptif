package com.app.manfaattumbuhan.presentation.guru.akun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.local.TokenManager
import com.app.manfaattumbuhan.data.remote.model.SiswaInfo
import com.app.manfaattumbuhan.databinding.FragmentKelolaAkunBinding
import com.app.manfaattumbuhan.presentation.adapter.SiswaAdapter
import com.bumptech.glide.Glide
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class KelolaAkunFragment : Fragment() {

    private var _binding: FragmentKelolaAkunBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: KelolaAkunViewModel
    private var fullSiswaList = emptyList<SiswaInfo>()
    private var currentPage = 1
    private val pageSize = 6
    private lateinit var adapter: SiswaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentKelolaAkunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        TokenManager.init(requireContext())
        viewModel = ViewModelProvider(this)[KelolaAkunViewModel::class.java]

        adapter = SiswaAdapter(
            onEdit = { siswa -> showEditDialog(siswa) },
            onDelete = { siswa -> showDeleteDialog(siswa) }
        )
        binding.rvSiswa.layoutManager = LinearLayoutManager(context)
        binding.rvSiswa.adapter = adapter

        binding.btnPrevPage.setOnClickListener {
            if (currentPage > 1) {
                currentPage--
                updateUI()
            }
        }

        binding.btnNextPage.setOnClickListener {
            val maxPage = (fullSiswaList.size + pageSize - 1) / pageSize
            if (currentPage < maxPage) {
                currentPage++
                updateUI()
            }
        }

        binding.etSearchSiswa.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: android.text.Editable?) {
                viewModel.setSearchQuery(s.toString())
                currentPage = 1 // Reset to first page on search
            }
        })

        viewModel.loadSiswa()

        viewModel.siswaList.observe(viewLifecycleOwner) { list ->
            fullSiswaList = list
            binding.tvJumlahSiswa.text = "${list.size} siswa"
            updateUI()
        }


        viewModel.error.observe(viewLifecycleOwner) { err ->
            err?.let { Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show() }
        }

        binding.btnTambahSiswa.setOnClickListener {
            showAddDialog()
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
            findNavController().navigate(R.id.action_akun_to_profil)
        }
    }

    private fun updateUI() {
        if (fullSiswaList.isEmpty()) {
            adapter.submitList(emptyList())
            val query = binding.etSearchSiswa.text.toString().trim()
            if (query.isNotBlank()) {
                binding.tvEmptyState.text = "Siswa dengan nama \"$query\" tidak ditemukan."
            } else {
                binding.tvEmptyState.text = "Belum ada siswa yang terdaftar."
            }
            binding.tvEmptyState.visibility = View.VISIBLE
            binding.rvSiswa.visibility = View.GONE
            binding.layoutPagination.visibility = View.GONE
            return
        }

        binding.tvEmptyState.visibility = View.GONE
        binding.rvSiswa.visibility = View.VISIBLE

        binding.layoutPagination.visibility = View.VISIBLE
        val maxPage = (fullSiswaList.size + pageSize - 1) / pageSize
        if (currentPage > maxPage && maxPage > 0) currentPage = maxPage

        val start = (currentPage - 1) * pageSize
        val end = (start + pageSize).coerceAtMost(fullSiswaList.size)
        val pagedList = fullSiswaList.subList(start, end)

        adapter.submitList(pagedList)
        binding.tvPageIndicator.text = "Halaman $currentPage dari $maxPage"

        binding.btnPrevPage.isEnabled = currentPage > 1
        binding.btnNextPage.isEnabled = currentPage < maxPage
        binding.btnPrevPage.alpha = if (currentPage > 1) 1.0f else 0.3f
        binding.btnNextPage.alpha = if (currentPage < maxPage) 1.0f else 0.3f
    }

    private fun showAddDialog() {
        val layout = layoutInflater.inflate(R.layout.dialog_kelola_akun_form, null)
        val etNisn = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNisn)
        val etNama = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNama)
        val etKelas = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etKelas)
        val etPassword = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword)

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tambah Siswa Baru")
            .setView(layout)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val nisn = etNisn.text.toString().trim()
                val nama = etNama.text.toString().trim()
                val kelas = etKelas.text.toString().trim()
                val password = etPassword.text.toString().trim()

                val tvError = layout.findViewById<TextView>(R.id.tvErrorStatus)
                tvError.visibility = View.GONE

                if (nisn.isBlank() || nama.isBlank() || password.isBlank() || kelas.isBlank()) {
                    tvError.text = "⚠️ Semua field wajib diisi"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (password.length < 7) {
                    tvError.text = "⚠️ Password minimal 7 karakter"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                dialog.dismiss()
                viewModel.addSiswa(nisn, nama, kelas, password) {
                    Toast.makeText(requireContext(), "Siswa baru berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun showEditDialog(siswa: SiswaInfo) {
        val layout = layoutInflater.inflate(R.layout.dialog_kelola_akun_form, null)
        val etNisn = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNisn)
        val etNama = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etNama)
        val etKelas = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etKelas)
        val etPassword = layout.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etPassword)

        etNisn.setText(siswa.nisn)
        etNama.setText(siswa.nama)
        etKelas.setText(siswa.kelas)
        
        // Ensure hint change is applied to the TextInputLayout, not just EditText, but EditText hint acts as placeholder
        val tilPassword = etPassword.parent.parent as? com.google.android.material.textfield.TextInputLayout
        tilPassword?.hint = "Password Baru (opsional)"

        val dialog = MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Siswa")
            .setView(layout)
            .setPositiveButton("Simpan", null)
            .setNegativeButton("Batal", null)
            .create()

        dialog.setOnShowListener {
            val btnSimpan = dialog.getButton(androidx.appcompat.app.AlertDialog.BUTTON_POSITIVE)
            btnSimpan.setOnClickListener {
                val nama = etNama.text.toString().trim().ifBlank { null }
                val nisn = etNisn.text.toString().trim().ifBlank { null }
                val kelas = etKelas.text.toString().trim().ifBlank { null }
                val password = etPassword.text.toString().trim().ifBlank { null }

                val tvError = layout.findViewById<TextView>(R.id.tvErrorStatus)
                tvError.visibility = View.GONE

                val isNamaChanged = nama != null && nama != siswa.nama
                val isNisnChanged = nisn != null && nisn != siswa.nisn
                val isKelasChanged = kelas != null && kelas != siswa.kelas
                val isPasswordChanged = password != null

                if (!isNamaChanged && !isNisnChanged && !isKelasChanged && !isPasswordChanged) {
                    tvError.text = "⚠️ Tidak ada perubahan yang disimpan"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                if (password != null && password.length < 7) {
                    tvError.text = "⚠️ Password minimal 7 karakter"
                    tvError.visibility = View.VISIBLE
                    return@setOnClickListener
                }

                val updates = mutableListOf<String>()
                if (isNamaChanged) updates.add("Nama")
                if (isNisnChanged) updates.add("NISN")
                if (isKelasChanged) updates.add("Kelas")
                if (isPasswordChanged) updates.add("Password")

                val successMsg = when {
                    updates.size == 1 -> "${updates[0]} berhasil diperbarui"
                    updates.size == 2 -> "${updates[0]} dan ${updates[1]} berhasil diperbarui"
                    updates.size > 2 -> "${updates.dropLast(1).joinToString(", ")}, dan ${updates.last()} berhasil diperbarui"
                    else -> "Data siswa berhasil diperbarui"
                }

                dialog.dismiss()
                viewModel.updateSiswa(siswa.id, nama, nisn, kelas, password) {
                    Toast.makeText(requireContext(), successMsg, Toast.LENGTH_SHORT).show()
                }
            }
        }
        dialog.show()
    }

    private fun showDeleteDialog(siswa: SiswaInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Siswa")
            .setMessage("Apakah Anda yakin ingin menghapus ${siswa.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteSiswa(siswa.id) {
                    Toast.makeText(requireContext(), "Siswa ${siswa.nama} berhasil dihapus", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
