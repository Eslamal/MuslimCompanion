package com.example.islamic.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object PrayerService {
    private const val BASE_URL = " https://api.aladhan.com"
    private val client = OkHttpClient.Builder().build()

    private fun getRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL).client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val prayerService: PrayerApi = getRetrofit()
        .create(PrayerApi::class.java)
}