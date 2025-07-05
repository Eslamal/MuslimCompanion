package com.example.islamic.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.islamic.R
import com.example.islamic.model.Day
import java.text.SimpleDateFormat
import java.util.*

class PrayerAdapter(
    private var daysList: List<Day>,
    private val clickListener: OnClickDayListener,
    private val locale: Locale
) : RecyclerView.Adapter<PrayerAdapter.ViewHolder>() {

    private var selectedPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_calender, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentDay = daysList[position]
        holder.bind(currentDay)
        if (currentDay.selected) {
            holder.itemView.setBackgroundResource(R.drawable.selected_day_background)
            holder.num.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
            holder.day.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.white))
        } else if (currentDay.today) {
            holder.itemView.setBackgroundResource(R.drawable.today_day_background)
            holder.num.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.day.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        } else {
            holder.itemView.setBackgroundResource(R.drawable.background)
            holder.num.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
            holder.day.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.black))
        }
    }

    override fun getItemCount(): Int {
        return daysList.size
    }

    fun setData(newList: List<Day>) {
        daysList = newList
        selectedPosition = RecyclerView.NO_POSITION
        val currentDayIndex = daysList.indexOfFirst { it.today }
        if (currentDayIndex != -1) {
            if (selectedPosition == RecyclerView.NO_POSITION) {
                daysList[currentDayIndex].selected = true
                selectedPosition = currentDayIndex
            }
        }
        notifyDataSetChanged()
    }


    fun setSelectedDay(dayToSelect: Day) {
        val newSelectedPosition = daysList.indexOf(dayToSelect)
        if (newSelectedPosition != RecyclerView.NO_POSITION) {
            if (selectedPosition != RecyclerView.NO_POSITION && selectedPosition < daysList.size) {
                if (daysList.indices.contains(selectedPosition)) {
                    daysList[selectedPosition].selected = false
                    notifyItemChanged(selectedPosition)
                }
            }
            selectedPosition = newSelectedPosition
            daysList[selectedPosition].selected = true
            notifyItemChanged(selectedPosition)
        }
    }


    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {

        var num: TextView = view.findViewById(R.id.date)
        var day: TextView = view.findViewById(R.id.day)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            when (p0) {
                itemView -> {
                    val clickedDay = daysList[adapterPosition]
                    setSelectedDay(clickedDay)
                    clickListener.onDayClick(clickedDay)
                }
            }
        }

        fun bind(dayData: Day) {
            num.text = convertToEasternArabic(dayData.day.toString())

            val calendar = Calendar.getInstance(locale)
            calendar.set(Calendar.YEAR, dayData.year)
            calendar.set(Calendar.MONTH, dayData.month - 1)
            calendar.set(Calendar.DAY_OF_MONTH, dayData.day)

            val dayOfWeekFormat = SimpleDateFormat("EE", locale)
            day.text = dayOfWeekFormat.format(calendar.time)
            if (dayData.today && !dayData.selected) {
                day.text = itemView.context.getString(R.string.today)
            }
        }
    }

    interface OnClickDayListener {
        fun onDayClick(item: Day)
    }

    private fun convertToEasternArabic(numberString: String): String {
        val arabicNumbers = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
        val builder = StringBuilder()
        for (char in numberString) {
            if (char.isDigit()) {
                builder.append(arabicNumbers[char.toString().toInt()])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }
}