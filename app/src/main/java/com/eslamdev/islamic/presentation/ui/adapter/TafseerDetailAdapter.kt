package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.data.model.TafseerAya
import com.eslamdev.islamic.databinding.TafseerItemLayoutBinding // تأكد من اسم ملف الـ XML

class TafseerDetailAdapter : ListAdapter<TafseerAya, TafseerDetailAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = TafseerItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: TafseerItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TafseerAya) {
            binding.ayaText.text = "${item.ayaText} (${item.ayaNumber})"
            binding.tafseerText.text = item.tafseerText
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<TafseerAya>() {
        override fun areItemsTheSame(oldItem: TafseerAya, newItem: TafseerAya) = oldItem.ayaNumber == newItem.ayaNumber
        override fun areContentsTheSame(oldItem: TafseerAya, newItem: TafseerAya) = oldItem == newItem
    }
}