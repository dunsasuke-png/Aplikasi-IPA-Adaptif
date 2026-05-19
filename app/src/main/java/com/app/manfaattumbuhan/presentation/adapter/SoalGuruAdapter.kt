package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.data.remote.model.SoalApi
import com.app.manfaattumbuhan.databinding.ItemSoalGuruBinding

class SoalGuruAdapter(
    private val onEdit: (SoalApi, Int) -> Unit,
    private val onDelete: (SoalApi, Int) -> Unit
) : ListAdapter<SoalApi, SoalGuruAdapter.ViewHolder>(DiffCallback()) {

    /**
     * Dipakai untuk membuat nomor tampilan tetap berurutan saat pagination.
     * Contoh: page 2 (itemsPerPage=5) => offset=5, jadi item pertama di page 2 tampil sebagai No. 6.
     */
    var pageOffset: Int = 0

    inner class ViewHolder(private val binding: ItemSoalGuruBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(soal: SoalApi, displayNumber: Int) {
            binding.tvNomor.text = "No. $displayNumber"
            binding.tvJudul.text = soal.judul
            binding.tvDeskripsi.text = parsePilihanPreview(soal.deskripsi)
            binding.tvTerakhirDiubah.text = "Dibuat: ${soal.created_at?.take(10) ?: "-"}"
            binding.tvTingkat.text = when (soal.tingkat) {
                "pretest" -> "Pre-test"
                "mudah" -> "Mudah"
                "sedang" -> "Sedang"
                "sulit" -> "Sulit"
                else -> "Pre-test"
            }

            binding.btnEdit.setOnClickListener { onEdit(soal, displayNumber) }
            binding.btnDelete.setOnClickListener { onDelete(soal, displayNumber) }
        }

        private fun parsePilihanPreview(deskripsi: String): String {
            return try {
                val json = org.json.JSONObject(deskripsi)
                val pilihanArray = json.getJSONArray("pilihan")
                val jawabanBenar = json.getInt("jawabanBenar")
                val labels = listOf("A", "B", "C", "D", "E", "F")
                val sb = StringBuilder()
                for (i in 0 until pilihanArray.length()) {
                    val label = if (i < labels.size) labels[i] else "${i + 1}"
                    val marker = if (i == jawabanBenar) " *" else ""
                    sb.append("$label. ${pilihanArray.getString(i)}$marker")
                    if (i < pilihanArray.length() - 1) sb.append("  |  ")
                }
                sb.toString()
            } catch (_: Exception) {
                deskripsi
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSoalGuruBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), pageOffset + position + 1)
    }

    class DiffCallback : DiffUtil.ItemCallback<SoalApi>() {
        override fun areItemsTheSame(oldItem: SoalApi, newItem: SoalApi) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SoalApi, newItem: SoalApi) = oldItem == newItem
    }
}
