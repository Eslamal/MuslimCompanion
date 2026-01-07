package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.data.model.SurahModel
import com.eslamdev.islamic.databinding.SurahItemForTafseerBinding
// ^ ملاحظة: هنستخدم نفس تصميم الـ XML القديم لأنه أشيك (surah_item_for_tafseer.xml)
// تأكد إن اسم الملف XML هو surah_item_for_tafseer.xml عشان الـ Binding ده يشتغل

class SurahAdapter(private val onClick: (SurahModel) -> Unit) :
    ListAdapter<SurahModel, SurahAdapter.SurahViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SurahViewHolder {
        val binding = SurahItemForTafseerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SurahViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SurahViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    inner class SurahViewHolder(private val binding: SurahItemForTafseerBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(surah: SurahModel) {
            binding.surahNumber.text = surah.id.toString()
            binding.arabicName.text = surah.name

            binding.root.setOnClickListener {
                onClick(surah)
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<SurahModel>() {
        override fun areItemsTheSame(oldItem: SurahModel, newItem: SurahModel) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: SurahModel, newItem: SurahModel) = oldItem == newItem
    }
}