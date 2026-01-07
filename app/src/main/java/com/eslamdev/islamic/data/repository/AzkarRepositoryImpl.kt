package com.eslamdev.islamic.data.repository

import android.content.Context
import com.eslamdev.islamic.data.model.ZekrModel
import com.eslamdev.islamic.domain.repository.AzkarType
import com.eslamdev.islamic.domain.repository.IAzkarRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

class AzkarRepositoryImpl(private val context: Context) : IAzkarRepository {

    override suspend fun getAzkar(type: AzkarType): List<ZekrModel> {
        // بنستخدم withContext(Dispatchers.IO) عشان العملية دي تاخد وقت ومتهنجش الواجهة
        return withContext(Dispatchers.IO) {
            val zekrList = mutableListOf<ZekrModel>()
            try {
                val inputStream = context.assets.open(type.fileName)
                val bufferedReader = BufferedReader(InputStreamReader(inputStream))
                val stringBuilder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    stringBuilder.append(line)
                }
                inputStream.close()

                val jsonObject = JSONObject(stringBuilder.toString())
                val jsonArray = jsonObject.getJSONArray("content")

                for (i in 0 until jsonArray.length()) {
                    val data = jsonArray.getJSONObject(i)
                    val zekrText = data.getString("zekr")
                    val repeat = data.getInt("repeat")
                    // بنضيف الذكر للقائمة كمودل كامل
                    zekrList.add(ZekrModel(id = i, zekr = zekrText, repeat = repeat))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            zekrList // بيرجع القائمة في الآخر
        }
    }
}