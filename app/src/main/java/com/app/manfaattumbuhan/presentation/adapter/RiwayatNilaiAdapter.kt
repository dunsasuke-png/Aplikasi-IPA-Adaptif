package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.data.remote.model.NilaiApi
import com.app.manfaattumbuhan.databinding.ItemRiwayatNilaiBinding

class RiwayatNilaiAdapter : ListAdapter<NilaiApi, RiwayatNilaiAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemRiwayatNilaiBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRiwayatNilaiBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        val isPreTest = item.soal_id?.contains("pre-test", ignoreCase = true) == true || 
                        item.soal?.judul?.contains("pre-test", ignoreCase = true) == true
                        
        val rawCatatan = item.catatan ?: "-"
        val cleanCatatan = if (rawCatatan.contains(" - Level")) {
            rawCatatan.substringBefore(" - Level")
        } else {
            rawCatatan
        }

        if (isPreTest) {
            holder.binding.tvTingkat.text = "Pre-test"
        } else {
            holder.binding.tvTingkat.text = item.soal?.judul ?: formatSoalId(item.soal_id ?: "")
        }
        
        holder.binding.tvDetail.text = cleanCatatan

        val detailsList = mutableListOf<String>()
        val waktu = item.waktu_pengerjaan
        if (waktu != null && waktu > 0) {
            val minutes = waktu / 60
            val seconds = waktu % 60
            val timeStr = if (minutes > 0) "${minutes}m ${seconds}s" else "${seconds}s"
            detailsList.add("Waktu: $timeStr")
        } else {
            detailsList.add("Waktu: -")
        }

        if (!isPreTest) {
            val sblm = item.kesulitan_sebelumnya
            if (!sblm.isNullOrBlank()) {
                detailsList.add("Sebelumnya: ${sblm.replaceFirstChar { it.uppercase() }}")
            } else {
                detailsList.add("Sebelumnya: -")
            }
        }

        val slnjt = item.kesulitan_selanjutnya
        if (!slnjt.isNullOrBlank()) {
            detailsList.add("Rekomendasi: ${slnjt.replaceFirstChar { it.uppercase() }}")
        } else {
            detailsList.add("Rekomendasi: -")
        }

        if (detailsList.isNotEmpty()) {
            holder.binding.tvDetail.text = "${holder.binding.tvDetail.text}\n${detailsList.joinToString("\n")}"
        }

        holder.binding.tvTanggal.text = item.created_at?.take(10) ?: "-"
        holder.binding.tvNilai.text = String.format("%.0f", item.nilai)
    }

    private fun formatSoalId(soalId: String): String {
        if (soalId.startsWith("latihan-")) {
            return "Latihan ${soalId.removePrefix("latihan-")}"
        }
        return "Latihan"
    }

    class DiffCallback : DiffUtil.ItemCallback<NilaiApi>() {
        override fun areItemsTheSame(oldItem: NilaiApi, newItem: NilaiApi) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NilaiApi, newItem: NilaiApi) = oldItem == newItem
    }
}
