package com.eslamdev.islamic.presentation.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R

data class PrayerDisplayItem(
    val name: String,
    val time: String,
    val isNext: Boolean = false
)

class PrayerTimesAdapter : RecyclerView.Adapter<PrayerTimesAdapter.ViewHolder>() {

    private var items: List<PrayerDisplayItem> = emptyList()

    fun submitList(newItems: List<PrayerDisplayItem>) {
        items = newItems
        notifyDataSetChanged()
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: CardView = itemView.findViewById(R.id.card_prayer_root)
        val name: TextView = itemView.findViewById(R.id.tv_prayer_name)
        val time: TextView = itemView.findViewById(R.id.tv_prayer_time)
        val indicator: ImageView = itemView.findViewById(R.id.iv_next_indicator)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pray, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        val context = holder.itemView.context

        holder.name.text = item.name
        holder.time.text = item.time

        if (item.isNext) {
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
            holder.name.setTextColor(Color.WHITE)
            holder.time.setTextColor(Color.WHITE)
            holder.indicator.visibility = View.VISIBLE
        } else {
            holder.card.setCardBackgroundColor(Color.WHITE)
            holder.name.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
            holder.time.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
            holder.indicator.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = items.size
}