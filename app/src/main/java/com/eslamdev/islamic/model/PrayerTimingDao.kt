package com.eslamdev.islamic.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface PrayerTimingDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayerTiming(prayerTiming: PrayerTimingEntity)

    @Query("SELECT * FROM prayer_timing WHERE date = :date")
    suspend fun getPrayerTimingByDate(date: String): PrayerTimingEntity?

    @Query("DELETE FROM prayer_timing")
    suspend fun deleteAllTimings()
}
