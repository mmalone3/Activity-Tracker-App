package com.activitytracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ActivityRecord::class], version = 1, exportSchema = false)
abstract class ActivityDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao

    companion object {
        @Volatile
        private var instance: ActivityDatabase? = null

        fun getInstance(context: Context): ActivityDatabase =
            instance ?: synchronized(this) {
                val appContext = context.applicationContext ?: context
                instance ?: Room.databaseBuilder(
                    appContext,
                    ActivityDatabase::class.java,
                    "activity_tracker_database.db"
                )
                .fallbackToDestructiveMigration()
                .build().also { instance = it }
            }
    }
}