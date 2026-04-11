package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R

data class Companion(
    val name: String,
    val title: String,
    val story: String
)

class CompanionsAdapter(
    private val companionsList: List<Companion>,
    private val onItemClick: (Companion) -> Unit
) : RecyclerView.Adapter<CompanionsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tv_companion_name)
        val title: TextView = itemView.findViewById(R.id.tv_companion_title)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_companion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val companion = companionsList[position]
        holder.name.text = companion.name
        holder.title.text = companion.title

        // تفعيل الضغط على الكارت
        holder.itemView.setOnClickListener {
            onItemClick(companion)
        }
    }

    override fun getItemCount(): Int = companionsList.size
}