package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.data.remote.model.SoalApi
import com.app.manfaattumbuhan.databinding.ItemSoalGuruBinding

class SoalGuruAdapter(
    private val onEdit: (SoalApi) -> Unit,
    private val onDelete: (SoalApi) -> Unit
) : ListAdapter<SoalApi, SoalGuruAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemSoalGuruBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(soal: SoalApi) {
            binding.tvJudul.text = soal.judul
            binding.tvDeskripsi.text = parsePilihanPreview(soal.deskripsi)
            binding.tvTerakhirDiubah.text = "Dibuat: ${soal.created_at?.take(10) ?: "-"}"

            binding.btnEdit.setOnClickListener { onEdit(soal) }
            binding.btnDelete.setOnClickListener { onDelete(soal) }
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
            } catch (e: Exception) {
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
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SoalApi>() {
        override fun areItemsTheSame(oldItem: SoalApi, newItem: SoalApi) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SoalApi, newItem: SoalApi) = oldItem == newItem
    }
}
