package com.eslamdev.islamic.presentation.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R

data class ShortSurah(
    val name: String,
    val text: String,
    val info: String
)

class ShortSurahsAdapter(private val surahsList: List<ShortSurah>) :
    RecyclerView.Adapter<ShortSurahsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val surahName: TextView = itemView.findViewById(R.id.tv_surah_name)
        val surahText: TextView = itemView.findViewById(R.id.tv_surah_text)
        val surahInfo: TextView = itemView.findViewById(R.id.tv_surah_info)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_short_surah, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val surah = surahsList[position]
        holder.surahName.text = surah.name
        holder.surahInfo.text = surah.info

        // 🌟 السحر هنا: تمرير النص للدالة عشان تحط الأرقام
        holder.surahText.text = formatAyahText(surah.text)
    }

    override fun getItemCount(): Int = surahsList.size

    // ==========================================
    // دوال المساعدة لترقيم الآيات
    // ==========================================

    private fun formatAyahText(text: String): String {
        // بنقسم النص بناءً على علامة نهاية الآية القديمة
        val parts = text.split("۝")
        val builder = java.lang.StringBuilder()

        // بنلف على كل الآيات (ونستثني آخر جزء لأنه بيكون فاضي بعد آخر علامة)
        for (i in 0 until parts.size - 1) {
            val arabicNum = getArabicNumber(i + 1)
            // رجعنا العلامة القديمة (۝) وحطينا الرقم بعدها مباشرة
            // ملحوظة: الخطوط القرآنية بتفهم الترتيب ده وتدخل الرقم جوه الدائرة تلقائياً
            builder.append(parts[i].trim()).append(" ۝$arabicNum ")
        }

        // لو في نص متبقي بالصدفة بعد آخر علامة بنضيفه
        if (parts.last().isNotBlank()) {
            builder.append(parts.last().trim())
        }

        return builder.toString()
    }

    private fun getArabicNumber(number: Int): String {
        val arabicNumbers = arrayOf("٠", "١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩")
        val builder = java.lang.StringBuilder()
        for (char in number.toString()) {
            if (char.isDigit()) {
                builder.append(arabicNumbers[char.digitToInt()])
            } else {
                builder.append(char)
            }
        }
        return builder.toString()
    }
}