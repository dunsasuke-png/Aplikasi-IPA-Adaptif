package com.app.manfaattumbuhan.presentation.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.app.manfaattumbuhan.R
import com.app.manfaattumbuhan.databinding.ItemTumbuhanBinding
import com.app.manfaattumbuhan.domain.model.Tumbuhan
import com.bumptech.glide.Glide

class TumbuhanAdapter(
    private val onClick: (Tumbuhan) -> Unit
) : ListAdapter<Tumbuhan, TumbuhanAdapter.ViewHolder>(DiffCallback()) {

    var lockedIndices: Set<Int> = emptySet()

    inner class ViewHolder(private val binding: ItemTumbuhanBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tumbuhan: Tumbuhan, isLocked: Boolean) {
            binding.tvNama.text = tumbuhan.nama
            binding.tvDeskripsi.text = tumbuhan.deskripsi

            if (!tumbuhan.gambarUrl.isNullOrBlank()) {
                Glide.with(binding.root.context)
                    .load(tumbuhan.gambarUrl)
                    .placeholder(R.drawable.img_padi)
                    .error(R.drawable.img_padi)
                    .into(binding.imgTumbuhan)
            } else if (tumbuhan.imageRes != 0) {
                binding.imgTumbuhan.setImageResource(tumbuhan.imageRes)
            } else {
                binding.imgTumbuhan.setImageResource(R.drawable.img_padi)
            }

            if (isLocked) {
                binding.lockOverlay.visibility = View.VISIBLE
                binding.root.alpha = 0.6f
                binding.root.setOnClickListener(null)
            } else {
                binding.lockOverlay.visibility = View.GONE
                binding.root.alpha = 1f
                binding.root.setOnClickListener { onClick(tumbuhan) }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTumbuhanBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), lockedIndices.contains(position))
    }

    class DiffCallback : DiffUtil.ItemCallback<Tumbuhan>() {
        override fun areItemsTheSame(oldItem: Tumbuhan, newItem: Tumbuhan) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Tumbuhan, newItem: Tumbuhan) = oldItem == newItem
    }
}
