package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.data.model.DuaCategory
import com.eslamdev.islamic.data.model.DuaItem
import com.eslamdev.islamic.data.model.DuaListItem

class DuaAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var items: List<DuaListItem> = emptyList()

    fun submitList(newItems: List<DuaListItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return when (items[position]) {
            is DuaCategory -> R.layout.category_header_layout
            is DuaItem -> R.layout.dua_item_layout
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
        return when (viewType) {
            R.layout.category_header_layout -> CategoryViewHolder(view)
            else -> DuaViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = items[position]) {
            is DuaCategory -> (holder as CategoryViewHolder).bind(item)
            is DuaItem -> {
                (holder as DuaViewHolder).bind(item)

                holder.itemView.setOnClickListener {
                    onItemClick(item.content)
                }
            }
        }
    }

    override fun getItemCount(): Int = items.size

    class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_category_title)
        fun bind(item: DuaCategory) {
            title.text = item.title
        }
    }

    class DuaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tv_dua_title)
        private val text: TextView = itemView.findViewById(R.id.tv_dua_text)
        fun bind(item: DuaItem) {
            title.text = item.title
            text.text = item.content
        }
    }
}