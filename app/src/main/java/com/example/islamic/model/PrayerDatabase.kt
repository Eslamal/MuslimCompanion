package com.example.islamic.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PrayerTimingEntity::class], version = 1)
abstract class PrayerDatabase : RoomDatabase() {
    abstract fun prayerTimingDao(): PrayerTimingDao

    companion object {
        @Volatile
        private var INSTANCE: PrayerDatabase? = null

        fun getDatabase(context: Context): PrayerDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PrayerDatabase::class.java,
                    "prayer_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
