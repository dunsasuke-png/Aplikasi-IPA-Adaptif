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
        holder.binding.tvTingkat.text = item.soal?.judul ?: formatSoalId(item.soal_id)
        holder.binding.tvDetail.text = item.catatan ?: "-"
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
