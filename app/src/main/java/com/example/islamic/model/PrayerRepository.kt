package com.example.islamic.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.islamic.api.PrayerApi
import com.example.islamic.model.PrayerData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response

class PrayerRepository(private val prayerApi: PrayerApi) {

    private val _prayerData = MutableLiveData<PrayerData?>()
    val prayerData: LiveData<PrayerData?> get() = _prayerData

    suspend fun getPrayerTimes(latitude: String, longitude: String, month: String, year: String) {
        withContext(Dispatchers.IO) {
            try {
                val response: Response<PrayerData?> = prayerApi.getPrayerTimes(latitude, longitude, month, year)
                if (response.isSuccessful) {
                    _prayerData.postValue(response.body())
                } else {
                    _prayerData.postValue(null)
                }
            } catch (e: Exception) {
                _prayerData.postValue(null)
            }
        }
    }
}
