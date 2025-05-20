package com.example.travelbuddy.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Destination::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun destinationDao(): DestinationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "travel_buddy_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
