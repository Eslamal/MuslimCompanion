package com.eslamdev.islamic.data.repository

import android.content.Context
import com.eslamdev.islamic.data.model.HadithModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.io.BufferedReader
import java.io.InputStreamReader

class HadithRepository(private val context: Context) {

    suspend fun getAllHadiths(): List<HadithModel> {
        return withContext(Dispatchers.IO) {
            val hadithList = mutableListOf<HadithModel>()
            try {
                val inputStream = context.assets.open("hadith.json")
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val jsonString = bufferedReader.use { it.readText() }

                val jsonArray = JSONArray(jsonString)
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val fullText = obj.getString("hadith")
                    val description = obj.optString("description", "")

                    // فصل العنوان عن النص (تحسين المنطق)
                    val parts = fullText.split("\n\n", limit = 2)
                    val title = if (parts.isNotEmpty()) parts[0] else "حديث ${i + 1}"
                    val content = if (parts.size > 1) parts[1] else fullText

                    hadithList.add(HadithModel(i, title, content, description))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            hadithList
        }
    }
}