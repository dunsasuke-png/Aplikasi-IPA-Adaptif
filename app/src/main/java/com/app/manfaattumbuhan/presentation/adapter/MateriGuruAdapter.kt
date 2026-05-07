package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.data.remote.model.MateriApi
import com.app.manfaattumbuhan.databinding.ItemMateriGuruBinding

class MateriGuruAdapter(
    private val onEdit: (MateriApi) -> Unit,
    private val onDelete: (MateriApi) -> Unit
) : ListAdapter<MateriApi, MateriGuruAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemMateriGuruBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(materi: MateriApi) {
            binding.tvNamaMateri.text = materi.nama
            binding.tvDeskripsiMateri.text = materi.deskripsi
            binding.tvUrutan.text = "#${materi.urutan}"
            binding.btnEdit.setOnClickListener { onEdit(materi) }
            binding.btnHapus.setOnClickListener { onDelete(materi) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMateriGuruBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<MateriApi>() {
        override fun areItemsTheSame(oldItem: MateriApi, newItem: MateriApi) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: MateriApi, newItem: MateriApi) = oldItem == newItem
    }
}
