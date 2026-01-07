package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.data.model.HadithModel
import com.eslamdev.islamic.databinding.HadithItemLayoutBinding // تأكد إن الـ XML اسمه كدا أو عدله

class HadithAdapter(private val onClick: (HadithModel) -> Unit) :
    ListAdapter<HadithModel, HadithAdapter.HadithViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HadithViewHolder {
        val binding = HadithItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HadithViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HadithViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class HadithViewHolder(private val binding: HadithItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(hadith: HadithModel) {
            binding.hadithId.text = "${position + 1}"
            binding.hadithTitle.text = hadith.title
            binding.root.setOnClickListener { onClick(hadith) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<HadithModel>() {
        override fun areItemsTheSame(oldItem: HadithModel, newItem: HadithModel) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: HadithModel, newItem: HadithModel) = oldItem == newItem
    }
}