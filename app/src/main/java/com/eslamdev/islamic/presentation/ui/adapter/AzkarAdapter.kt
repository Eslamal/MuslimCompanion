package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.data.model.ZekrModel

class AzkarAdapter : ListAdapter<ZekrModel, AzkarAdapter.ZekrViewHolder>(ZekrDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZekrViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_zekr, parent, false)
        return ZekrViewHolder(view)
    }

    override fun onBindViewHolder(holder: ZekrViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item)
    }

    class ZekrViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val zekrText: TextView = itemView.findViewById(R.id.zekr_text)
        private val repeatCount: TextView = itemView.findViewById(R.id.repeat_count)

        fun bind(item: ZekrModel) {
            zekrText.text = item.zekr
            val formattedCount = convertToEasternArabic(item.repeat.toString())
            repeatCount.text = itemView.context.getString(R.string.repeat_count_format, formattedCount)
        }

        private fun convertToEasternArabic(numberString: String): String {
            val arabicNumbers = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
            val builder = StringBuilder()
            for (char in numberString) {
                if (char.isDigit()) {
                    builder.append(arabicNumbers[Character.getNumericValue(char)])
                } else {
                    builder.append(char)
                }
            }
            return builder.toString()
        }
    }

    class ZekrDiffCallback : DiffUtil.ItemCallback<ZekrModel>() {
        override fun areItemsTheSame(oldItem: ZekrModel, newItem: ZekrModel): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ZekrModel, newItem: ZekrModel): Boolean {
            return oldItem == newItem
        }
    }
}