package com.eslamdev.islamic.data.repository

import android.content.Context
import com.eslamdev.islamic.data.model.SurahModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject

class QuranRepository(private val context: Context) {

    suspend fun getSurahList(): List<SurahModel> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<SurahModel>()
            try {
                val inputStream = context.assets.open("Quran.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }

                val jsonObject = JSONObject(jsonString)
                val jsonArray = jsonObject.getJSONArray("surah")

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val id = obj.getInt("index")
                    val name = obj.getString("name")
                    list.add(SurahModel(id, name))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            list.sortedBy { it.id }
        }
    }
}