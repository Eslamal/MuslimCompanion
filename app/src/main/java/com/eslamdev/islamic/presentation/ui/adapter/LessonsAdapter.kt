package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R

data class Lesson(
    val title: String,
    val sheikh: String,
    val content: String
)

class LessonsAdapter(
    private val lessonsList: List<Lesson>,
    private val onItemClick: (Lesson) -> Unit
) : RecyclerView.Adapter<LessonsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.tv_lesson_title)
        val sheikh: TextView = itemView.findViewById(R.id.tv_lesson_sheikh)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lesson, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val lesson = lessonsList[position]
        holder.title.text = lesson.title
        holder.sheikh.text = lesson.sheikh

        holder.itemView.setOnClickListener {
            onItemClick(lesson)
        }
    }

    override fun getItemCount(): Int = lessonsList.size
}