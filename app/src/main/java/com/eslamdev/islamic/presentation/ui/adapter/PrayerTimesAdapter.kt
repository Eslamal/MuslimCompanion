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
    val isNext: Boolean = false,
    val isPassed: Boolean = false,
    val isSunrise: Boolean = false
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

        val lineTop: View = itemView.findViewById(R.id.timeline_line_top)
        val lineBottom: View = itemView.findViewById(R.id.timeline_line_bottom)
        val node: CardView = itemView.findViewById(R.id.timeline_node)
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

        holder.lineTop.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        holder.lineBottom.visibility = if (position == itemCount - 1) View.INVISIBLE else View.VISIBLE

        val colorPrimary = ContextCompat.getColor(context, R.color.colorPrimary)
        val colorPassed = Color.parseColor("#9E9E9E")
        val colorDefaultLine = Color.parseColor("#E0E0E0")
        val colorCardBg = ContextCompat.getColor(context, R.color.card_background)

        when {
            item.isNext -> {
                holder.card.setCardBackgroundColor(colorPrimary)
                holder.name.setTextColor(Color.WHITE)
                holder.time.setTextColor(Color.WHITE)
                holder.indicator.visibility = View.VISIBLE
                holder.indicator.setColorFilter(Color.WHITE)

                holder.node.setCardBackgroundColor(colorPrimary)
                holder.lineTop.setBackgroundColor(colorPrimary)
                holder.lineBottom.setBackgroundColor(colorDefaultLine)
            }
            item.isPassed -> {
                holder.card.setCardBackgroundColor(colorCardBg)
                holder.name.setTextColor(colorPassed)
                holder.time.setTextColor(colorPassed)
                holder.indicator.visibility = View.GONE

                holder.node.setCardBackgroundColor(colorPrimary)
                holder.lineTop.setBackgroundColor(colorPrimary)
                holder.lineBottom.setBackgroundColor(colorPrimary)
            }
            item.isSunrise -> {
                holder.card.setCardBackgroundColor(colorCardBg)
                holder.name.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary))
                holder.time.setTextColor(ContextCompat.getColor(context, R.color.colorSecondary))
                holder.indicator.visibility = View.GONE

                holder.node.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorSecondary))

                if (item.isPassed) {
                    holder.lineTop.setBackgroundColor(colorPrimary)
                    holder.lineBottom.setBackgroundColor(colorPrimary)
                } else {
                    holder.lineTop.setBackgroundColor(colorDefaultLine)
                    holder.lineBottom.setBackgroundColor(colorDefaultLine)
                }
            }
            else -> {
                holder.card.setCardBackgroundColor(colorCardBg)
                holder.name.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                holder.time.setTextColor(colorPrimary)
                holder.indicator.visibility = View.GONE

                holder.node.setCardBackgroundColor(colorDefaultLine)
                holder.lineTop.setBackgroundColor(colorDefaultLine)
                holder.lineBottom.setBackgroundColor(colorDefaultLine)
            }
        }
    }

    override fun getItemCount(): Int = items.size
}