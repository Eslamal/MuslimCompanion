package com.eslamdev.islamic.presentation.ui.activity

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.presentation.ui.adapter.Companion
import com.eslamdev.islamic.presentation.ui.adapter.CompanionsAdapter
import org.json.JSONArray
import java.io.InputStreamReader

class CompanionsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_companions)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.rv_companions)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val companions = loadCompanionsFromJson()

        // لما المستخدم يضغط على كارت الصحابي، هنعرض القصة
        recyclerView.adapter = CompanionsAdapter(companions) { companion ->
            showStoryDialog(companion)
        }
    }

    private fun loadCompanionsFromJson(): List<Companion> {
        val list = mutableListOf<Companion>()
        try {
            val inputStream = assets.open("companions.json")
            val jsonString = InputStreamReader(inputStream).readText()
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                list.add(
                    Companion(
                        name = obj.getString("name"),
                        title = obj.getString("title"),
                        story = obj.getString("story")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    // دالة لعرض القصة بشكل جميل داخل Dialog
    private fun showStoryDialog(companion: Companion) {
        val scrollView = ScrollView(this)
        val textView = TextView(this)

        textView.text = companion.story
        textView.setPadding(50, 40, 50, 40)
        textView.textSize = 18f
        textView.setLineSpacing(12f, 1f)
        textView.setTextColor(resources.getColor(R.color.text_primary))

        scrollView.addView(textView)

        AlertDialog.Builder(this)
            .setTitle(companion.name)
            .setView(scrollView)
            .setPositiveButton("إغلاق") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}