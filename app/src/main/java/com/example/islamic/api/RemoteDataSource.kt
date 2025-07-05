package com.example.islamic.api

import android.util.Log
import com.example.islamic.model.PrayerData

class RemoteDataSource() {
    suspend fun getPrayerTimes(lat:String,long:String,month:String,year:String): PrayerData?{
        val response = PrayerService.prayerService.getPrayerTimes(lat,long,month,year)
        try {
            if (response.isSuccessful) {
                response.body()?.let {
                    Log.i("Menna", "getPrayerTimes response " + it)
                    return it
                }
            } else {
                Log.i("Menna", "getPrayerTimes response failuer " + response.errorBody().toString())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.i("Menna", "getPrayerTimes error? " + e.printStackTrace())
        }
        return null
    }
}