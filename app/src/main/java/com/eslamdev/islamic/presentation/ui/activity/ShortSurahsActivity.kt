package com.eslamdev.islamic.presentation.ui.activity

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.presentation.ui.adapter.ShortSurah
import com.eslamdev.islamic.presentation.ui.adapter.ShortSurahsAdapter
import org.json.JSONArray
import java.io.InputStreamReader

class ShortSurahsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_short_surahs)

        findViewById<ImageButton>(R.id.btn_back).setOnClickListener { finish() }

        val recyclerView = findViewById<RecyclerView>(R.id.rv_short_surahs)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val surahs = loadSurahsFromJson()

        recyclerView.adapter = ShortSurahsAdapter(surahs)
    }

    private fun loadSurahsFromJson(): List<ShortSurah> {
        val list = mutableListOf<ShortSurah>()
        try {
            val inputStream = assets.open("short_surahs.json")
            val jsonString = InputStreamReader(inputStream).readText()

            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)

                val startAya = if (jsonObject.has("startAyahNumber")) {
                    jsonObject.getInt("startAyahNumber")
                } else {
                    1
                }

                list.add(
                    ShortSurah(
                        name = jsonObject.getString("name"),
                        info = jsonObject.getString("info"),
                        text = jsonObject.getString("text"),
                        startAyahNumber = startAya
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }
}