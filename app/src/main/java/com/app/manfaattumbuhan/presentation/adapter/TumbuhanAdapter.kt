package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.databinding.ItemTumbuhanBinding
import com.app.manfaattumbuhan.domain.model.Tumbuhan

class TumbuhanAdapter(
    private val onClick: (Tumbuhan) -> Unit
) : ListAdapter<Tumbuhan, TumbuhanAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(private val binding: ItemTumbuhanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tumbuhan: Tumbuhan) {
            binding.tvNama.text = tumbuhan.nama
            binding.tvDeskripsi.text = tumbuhan.deskripsi
            binding.imgTumbuhan.setImageResource(tumbuhan.imageRes)
            binding.root.setOnClickListener { onClick(tumbuhan) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTumbuhanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Tumbuhan>() {
        override fun areItemsTheSame(oldItem: Tumbuhan, newItem: Tumbuhan) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tumbuhan, newItem: Tumbuhan) = oldItem == newItem
    }
}
