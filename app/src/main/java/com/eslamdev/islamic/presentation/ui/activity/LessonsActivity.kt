package com.eslamdev.islamic.presentation.ui.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.presentation.ui.adapter.Lesson
import com.eslamdev.islamic.presentation.ui.adapter.LessonsAdapter
import org.json.JSONArray
import java.io.InputStreamReader

class LessonsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lessons)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.rv_lessons)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val lessons = loadLessonsFromJson()

        recyclerView.adapter = LessonsAdapter(lessons) { lesson ->
            showLessonDialog(lesson)
        }
    }

    private fun loadLessonsFromJson(): List<Lesson> {
        val list = mutableListOf<Lesson>()
        try {
            val inputStream = assets.open("lessons.json")
            val jsonString = InputStreamReader(inputStream).readText()
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Lesson(
                        title = obj.getString("title"),
                        sheikh = obj.getString("sheikh"),
                        content = obj.getString("content")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    private fun showLessonDialog(lesson: Lesson) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_story, null)

        val dialogTitle = dialogView.findViewById<TextView>(R.id.dialog_title)
        val dialogSubtitle = dialogView.findViewById<TextView>(R.id.dialog_subtitle)
        val dialogText = dialogView.findViewById<TextView>(R.id.dialog_story_text)
        val btnClose = dialogView.findViewById<Button>(R.id.btn_close)
        val btnCopy = dialogView.findViewById<Button>(R.id.btn_copy)

        dialogTitle.text = lesson.title
        dialogSubtitle.text = lesson.sheikh
        dialogText.text = lesson.content

        val builder = AlertDialog.Builder(this)
        builder.setView(dialogView)
        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        btnClose.setOnClickListener { dialog.dismiss() }

        btnCopy.setOnClickListener {
            val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val copyText = "🎙️ ${lesson.title}\n\n${lesson.content}\n\n- من قسم الخطب والدروس"
            val clip = ClipData.newPlainText("Lesson Content", copyText)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "تم نسخ الخطبة بنجاح", Toast.LENGTH_SHORT).show()
        }

        dialog.show()
    }
}