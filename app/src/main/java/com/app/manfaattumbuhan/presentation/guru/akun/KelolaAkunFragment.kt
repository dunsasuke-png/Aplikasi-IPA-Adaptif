package com.app.manfaattumbuhan.presentation.guru.akun

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
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

        val adapter = SiswaAdapter(
            onEdit = { siswa -> showEditDialog(siswa) },
            onDelete = { siswa -> showDeleteDialog(siswa) }
        )
        binding.rvSiswa.layoutManager = LinearLayoutManager(context)
        binding.rvSiswa.adapter = adapter

        viewModel.loadSiswa()

        viewModel.siswaList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.tvJumlahSiswa.text = "${list.size} siswa"
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

    private fun showAddDialog() {
        val layout = createUserFormLayout()
        val views = layout.tag as UserFormViews

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Tambah Siswa Baru")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val nim = views.etNim.text.toString().trim()
                val nama = views.etNama.text.toString().trim()
                val kelas = views.etKelas.text.toString().trim()
                val password = views.etPassword.text.toString().trim()

                if (nim.isNotBlank() && nama.isNotBlank() && password.isNotBlank() && kelas.isNotBlank()) {
                    if (password.length < 7) {
                        Toast.makeText(context, "Password minimal 7 karakter", Toast.LENGTH_SHORT).show()
                        return@setPositiveButton
                    }
                    viewModel.addSiswa(nim, nama, kelas, password)
                } else {
                    Toast.makeText(context, "Semua field wajib diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(siswa: SiswaInfo) {
        val layout = createUserFormLayout()
        val views = layout.tag as UserFormViews

        views.etNim.setText(siswa.nim)
        views.etNama.setText(siswa.nama)
        views.etKelas.setText(siswa.kelas)
        views.etPassword.hint = "Password (kosongkan jika tidak diubah)"

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Edit Siswa")
            .setView(layout)
            .setPositiveButton("Simpan") { _, _ ->
                val nama = views.etNama.text.toString().trim().ifBlank { null }
                val nim = views.etNim.text.toString().trim().ifBlank { null }
                val kelas = views.etKelas.text.toString().trim().ifBlank { null }
                val password = views.etPassword.text.toString().trim().ifBlank { null }

                if (password != null && password.length < 7) {
                    Toast.makeText(context, "Password minimal 7 karakter", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                viewModel.updateSiswa(siswa.id, nama, nim, kelas, password)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showDeleteDialog(siswa: SiswaInfo) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Hapus Siswa")
            .setMessage("Apakah Anda yakin ingin menghapus ${siswa.nama}?")
            .setPositiveButton("Hapus") { _, _ ->
                viewModel.deleteSiswa(siswa.id)
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun createUserFormLayout(): LinearLayout {
        val layout = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 16)
        }

        val etNim = EditText(requireContext()).apply { hint = "NISN" }
        val etNama = EditText(requireContext()).apply { hint = "Nama Lengkap" }
        val etKelas = EditText(requireContext()).apply { hint = "Kelas" }
        val etPassword = EditText(requireContext()).apply { hint = "Password" }

        layout.addView(etNim)
        layout.addView(etNama)
        layout.addView(etKelas)
        layout.addView(etPassword)

        layout.tag = UserFormViews(etNim, etNama, etKelas, etPassword)

        return layout
    }

    private data class UserFormViews(
        val etNim: EditText,
        val etNama: EditText,
        val etKelas: EditText,
        val etPassword: EditText
    )

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
