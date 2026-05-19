package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.databinding.ItemLaporanBinding
import com.app.manfaattumbuhan.presentation.guru.laporan.LaporanItem

class LaporanAdapter(
    private val onItemClick: (LaporanItem) -> Unit
) : ListAdapter<LaporanItem, LaporanAdapter.ViewHolder>(DiffCallback()) {

    class ViewHolder(val binding: ItemLaporanBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemLaporanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.tvNamaSiswa.text = item.siswa.nama
        holder.binding.tvTingkat.text = "Kelas: ${item.siswa.kelas}"
        holder.binding.tvTanggal.text = "NISN: ${item.siswa.nisn}"
        holder.binding.tvDetail.text = "${item.nilaiList.size} nilai"
        holder.binding.root.setOnClickListener {
            onItemClick(item)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<LaporanItem>() {
        override fun areItemsTheSame(oldItem: LaporanItem, newItem: LaporanItem) = oldItem.siswa.id == newItem.siswa.id
        override fun areContentsTheSame(oldItem: LaporanItem, newItem: LaporanItem) = oldItem == newItem
    }
}
