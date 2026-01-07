package com.eslamdev.islamic.presentation.ui.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.databinding.ItemCalenderBinding
import java.text.SimpleDateFormat
import java.util.*

class DayAdapter : RecyclerView.Adapter<DayAdapter.DayViewHolder>() {

    private var days = listOf<Int>()
    private var selectedDay = -1

    // متغير جديد عشان نعرف إحنا في شهر إيه وسنة كام
    private var currentCalendar: Calendar = Calendar.getInstance()

    var onDayClick: ((Int) -> Unit)? = null

    fun submitList(newDays: List<Int>, calendar: Calendar) {
        days = newDays
        // بناخد نسخة من الكالندر عشان نستخدمها في حساب الأيام
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
            binding.date.text = day.toString()

            // ### التصحيح هنا ###
            // بنستخدم الكالندر المبعوتة (للشهر المختار) مش الوقت الحالي
            val tempCal = currentCalendar.clone() as Calendar
            tempCal.set(Calendar.DAY_OF_MONTH, day)

            val dayName = SimpleDateFormat("EEE", Locale("ar")).format(tempCal.time)
            binding.day.text = dayName

            if (isSelected) {
                binding.root.setCardBackgroundColor(Color.parseColor("#006D5B"))
                binding.date.setTextColor(Color.WHITE)
                binding.day.setTextColor(Color.WHITE)
            } else {
                binding.root.setCardBackgroundColor(Color.WHITE)
                binding.date.setTextColor(Color.BLACK)
                binding.day.setTextColor(Color.GRAY)
            }

            binding.root.setOnClickListener {
                selectedDay = day
                notifyDataSetChanged()
                onDayClick?.invoke(day)
            }
        }
    }
}