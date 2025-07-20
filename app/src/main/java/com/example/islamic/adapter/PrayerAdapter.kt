package com.example.islamic.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.islamic.R
import com.example.islamic.databinding.ItemCalenderBinding
import com.example.islamic.model.Day
import java.util.*

class PrayerAdapter(
    private var days: List<Day>,
    private val listener: OnClickDayListener,
    private val arabicLocale: Locale
) : RecyclerView.Adapter<PrayerAdapter.ViewHolder>() {

    private var selectedDay: Day? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCalenderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day)
    }

    override fun getItemCount(): Int = days.size

    fun setData(newDays: List<Day>) {
        this.days = newDays
        this.selectedDay = newDays.find { it.isToday } ?: newDays.firstOrNull()
        notifyDataSetChanged()
    }

    fun setSelectedDay(day: Day) {
        selectedDay = day
        notifyDataSetChanged()
    }

    fun isCurrentDaySelectedToday(): Boolean {
        return selectedDay?.isToday ?: false
    }

    inner class ViewHolder(private val binding: ItemCalenderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: Day) {
            binding.date.text = convertToEasternArabic(day.dayNum.toString())
            binding.day.text = getArabicDayName(day.dayOfWeekEn)

            val context = itemView.context

            // *** بداية المنطق الجديد لتمييز الخلفية ***
            when {
                // إذا كان اليوم هو اليوم المحدد
                day == selectedDay -> {
                    binding.clCalendarItem.setBackgroundResource(R.drawable.today_item_background)
                    binding.date.setTextColor(ContextCompat.getColor(context, R.color.green_quran))
                    binding.day.setTextColor(ContextCompat.getColor(context, R.color.green_quran))
                }
                // إذا كان اليوم هو اليوم الحالي (وليس المحدد)
                day.isToday -> {
                    binding.clCalendarItem.setBackgroundResource(R.drawable.today_day_background)
                    binding.date.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    binding.day.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
                // أي يوم آخر
                else -> {
                    binding.clCalendarItem.setBackgroundResource(R.drawable.neumorphic_card_background)
                    binding.date.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    binding.day.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            }
            // *** نهاية المنطق الجديد ***

            itemView.setOnClickListener {
                listener.onDayClick(day)
            }
        }
    }

    interface OnClickDayListener {
        fun onDayClick(item: Day)
    }

    // ... باقي الدوال المساعدة تبقى كما هي ...
    private fun getArabicDayName(dayOfWeekEn: String): String {
        return when (dayOfWeekEn.lowercase(Locale.US)) {
            "saturday" -> "السبت"
            "sunday" -> "الأحد"
            "monday" -> "الاثنين"
            "tuesday" -> "الثلاثاء"
            "wednesday" -> "الأربعاء"
            "thursday" -> "الخميس"
            "friday" -> "الجمعة"
            else -> dayOfWeekEn
        }
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