package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.data.remote.model.SiswaInfo
import com.app.manfaattumbuhan.databinding.ItemSiswaBinding

class SiswaAdapter(
    private val onEdit: ((SiswaInfo) -> Unit)? = null,
    private val onDelete: ((SiswaInfo) -> Unit)? = null
) : ListAdapter<SiswaInfo, SiswaAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemSiswaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(siswa: SiswaInfo) {
            binding.tvNamaSiswa.text = siswa.nama
            binding.tvKelasSiswa.text = "NISN: ${siswa.nim} | Kelas: ${siswa.kelas}"
            binding.imgAvatar.setImageResource(R.drawable.avatar_siswa)

            binding.btnEditSiswa.setOnClickListener { onEdit?.invoke(siswa) }
            binding.btnDeleteSiswa.setOnClickListener { onDelete?.invoke(siswa) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSiswaBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<SiswaInfo>() {
        override fun areItemsTheSame(oldItem: SiswaInfo, newItem: SiswaInfo) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SiswaInfo, newItem: SiswaInfo) = oldItem == newItem
    }
}
