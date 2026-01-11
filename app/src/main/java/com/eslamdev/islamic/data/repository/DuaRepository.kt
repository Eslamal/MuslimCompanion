package com.eslamdev.islamic.data.repository

import android.content.Context
import com.eslamdev.islamic.data.model.DuaCategory
import com.eslamdev.islamic.data.model.DuaItem
import com.eslamdev.islamic.data.model.DuaListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class DuaRepository(private val context: Context) {

    suspend fun getDuas(): List<DuaListItem> {
        return withContext(Dispatchers.IO) {
            val fullList = mutableListOf<DuaListItem>()
            try {
                val inputStream = context.assets.open("duas_collection.json")
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                val categoriesArray = JSONArray(jsonString)

                for (i in 0 until categoriesArray.length()) {
                    val catObj = categoriesArray.getJSONObject(i)
                    val catTitle = catObj.getString("category")

                    fullList.add(DuaCategory(catTitle))

                    val duasArray = catObj.getJSONArray("duas")
                    for (j in 0 until duasArray.length()) {
                        val duaObj = duasArray.getJSONObject(j)
                        val title = duaObj.getString("title")
                        val content = duaObj.getString("dua")

                        fullList.add(DuaItem(title, content, catTitle))
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            fullList
        }
    }
}