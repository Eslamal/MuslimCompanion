package com.eslamdev.islamic.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.eslamdev.islamic.R
import com.eslamdev.islamic.data.model.TafseerAya
import com.eslamdev.islamic.presentation.ui.adapter.TafseerDetailAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class TafseerDetailActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var surahNameTv: TextView
    private val adapter = TafseerDetailAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tafseer_detail)

        surahNameTv = findViewById(R.id.surah_name)
        recyclerView = findViewById(R.id.tafseer_recycler_view)

        val surahIndex = intent.getIntExtra("SURA_NO", 1)
        val surahName = intent.getStringExtra("SURA_NAME") ?: ""

        surahNameTv.text = "تفسير سورة $surahName"

        val btnBack = findViewById<ImageButton>(R.id.btn_back)
        btnBack.setOnClickListener {
            finish()
        }
        setupRecyclerView()
        loadTafseer(surahIndex)
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadTafseer(surahIndex: Int) {
        lifecycleScope.launch(Dispatchers.IO) {
            val list = ArrayList<TafseerAya>()
            try {

                val quranMap = HashMap<Int, String>()
                try {
                    val quranIs = assets.open("QuranDetails.json")
                    val quranJson = JSONObject(quranIs.bufferedReader().use { it.readText() })
                    val surahArray = quranJson.getJSONArray("surah")

                    for (i in 0 until surahArray.length()) {
                        val sObj = surahArray.getJSONObject(i)
                        if (sObj.getInt("index") == surahIndex) {
                            val ayas = sObj.getJSONArray("aya")
                            for (j in 0 until ayas.length()) {
                                val aObj = ayas.getJSONObject(j)
                                quranMap[aObj.getInt("index")] = aObj.getString("text")
                            }
                            break
                        }
                    }
                } catch (e: Exception) {
                    Log.e("TafseerError", "Error loading QuranDetails: ${e.message}")
                }

                val tafseerIs = assets.open("tafseer.json")
                val jsonString = tafseerIs.bufferedReader().use { it.readText() }

                val tafseerArray = JSONArray(jsonString)

                for (i in 0 until tafseerArray.length()) {
                    val tObj = tafseerArray.getJSONObject(i)

                    val jsonSuraNumStr = tObj.optString("number")
                    val jsonSuraNum = jsonSuraNumStr.toIntOrNull() ?: -1

                    if (jsonSuraNum == surahIndex) {
                        val ayaNumStr = tObj.optString("aya")
                        val ayaNum = ayaNumStr.toIntOrNull() ?: -1
                        val tafseerText = tObj.getString("text")

                        var ayaText = quranMap[ayaNum] ?: ""

                        if (ayaNum == 1 && surahIndex != 1 && surahIndex != 9 && ayaText.contains("بِسْمِ اللَّهِ")) {
                            ayaText = ayaText.replace("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "").trim()
                        }

                        if (ayaNum != -1) {
                            list.add(TafseerAya(ayaNum, ayaText, tafseerText))
                        }
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
                Log.e("TafseerError", "Error parsing tafseer: ${e.message}")
            }

            withContext(Dispatchers.Main) {
                if (list.isEmpty()) {
                    Toast.makeText(this@TafseerDetailActivity, "لا يوجد تفسير متاح لهذه السورة حالياً", Toast.LENGTH_SHORT).show()
                }
                adapter.submitList(list)
            }
        }
    }
}