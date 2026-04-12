package com.eslamdev.islamic.presentation.ui.adapter

import android.graphics.Color
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.databinding.ItemCalenderBinding
import java.text.SimpleDateFormat
import java.util.*

class DayAdapter : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private var days = listOf<Int>()
    private var selectedDay = -1

    private var currentCalendar: Calendar = Calendar.getInstance()

    var onDayClick: ((Int) -> Unit)? = null

    fun submitList(newDays: List<Int>, calendar: Calendar) {
        days = newDays
        currentCalendar = calendar.clone() as Calendar
        notifyDataSetChanged()
    }

    fun selectDay(day: Int) {
        selectedDay = day
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val binding = ItemCalenderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val day = days[position]
        holder.bind(day, day == selectedDay)
    }

    override fun getItemCount(): Int = days.size

    inner class DayViewHolder(private val binding: ItemCalenderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(day: Int, isSelected: Boolean) {
            val context = binding.root.context
            binding.date.text = day.toString()

            val tempCal = currentCalendar.clone() as Calendar
            tempCal.set(Calendar.DAY_OF_MONTH, day)

            val dayName = SimpleDateFormat("EEE", Locale("ar")).format(tempCal.time)
            binding.day.text = dayName

            val dayOfWeek = tempCal.get(Calendar.DAY_OF_WEEK)
            val isSunnahFastingDay = dayOfWeek == Calendar.MONDAY || dayOfWeek == Calendar.THURSDAY

            var isWhiteDay = false
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    val localDate = java.time.LocalDate.of(
                        tempCal.get(Calendar.YEAR),
                        tempCal.get(Calendar.MONTH) + 1,
                        day
                    )
                    val hijriDate = java.time.chrono.HijrahDate.from(localDate)
                    val hijriDay = hijriDate.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
                    isWhiteDay = hijriDay in 13..15
                } catch (e: Exception) {}
            }

            if (isSunnahFastingDay || isWhiteDay) {
                binding.ivFasting.visibility = View.VISIBLE
            } else {
                binding.ivFasting.visibility = View.GONE
            }

            if (isSelected) {
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
                binding.date.setTextColor(Color.WHITE)
                binding.day.setTextColor(Color.WHITE)
                binding.ivFasting.setColorFilter(Color.WHITE)
                binding.root.cardElevation = 8f
            } else {
                binding.root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.card_background))
                binding.date.setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                binding.day.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                binding.ivFasting.setColorFilter(ContextCompat.getColor(context, R.color.colorSecondary))
                binding.root.cardElevation = 0f
            }

            binding.root.setOnClickListener {
                selectedDay = day
                notifyDataSetChanged()
                onDayClick?.invoke(day)
            }
        }
    }
}